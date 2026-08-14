package com.finflow.transaction.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Bir xil kalit + bir xil payload → bir xil hash → xavfsiz retry.
 * Bir xil kalit + boshqa payload → boshqa hash → 422 (client xatosi jimgina yashirilmaydi).
 */
@Component
public class RequestHasher {

    private final ObjectMapper canonicalMapper;

    public RequestHasher() {
        // App ObjectMapper'dan izolyatsiya: Jackson config o'zgarsa hash'lar o'zgarmaydi.
        // ORDER_MAP_ENTRIES_BY_KEYS: {"b":1,"a":2} va {"a":2,"b":1} bir xil hash beradi.
        canonicalMapper = new ObjectMapper()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String hash(String bodyJson) {
        String source = (bodyJson == null || bodyJson.isBlank()) ? "{}" : bodyJson;
        try {
            Object parsed = canonicalMapper.readValue(source, Object.class);
            byte[] canonical = canonicalMapper.writeValueAsBytes(parsed);
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(canonical);
            return HexFormat.of().formatHex(sha256);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot compute idempotency request hash", e);
        }
    }
}