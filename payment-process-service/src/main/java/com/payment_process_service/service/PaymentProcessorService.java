package com.payment_process_service.service;

import com.payment_process_service.client.AccountClient;
import com.payment_process_service.dto.TransferRequest;
import com.payment_process_service.enums.PaymentStatus;
import com.payment_process_service.event.PaymentStatusEvent;
import com.payment_process_service.model.Payment;
import com.payment_process_service.outbox.enums.OutboxStatus;
import com.payment_process_service.outbox.model.PaymentStatusOutbox;
import com.payment_process_service.outbox.repository.PaymentStatusOutboxRepository;
import com.payment_process_service.publisher.PaymentStatusPublisher;
import com.payment_process_service.repository.PaymentRepository;
import com.payment_process_service.saga.enums.SagaState;
import com.payment_process_service.saga.model.PaymentSaga;
import com.payment_process_service.saga.repository.PaymentSagaRepository;
import com.payment_process_service.util.PacsUtil;
import com.payment_process_service.xml.Pacs008;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentProcessorService {
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorService.class);
    private final AccountClient accountClient;
    private final PaymentRepository paymentRepository;
    private final PacsUtil pacsUtil;
    private final PaymentStatusPublisher paymentStatusPublisher; // kept (legacy path)
    private final PaymentSagaRepository sagaRepository;
    private final PaymentStatusOutboxRepository outboxRepository;

    /**
     * Orchestrated saga entrypoint.
     *
     * We intentionally use DB locks (pessimistic write) on saga + payment rows to ensure
     * only one concurrent consumer can process a given paymentId at a time.
     * This prevents optimistic-lock conflicts under Kafka retries/redeliveries.
     */
    @Transactional
    public void processPayment(UUID paymentId, Pacs008 paymentXml) {
        PaymentSaga saga = lockOrCreateSaga(paymentId);
        SagaState state = saga.getState();

        // If we already finished this saga, ignore duplicates (Kafka retries / redelivery).
        if (state == SagaState.COMPLETED) {
            log.info("Saga already completed for paymentId={}", paymentId);
            return;
        }

        try {
            Payment payment = paymentRepository.findByIdForUpdate(paymentId);
            if (payment == null) throw new RuntimeException("Payment Not Found");

            if (payment.getStatus() != PaymentStatus.PROCESSING) {
                payment.setStatus(PaymentStatus.PROCESSING);
                paymentRepository.save(payment);
            }

            validatePayment(paymentXml);

            if (state == SagaState.STARTED) {
                updateLedger(paymentId, paymentXml);
                updateSagaState(paymentId, SagaState.TRANSFER_COMPLETED, null);
                state = SagaState.TRANSFER_COMPLETED;
            }

            if (state == SagaState.TRANSFER_COMPLETED) {
                // Generate pacs.002 for success
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);

                String pacs002Xml = pacsUtil.generatePacs002(payment, paymentXml, "ACSP", null);
                enqueueStatusEvent(payment.getId(), payment.getStatus(), pacs002Xml);
                updateSagaState(paymentId, SagaState.STATUS_ENQUEUED, null);
                state = SagaState.STATUS_ENQUEUED;
            }

            if (state == SagaState.STATUS_ENQUEUED) {
                updateSagaState(paymentId, SagaState.COMPLETED, null);
                state = SagaState.COMPLETED;
            }

        } catch (RuntimeException ex) {
            handleFailure(paymentId, paymentXml, ex);
        }
    }

    @Transactional
    protected PaymentSaga lockOrCreateSaga(UUID paymentId) {
        // Try locking existing saga row
        PaymentSaga existing = sagaRepository.findById(paymentId).orElse(null);
        if (existing != null) {
            return sagaRepository.findByPaymentIdForUpdate(paymentId);
        }

        // Create saga row (race-safe enough since paymentId is the PK; if two threads race, one will win)
        sagaRepository.save(PaymentSaga.builder()
                .paymentId(paymentId)
                .state(SagaState.STARTED)
                .build());

        // Lock the freshly created row
        return sagaRepository.findByPaymentIdForUpdate(paymentId);
    }

    @Transactional
    protected void updateSagaState(UUID paymentId, SagaState state, String lastError) {
        PaymentSaga saga = sagaRepository.findByPaymentIdForUpdate(paymentId);
        if (saga == null) throw new RuntimeException("Saga not found for paymentId=" + paymentId);
        saga.setState(state);
        saga.setLastError(lastError);
        sagaRepository.save(saga);
    }

    @Transactional
    protected void enqueueStatusEvent(UUID paymentId, PaymentStatus status, String pacs002Xml) {
        if (outboxRepository.existsByPaymentId(paymentId)) {
            log.info("Outbox already exists for paymentId={}, skipping enqueue", paymentId);
            return;
        }
        outboxRepository.save(PaymentStatusOutbox.builder()
                .paymentId(paymentId)
                .paymentStatus(status)
                .pacs002Xml(pacs002Xml)
                .status(OutboxStatus.PENDING)
                .build());
    }

    protected void handleFailure(UUID paymentId, Pacs008 paymentXml, RuntimeException ex) {
        String reason = ex.getMessage();
        log.warn("Payment failed paymentId={} reason={}", paymentId, reason, ex);

        Payment payment = paymentRepository.findByIdForUpdate(paymentId);
        if (payment == null) throw new RuntimeException("Payment Not Found");

        PaymentStatus failureStatus = determineFailureStatus(ex);
        payment.setStatus(failureStatus);
        paymentRepository.save(payment);

        String pacs002Xml = pacsUtil.generatePacs002(payment, paymentXml, "RJCT", reason);
        enqueueStatusEvent(payment.getId(), payment.getStatus(), pacs002Xml);
        updateSagaState(paymentId, SagaState.FAILED, reason);
    }
    public void validatePayment(Pacs008 paymentXml){
        if(paymentXml.getPmtInf().getAmt()==null || paymentXml.getPmtInf().getAmt().compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Invalid Payment Amount");
        }
        if(!accountClient.accountExists(UUID.fromString(paymentXml.getPmtInf().getDbtr()))){
            throw new RuntimeException("Sender Account Does not Exist");
        }
        if(!accountClient.accountExists(UUID.fromString(paymentXml.getPmtInf().getCdtr()))){
            throw new RuntimeException("Receiver Account Does not Exist");
        }
    }


    public  void updateLedger(UUID paymentId, Pacs008 paymentXml){
        UUID senderId = UUID.fromString(paymentXml.getPmtInf().getDbtr());
        UUID receiverId = UUID.fromString(paymentXml.getPmtInf().getCdtr());
        BigDecimal amount = paymentXml.getPmtInf().getAmt();

        TransferRequest transferRequest = TransferRequest.builder()
                .paymentId(paymentId)
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();
        try {
            accountClient.transfer(transferRequest);
        } catch (feign.FeignException fe) {
            // Wrap FeignException in RuntimeException with readable reason
            String reason = fe.contentUTF8();  // This is the message returned by Account service
            throw new RuntimeException("Account transfer failed: " + reason);
        }
    }

    private void publishStatusEvent(Payment payment, String pacs002Xml) {
        PaymentStatusEvent statusEvent = PaymentStatusEvent.builder()
                .paymentId(payment.getId())
                .paymentStatus(payment.getStatus())
                .pacs002Xml(pacs002Xml)
                .build();
        paymentStatusPublisher.publish(statusEvent);
    }
    private PaymentStatus determineFailureStatus(RuntimeException ex) {
        String msg = ex.getMessage().toLowerCase();
        if (msg.contains("insufficient") || msg.contains("account")) {
            return PaymentStatus.FAILED;
        }
        if (msg.contains("invalid") || msg.contains("validation")) {
            return PaymentStatus.VALIDATION_FAILED;
        }
        return PaymentStatus.FAILED; // default fallback
    }
}
