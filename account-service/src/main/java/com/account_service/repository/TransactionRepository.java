package com.account_service.repository;

import com.account_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    boolean existsByPaymentIdAndStatus(UUID paymentId, com.account_service.enums.TransactionStatus status);
}