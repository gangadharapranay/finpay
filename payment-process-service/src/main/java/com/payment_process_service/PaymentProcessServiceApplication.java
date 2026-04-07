package com.payment_process_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.payment_process_service.client")
@EnableScheduling
public class PaymentProcessServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentProcessServiceApplication.class, args);
	}

}
