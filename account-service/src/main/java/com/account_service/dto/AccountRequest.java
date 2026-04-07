package com.account_service.dto;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {
    private UUID accountId;
    private BigDecimal amount;
    private UUID paymentId;
}
