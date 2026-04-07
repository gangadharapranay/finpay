package com.reconciliation_service.model;

import com.reconciliation_service.enums.SagaState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "payment_saga")
@Getter
@Setter
@NoArgsConstructor
public class PaymentSaga {

    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SagaState state;
}
