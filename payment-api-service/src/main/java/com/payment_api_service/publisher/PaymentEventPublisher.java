package com.payment_api_service.publisher;

import com.payment_api_service.event.PaymentInitiationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final KafkaTemplate<String, PaymentInitiationEvent> kafkaTemplate;
    private static final String TOPIC = "payment-initiation";
    public void publish(PaymentInitiationEvent event){
        kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
    }
}
