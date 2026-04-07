package com.reconciliation_service.service;

import com.reconciliation_service.model.*;
import com.reconciliation_service.repository.PaymentRepository;
import com.reconciliation_service.repository.PaymentSagaRepository;
import com.reconciliation_service.repository.PaymentStatusOutboxRepository;
import com.reconciliation_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
        import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final PaymentRepository paymentRepository;
    private final PaymentSagaRepository paymentSagaRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentStatusOutboxRepository outboxRepository;

    // main scheduled method
    @Scheduled(cron = "0 0 2 * * ?") // runs daily at 2 AM
    public void runDailyReconciliation() {
        execute(LocalDate.now().minusDays(1)); // reconcile yesterday's payments
    }

    public void execute(LocalDate runDate) {
        System.out.println("Starting reconciliation for: " + runDate);

        LocalDateTime startOfDay = runDate.atStartOfDay();
        LocalDateTime endOfDay = runDate.plusDays(1).atStartOfDay();

        // 1 Fetch payments
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        System.out.println("Total payments: " + payments.size());
        List<UUID> paymentIds = payments.stream().map(Payment::getId).toList();

        // 2 Fetch sagas
        List<PaymentSaga> sagas = paymentSagaRepository.findByPaymentIdIn(paymentIds);
        Map<UUID, PaymentSaga> sagaMap = sagas.stream()
                .collect(Collectors.toMap(PaymentSaga::getPaymentId, s -> s));

        // 3 Fetch transactions
        List<Transaction> transactions = transactionRepository.findByPaymentIdIn(paymentIds);
        Map<UUID, List<Transaction>> transactionMap = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getPaymentId));

        // 4 Fetch outbox
        List<PaymentStatusOutbox> outboxRows = outboxRepository.findByPaymentIdIn(paymentIds);
        Map<UUID, PaymentStatusOutbox> outboxMap = outboxRows.stream()
                .collect(Collectors.toMap(PaymentStatusOutbox::getPaymentId, o -> o));

        // 5 Initialize issue collector
        List<ReconciliationIssue> issues = new ArrayList<>();

        // 6 Process each payment
        for (Payment payment : payments) {
            UUID pid = payment.getId();

            // 6a️⃣ Missing saga
            PaymentSaga saga = sagaMap.get(pid);
            if (saga == null) {
                issues.add(new ReconciliationIssue(
                        pid,
                        ReconciliationIssue.IssueType.MISSING_SAGA,
                        "PaymentSaga does not exist"
                ));
            }

            // 6 Transaction check (for SUCCESS payments)
            if (payment.getStatus() != null && payment.getStatus().name().equals("SUCCESS")) {
                List<Transaction> txs = transactionMap.getOrDefault(pid, new ArrayList<>());
                long debitCount = txs.stream().filter(t -> t.getStatus().name().equals("DEBITED")).count();
                long creditCount = txs.stream().filter(t -> t.getStatus().name().equals("CREDITED")).count();

                if (debitCount != 1 || creditCount != 1) {
                    issues.add(new ReconciliationIssue(
                            pid,
                            ReconciliationIssue.IssueType.MISSING_TRANSACTION,
                            "DEBITED/CREDITED transactions mismatch"
                    ));
                }
            }

            // 6 Outbox check
            PaymentStatusOutbox outbox = outboxMap.get(pid);
            if (outbox != null && outbox.getStatus().name().equals("PENDING")) {
                issues.add(new ReconciliationIssue(
                        pid,
                        ReconciliationIssue.IssueType.OUTBOX_PENDING,
                        "PaymentStatusOutbox still pending"
                ));
            }
        }

        // 7 Log results
        System.out.println("Reconciliation completed for: " + runDate);
        System.out.println("Total issues found: " + issues.size());
        issues.forEach(issue -> System.out.println(
                "PaymentId: " + issue.getPaymentId() +
                        ", Type: " + issue.getType() +
                        ", Desc: " + issue.getDescription()
        ));
    }
}