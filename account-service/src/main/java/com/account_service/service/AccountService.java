package com.account_service.service;

import com.account_service.model.Account;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountService {
    Account create(String name, BigDecimal balance, String currency);
    void debit(UUID accountId, BigDecimal amount, UUID paymentId);
    void credit(UUID accountId, BigDecimal amount, UUID paymentId);
    Account getAccount(UUID accountId);
    boolean existsById(UUID accountId);
}
