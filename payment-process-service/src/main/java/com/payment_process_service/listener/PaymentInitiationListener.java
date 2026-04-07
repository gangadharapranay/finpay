package com.payment_process_service.listener;

import com.payment_process_service.dto.PaymentInitiationEvent;
import com.payment_process_service.enums.PaymentStatus;
import com.payment_process_service.event.PaymentStatusEvent;
import com.payment_process_service.model.Payment;
import com.payment_process_service.publisher.PaymentStatusPublisher;
import com.payment_process_service.repository.PaymentRepository;
import com.payment_process_service.service.PaymentProcessorService;
import com.payment_process_service.util.PacsUtil;
import com.payment_process_service.xml.Pacs008;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;

@Service
@RequiredArgsConstructor
public class PaymentInitiationListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentInitiationListener.class);
    private final PaymentProcessorService paymentProcessorService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2), //2s->4s->8s
            dltTopicSuffix = ".dlq"
    )
    @KafkaListener(topics = "payment-initiation", groupId = "payment-processor-group")
    public void consume(PaymentInitiationEvent event){
        log.info("Consumed payment-initiation paymentId={}", event.getPaymentId());
        Pacs008 xml = parsePacs008(event.getPacs008Xml());

        paymentProcessorService.processPayment(event.getPaymentId(), xml);
    }

    public Pacs008 parsePacs008(String xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(Pacs008.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (Pacs008) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to parse pacs.008 XML", e);
        }
    }


}
