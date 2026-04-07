package com.payment_api_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic createPaymentInitiationTopic(){
        return new NewTopic("payment-initiation", 1, (short)1);
    }
}
