package com.account_service.service;

import com.account_service.enums.TransactionStatus;
import com.account_service.model.Transaction;
import com.account_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional
    public void transfer(UUID paymentId, UUID senderAccountId, UUID receiverAccountId, BigDecimal amount) {
        // Idempotency: Kafka/HTTP retries must not double-debit.
        // A successful transfer always writes a CREDITED transaction entry for the paymentId.
        if (transactionRepository.existsByPaymentIdAndStatus(paymentId, TransactionStatus.CREDITED)) {
            log.info("Ignoring duplicate transfer request for paymentId={}", paymentId);
            return;
        }

        // Step 1: Debit the sender account
        accountService.debit(senderAccountId, amount, paymentId);

        // Step 2: Create ledger entry for debit
        Transaction debitTx = Transaction.builder()
                .paymentId(paymentId)
                .fromAccount(senderAccountId)
                .toAccount(receiverAccountId)
                .amount(amount)
                .status(TransactionStatus.DEBITED)
                .build();
        transactionRepository.save(debitTx);

        // Step 3: Credit the receiver account
        accountService.credit(receiverAccountId, amount, paymentId);

        // Step 4: Create ledger entry for credit
        Transaction creditTx = Transaction.builder()
                .paymentId(paymentId)
                .fromAccount(senderAccountId)
                .toAccount(receiverAccountId)
                .amount(amount)
                .status(TransactionStatus.CREDITED)
                .build();
        transactionRepository.save(creditTx);
        log.info("Transfer completed for paymentId={} fromAccount={} toAccount={} amount={}", paymentId, senderAccountId, receiverAccountId, amount);
    }
}
