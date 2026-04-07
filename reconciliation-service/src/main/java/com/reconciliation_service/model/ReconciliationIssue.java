package com.reconciliation_service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class ReconciliationIssue {

    private UUID paymentId;

    private IssueType type;

    private String description;

    public ReconciliationIssue(UUID paymentId, IssueType type, String description) {
        this.paymentId = paymentId;
        this.type = type;
        this.description = description;
    }

    public enum IssueType {
        MISSING_SAGA,
        STATUS_MISMATCH,
        MISSING_TRANSACTION,
        DUPLICATE_TRANSACTION,
        OUTBOX_PENDING
    }
}
