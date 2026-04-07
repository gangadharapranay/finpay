package com.payment_process_service.outbox.repository;

import com.payment_process_service.outbox.enums.OutboxStatus;
import com.payment_process_service.outbox.model.PaymentStatusOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PaymentStatusOutboxRepository extends JpaRepository<PaymentStatusOutbox, UUID> {

    boolean existsByPaymentId(UUID paymentId);

    @Query("select o from PaymentStatusOutbox o where o.status = :status order by o.createdAt asc")
    List<PaymentStatusOutbox> findOldestByStatus(OutboxStatus status, Pageable pageable);
}

