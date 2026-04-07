package com.payment_process_service.saga.repository;

import com.payment_process_service.saga.model.PaymentSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PaymentSagaRepository extends JpaRepository<PaymentSaga, UUID> {

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PaymentSaga s where s.paymentId = :paymentId")
    PaymentSaga findByPaymentIdForUpdate(@Param("paymentId") UUID paymentId);
}

