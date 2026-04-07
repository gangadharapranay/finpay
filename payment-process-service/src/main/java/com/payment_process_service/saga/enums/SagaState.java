package com.payment_process_service.saga.enums;

public enum SagaState {
    STARTED,
    TRANSFER_COMPLETED,
    STATUS_ENQUEUED,
    COMPLETED,
    FAILED
}

