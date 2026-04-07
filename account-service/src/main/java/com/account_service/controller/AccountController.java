package com.account_service.controller;

import com.account_service.dto.AccountRequest;
import com.account_service.dto.CreateAccountRequest;
import com.account_service.dto.TransferRequest;
import com.account_service.model.Account;
import com.account_service.service.AccountService;
import com.account_service.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody CreateAccountRequest request){
        Account account = accountService.create(request.getName(), request.getBalance(), request.getCurrency());
        return ResponseEntity.ok(account);
    }

    @PostMapping("/debit")
    public ResponseEntity<String> debit(@RequestBody AccountRequest request) {
        accountService.debit(request.getAccountId(), request.getAmount(), request.getPaymentId());
        return ResponseEntity.ok("Debit successful");
    }

    @PostMapping("/credit")
    public ResponseEntity<String> credit(@RequestBody AccountRequest request) {
        accountService.credit(request.getAccountId(), request.getAmount(), request.getPaymentId());
        return ResponseEntity.ok("Credit successful");
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
    @GetMapping("/{accountId}/exists")
    public ResponseEntity<Boolean> accountExists(@PathVariable UUID accountId){
        boolean exists = accountService.existsById(accountId);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        ledgerService.transfer(
                request.getPaymentId(),
                request.getSenderAccountId(),
                request.getReceiverAccountId(),
                request.getAmount()
        );
        return ResponseEntity.ok("Transfer successful");
    }
}
