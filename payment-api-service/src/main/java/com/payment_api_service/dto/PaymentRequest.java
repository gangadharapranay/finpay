package com.payment_api_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    @NotNull(message = "Amount is Required")
    @DecimalMin(value="0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is Required")
    @Size(min = 3, max = 3, message = "Currency must be 3-letter code")
    private String currency;

    @NotNull(message = "Sender account is required")
    private UUID senderAccountId;

    @NotNull(message = "Receiver account is required")
    private UUID receiverAccountId;

    @JsonIgnore
    private String idempotencyKey;
}
