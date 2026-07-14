package com.finflow.transaction.util;

import com.finflow.transaction.exception.ExceptionWithStatusCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 hash generator.
 *
 * Idempotency uchun request payload'dan deterministic hash olinadi.
 * Bir xil payload -> har doim bir xil hash.
 */
@Component
public class HashGenerator {

    private static final String ALGORITHM = "SHA-256";

    public String sha256(String value) {
        if (value == null) {
            throw new ExceptionWithStatusCode(400, "Value must not be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}