package com.payment_api_service.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_api_service.dto.PaymentRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class HashUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static String generateHash(PaymentRequest paymentRequest, String idempotencyKey){
        try{
            Map<String, Object> map = new HashMap<>();
            map.put("payload", paymentRequest);
            map.put("idempotencyKey", idempotencyKey);
            String json = objectMapper.writeValueAsString(map);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for(byte b:hash){
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        }catch (Exception e){
            throw new RuntimeException("Failed to Generate Hash",e);
        }
    }

}
