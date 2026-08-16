package com.finflow.transaction.client.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.function.Supplier;

/**
 * FAQAT READ (idempotent, side-effect'siz) chaqiruvlar uchun — masalan getBalance(),
 * getUserStatus(). debit()/credit()da ISHLATILMAYDI: agar birinchi so'rov account-service
 * tomonida muvaffaqiyatli bajarilib, faqat javob tarmoqda yo'qolgan bo'lsa, qayta yuborish
 * ikkinchi marta pul ko'chiradi — account-service hozircha so'rov darajasida idempotency
 * key qabul qilmaydi (CLAUDE.md §7, "Xato kontrakti"). Bank loyihasida bu qabul qilinmaydi,
 * shuning uchun yozish operatsiyalari uchun retry ataylab yo'q — ular saga/compensation
 * orqali (TransactionServiceImpl) domen darajasida qayta ishlanadi.
 */
@Slf4j
final class RetrySupport {

    private RetrySupport() {
    }

    static <T> T withRetry(String operation, int maxAttempts, long backoffMs, Supplier<T> action) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (ResourceAccessException | HttpServerErrorException e) {
                lastError = e;
                log.warn("{}: {}/{}-urinish muvaffaqiyatsiz — {}", operation, attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    sleep(backoffMs * attempt);
                }
            }
        }
        throw lastError;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
