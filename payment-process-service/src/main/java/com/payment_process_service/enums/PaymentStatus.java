package com.payment_process_service.enums;

public enum PaymentStatus {

    INITIATED,          // payment request received
    VALIDATION_FAILED,  // account/balance validation failed
    PROCESSING,         // processor handling payment
    SUCCESS,            // payment completed
    FAILED,             // processing failed
    REVERSED            // payment reversed/refunded
}