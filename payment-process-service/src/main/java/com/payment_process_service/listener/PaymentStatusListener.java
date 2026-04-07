package com.payment_process_service.listener;

import com.payment_process_service.dto.PaymentInitiationEvent;
import com.payment_process_service.event.PaymentStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentStatusListener {
//    @KafkaListener(topics = "payment-status", groupId = "payment-processor-group")
//    public void consume(PaymentStatusEvent event){
//        System.out.println("Here is the Payment Status Event:"+event);
//    }
}
