package com.account_service.enums;

public enum TransactionStatus {
    PENDING,    // Transaction started, not completed yet
    DEBITED,    // Debit successful
    CREDITED,   // Credit successful
    FAILED      // Transaction failed
}
