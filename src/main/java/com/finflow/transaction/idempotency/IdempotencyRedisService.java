package com.finflow.transaction.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis'ning bu yerdagi vazifasi IKKITA:
 * 1) Tez lock — bir xil (userId, key) bilan bir vaqtda kelgan ikkita so'rovdan
 *    faqat bittasi DB'ga yozishga o'tsin (SETNX).
 * 2) Tez keshlash — DB'ga qayta bormasdan tayyor javobni qaytarish.
 *
 * Redis o'chib qolsa (yoki flush bo'lsa) — DB UNIQUE constraint haqiqiy
 * himoyani beradi. Shuning uchun bu klass "tezlashtirish", "haqiqat manbai" emas.
 */
@Component
@RequiredArgsConstructor
public class IdempotencyRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "idem:lock:";
    private static final String CACHE_PREFIX = "idem:cache:";
    private static final Duration TTL = Duration.ofHours(24);

    /** true — lock olindi (birinchi so'rov). false — allaqachon band (parallel duplicate). */
    public boolean tryLock(Long userId, String idempotencyKey) {
        String key = LOCK_PREFIX + userId + ":" + idempotencyKey;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSING", TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(Long userId, String idempotencyKey) {
        redisTemplate.delete(LOCK_PREFIX + userId + ":" + idempotencyKey);
    }

    public void cacheResponse(Long userId, String idempotencyKey, String responseJson) {
        String key = CACHE_PREFIX + userId + ":" + idempotencyKey;
        redisTemplate.opsForValue().set(key, responseJson, TTL);
    }

    public String getCachedResponse(Long userId, String idempotencyKey) {
        return redisTemplate.opsForValue().get(CACHE_PREFIX + userId + ":" + idempotencyKey);
    }
}