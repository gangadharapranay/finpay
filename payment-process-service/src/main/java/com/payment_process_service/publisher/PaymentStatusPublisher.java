package com.payment_process_service.publisher;

import com.payment_process_service.event.PaymentStatusEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentStatusPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentStatusPublisher.class);
    private final KafkaTemplate<String, PaymentStatusEvent> kafkaTemplate;
    private static final String TOPIC = "payment-status";
    public void publish(PaymentStatusEvent event){
        kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
        log.info("Published payment-status paymentId={} status={}", event.getPaymentId(), event.getPaymentStatus());
    }
}

