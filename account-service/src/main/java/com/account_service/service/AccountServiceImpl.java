package com.account_service.service;

import com.account_service.exception.InsufficientBalanceException;
import com.account_service.model.Account;
import com.account_service.model.Transaction;
import com.account_service.repository.AccountRepository;
import com.account_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;

    @Override
    public Account create(String name, BigDecimal balance, String currency) {
        Account account = Account.builder()
                .name(name)
                .balance(balance)
                .currency(currency)
                .build();
        return accountRepository.save(account);
    }

    @Override
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void debit(UUID accountId, BigDecimal amount, UUID paymentId) {
        try{
            Account account = accountRepository.findById(accountId).orElseThrow(()-> new RuntimeException("Account not Found"));
            if(account.getBalance().compareTo(amount)<0){
                throw new InsufficientBalanceException("Insufficient Balance");
            }
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);
        }catch (ObjectOptimisticLockingFailureException e){
            throw new RuntimeException("Concurrent Modification Detected, Please Retry", e);
        }
    }

    @Override
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void credit(UUID accountId, BigDecimal amount, UUID paymentId) {
        try{
            Account account = accountRepository.findById(accountId).orElseThrow(()->new RuntimeException("Account not Found"));
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);

        }catch (ObjectOptimisticLockingFailureException e){
            throw new RuntimeException("Concurreent Modification Detected, Please Retrry", e);
        }
    }

    @Override
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow(()-> new RuntimeException("Account not Found"));
    }

    @Override
    public boolean existsById(UUID accountId) {
        return accountRepository.existsById(accountId);
    }
}
