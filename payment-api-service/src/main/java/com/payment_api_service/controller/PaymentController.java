package com.payment_api_service.controller;

import com.payment_api_service.dto.ApiResponse;
import com.payment_api_service.dto.PaymentRequest;
import com.payment_api_service.dto.PaymentResponse;
import com.payment_api_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,@Validated @RequestBody PaymentRequest paymentRequest){
        paymentRequest.setIdempotencyKey(idempotencyKey);
        PaymentResponse paymentResponse  = paymentService.createPayment(paymentRequest);
        ApiResponse<PaymentResponse> response = ApiResponse.<PaymentResponse>builder()
                .success(true)
                .data(paymentResponse)
                .message("Payment created successfully")
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id){
        PaymentResponse paymentResponse = paymentService.getPayment(id);
        ApiResponse<PaymentResponse> response = ApiResponse.<PaymentResponse>builder()
                .success(true)
                .data(paymentResponse)
                .message("Payment created successfully")
                .timestamp(LocalDateTime.now())
                .traceId(UUID.randomUUID().toString())
                .build();
        return ResponseEntity.ok().body(response);
    }
}
