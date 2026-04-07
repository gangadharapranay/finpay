package com.payment_process_service.outbox.model;

import com.payment_process_service.enums.PaymentStatus;
import com.payment_process_service.outbox.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payment_status_outbox",
        indexes = {
                @Index(name = "idx_outbox_status_created", columnList = "status,created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_outbox_payment", columnNames = {"payment_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Lob
    @Column(name = "pacs002_xml", nullable = false)
    private String pacs002Xml;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

