package com.payment_api_service.service;

import com.payment_api_service.dto.PaymentRequest;
import com.payment_api_service.dto.PaymentResponse;
import com.payment_api_service.enums.PaymentStatus;
import com.payment_api_service.event.PaymentInitiationEvent;
import com.payment_api_service.exception.ResourceAlreadyExistsException;
import com.payment_api_service.exception.ResourceNotFoundException;
import com.payment_api_service.model.Payment;
import com.payment_api_service.publisher.PaymentEventPublisher;
import com.payment_api_service.repository.PaymentRepository;
import com.payment_api_service.util.HashUtil;
import com.payment_api_service.xml.Pacs002;
import com.payment_api_service.xml.Pacs008;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest){
        String requestHash = HashUtil.generateHash(paymentRequest, paymentRequest.getIdempotencyKey());
        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey());
        //Check if IdempotencyKey already exists
        if(existingPayment.isPresent()){
            Payment existing = existingPayment.get();

            //Compare hash Instead of Fields
            if(!existing.getRequestHash().equals(requestHash)){
                throw new ResourceAlreadyExistsException("Payment already exists with idempotency key: "+paymentRequest.getIdempotencyKey());
            }
            //True Idempotent Retry
            return mapToResponse(existing);
        }

        Payment payment = Payment.builder()
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .senderAccountId(paymentRequest.getSenderAccountId())
                .receiverAccountId(paymentRequest.getReceiverAccountId())
                .idempotencyKey(paymentRequest.getIdempotencyKey())
                .status(PaymentStatus.INITIATED)
                .requestHash(requestHash)
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        // Generate pacs.008 XML
        String pacs008Xml = generatePacs008Xml(paymentRequest);

        PaymentInitiationEvent event = PaymentInitiationEvent.builder()
                .paymentId(savedPayment.getId())
                .pacs008Xml(pacs008Xml)
                .build();
        paymentEventPublisher.publish(event);
        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id){
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Payment Not Found with Id : "+id));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment){
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .senderAccountId(payment.getSenderAccountId())
                .receiverAccountId(payment.getReceiverAccountId())
                .status(payment.getStatus())
                .idempotencyKey(payment.getIdempotencyKey())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }



    public String generatePacs008Xml(PaymentRequest request) {
        try {
            Pacs008 pacs008 = Pacs008.builder()
                    .grpHdr(Pacs008.GroupHeader.builder()
                            .msgId(request.getIdempotencyKey())
                            .creDtTm(LocalDateTime.now())
                            .nbOfTxs(1)
                            .build())
                    .pmtInf(Pacs008.PaymentInfo.builder()
                            .pmtInfId(request.getIdempotencyKey())
                            .instrId(request.getIdempotencyKey())
                            .amt(request.getAmount())
                            .ccy(request.getCurrency())
                            .dbtr(request.getSenderAccountId().toString())
                            .cdtr(request.getReceiverAccountId().toString())
                            .build())
                    .build();
            JAXBContext context = JAXBContext.newInstance(Pacs008.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            marshaller.marshal(pacs008, sw);
            return sw.toString();
        } catch (JAXBException e) {
            log.error("Failed to generate pacs.008 for idempotencyKey={}", request.getIdempotencyKey(), e);
            throw new RuntimeException("Failed to generate pacs.008 XML", e);
        }
    }


    public void updatePaymentStatus(UUID paymentId, Pacs002 pacs002) {
        String txStatus = pacs002.getOriginalPaymentInfo().getTxSts();
        String reason = pacs002.getOriginalPaymentInfo().getReason();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        PaymentStatus mappedStatus = mapIsoStatus(txStatus);
        payment.setStatus(mappedStatus);
        payment.setReason(reason);
        paymentRepository.save(payment);
        log.info("Updated paymentId={} status={} reason={}", paymentId, mappedStatus, reason);
    }

    private PaymentStatus mapIsoStatus(String txStatus) {
        return switch (txStatus) {
            case "ACSP" -> PaymentStatus.SUCCESS;    // Accepted for settlement
            case "ACSC" -> PaymentStatus.SUCCESS;
            case "PDNG" -> PaymentStatus.PROCESSING;     // Pending
            case "RJCT" -> PaymentStatus.FAILED;         // Rejected
            default -> PaymentStatus.FAILED;
        };
    }
}
