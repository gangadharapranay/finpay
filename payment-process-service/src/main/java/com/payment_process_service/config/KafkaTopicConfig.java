package com.payment_process_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic createPaymentStatusTopic(){
        return new NewTopic("payment-status", 1, (short)1);
    }
}

