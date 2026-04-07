package com.reconciliation_service.model;

import com.reconciliation_service.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "payment_status_outbox")
@Getter
@Setter
@NoArgsConstructor
public class PaymentStatusOutbox {

    @Id
    private UUID id;

    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
}
