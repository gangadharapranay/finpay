package com.reconciliation_service.repository;
import com.reconciliation_service.model.PaymentStatusOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentStatusOutboxRepository extends JpaRepository<PaymentStatusOutbox, UUID> {

    // Fetch all outbox rows for a list of paymentIds
    List<PaymentStatusOutbox> findByPaymentIdIn(List<UUID> paymentIds);
}