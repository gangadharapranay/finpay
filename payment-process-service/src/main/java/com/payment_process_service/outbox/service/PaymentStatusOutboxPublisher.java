package com.payment_process_service.outbox.service;

import com.payment_process_service.event.PaymentStatusEvent;
import com.payment_process_service.outbox.enums.OutboxStatus;
import com.payment_process_service.outbox.model.PaymentStatusOutbox;
import com.payment_process_service.outbox.repository.PaymentStatusOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentStatusOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentStatusOutboxPublisher.class);

    private static final String TOPIC = "payment-status";

    private final PaymentStatusOutboxRepository outboxRepository;
    private final KafkaTemplate<String, PaymentStatusEvent> kafkaTemplate;

    /**
     * Outbox publisher: drains pending outbox rows and publishes them to Kafka.
     * Crash-safety: if we commit DB changes but crash before publishing to Kafka, the row stays PENDING and gets published later.
     */
    @Scheduled(fixedDelayString = "${outbox.publisher.fixedDelayMs:2000}")
    @Transactional
    public void publishPending() {
        List<PaymentStatusOutbox> batch = outboxRepository.findOldestByStatus(
                OutboxStatus.PENDING,
                PageRequest.of(0, 50)
        );
        if (batch.isEmpty()) return;

        for (PaymentStatusOutbox row : batch) {
            PaymentStatusEvent event = PaymentStatusEvent.builder()
                    .paymentId(row.getPaymentId())
                    .paymentStatus(row.getPaymentStatus())
                    .pacs002Xml(row.getPacs002Xml())
                    .build();
            kafkaTemplate.send(TOPIC, row.getPaymentId().toString(), event);

            row.setStatus(OutboxStatus.PUBLISHED);
            row.setPublishedAt(LocalDateTime.now());
            outboxRepository.save(row);

            log.info("Outbox published paymentId={} status={}", row.getPaymentId(), row.getPaymentStatus());
        }
    }
}

