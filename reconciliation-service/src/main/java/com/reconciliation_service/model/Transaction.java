package com.reconciliation_service.model;

import com.reconciliation_service.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    private UUID id;

    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
