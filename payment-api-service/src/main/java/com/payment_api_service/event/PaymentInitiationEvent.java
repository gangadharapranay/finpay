package com.payment_api_service.event;

import com.payment_api_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInitiationEvent {
    private UUID paymentId;
    private String pacs008Xml;
}
