package com.payment_api_service.listener;

import com.payment_api_service.event.PaymentStatusEvent;
import com.payment_api_service.model.Payment;
import com.payment_api_service.repository.PaymentRepository;
import com.payment_api_service.service.PaymentService;
import com.payment_api_service.xml.Pacs002;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.io.StringReader;

@Service
@RequiredArgsConstructor
public class PaymentStatusListener {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2), //2s->4s->8s
            dltTopicSuffix = ".dlq"
    )
    @KafkaListener(
            topics = "payment-status",
            groupId = "payment-api-group"
    )
    public void consume(PaymentStatusEvent event) {
        Pacs002 pacs002 = parsePacs002(event.getPacs002Xml());
        paymentService.updatePaymentStatus(event.getPaymentId(), pacs002);
    }

    public Pacs002 parsePacs002(String xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(Pacs002.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (Pacs002) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to parse pacs.002 XML", e);
        }
    }
}
