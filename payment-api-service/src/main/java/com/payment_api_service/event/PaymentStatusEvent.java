package com.payment_api_service.event;

import com.payment_api_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEvent {
    private UUID paymentId;
    private PaymentStatus paymentStatus;
    private String pacs002Xml;
}
