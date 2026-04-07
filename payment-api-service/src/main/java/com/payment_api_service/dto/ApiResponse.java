package com.payment_api_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiResponse<T>{
 private boolean success;
 private T data;
 private String message;
 private LocalDateTime timestamp;
 private String traceId;
}
