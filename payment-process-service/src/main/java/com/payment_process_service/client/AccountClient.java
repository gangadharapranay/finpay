package com.payment_process_service.client;

import com.payment_process_service.dto.TransferRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "account-service", url = "${account.service.url}")
public interface AccountClient {

    @GetMapping("/api/accounts/{accountId}/exists")
    Boolean accountExists(@PathVariable("accountId") UUID accountId);

    @PostMapping("/api/accounts/transfer")
    void transfer(@RequestBody TransferRequest request);
}