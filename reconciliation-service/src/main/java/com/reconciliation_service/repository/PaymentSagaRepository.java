package com.reconciliation_service.repository;
import com.reconciliation_service.model.PaymentSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentSagaRepository extends JpaRepository<PaymentSaga, UUID> {

    // Fetch all sagas for a list of paymentIds
    List<PaymentSaga> findByPaymentIdIn(List<UUID> paymentIds);
}
