package com.payment_process_service.dto;

import com.payment_process_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationEvent {
    private UUID paymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String pacs008Xml;
}
