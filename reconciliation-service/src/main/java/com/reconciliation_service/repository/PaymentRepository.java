package com.reconciliation_service.repository;

import com.reconciliation_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Fetch payments created between start and end
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
