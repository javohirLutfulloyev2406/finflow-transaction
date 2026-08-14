package com.finflow.transaction.idempotency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finflow.transaction.exception.DuplicateRequestException;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Birinchi qatlam — tez yo'l. Redis o'chsa IdempotencyService DB qatlamiga tushadi.
 *
 * Saqlash formati:
 *   IN_PROGRESS  →  "IN_PROGRESS:{requestHash}"             (plain string, lock-ttl bilan)
 *   COMPLETED    →  {"hash":"...","status":N,"body":"..."}  (JSON, ttl bilan)
 */
@Slf4j
@Component
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String IN_PROGRESS_PREFIX = "IN_PROGRESS:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;
    // IN_PROGRESS uchun qisqa TTL: service crash bo'lsa marker shu vaqtda o'z-o'zidan tozalanadi.
    // 24h bilan qolsa, o'sha kalit 24 soat bloklanadi — bank serviceida qabul qilinmaydi.
    private final Duration lockTtl;

    public RedisIdempotencyStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${finflow.idempotency.ttl:PT24H}") String ttlConfig,
            @Value("${finflow.idempotency.lock-ttl:PT1M}") String lockTtlConfig) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.parse(ttlConfig);
        this.lockTtl = Duration.parse(lockTtlConfig);
    }

    @Override
    public Optional<String> tryAcquire(Long userId, String key, String requestHash) {
        String redisKey = buildKey(userId, key);

        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, IN_PROGRESS_PREFIX + requestHash, lockTtl);

            if (Boolean.TRUE.equals(acquired)) {
                return Optional.empty();
            }

            String existing = redisTemplate.opsForValue().get(redisKey);
            if (existing == null) {
                // lockTtl tugadi setIfAbsent va get orasida — yangi so'rov kabi davom etsin
                return Optional.empty();
            }

            return resolveExisting(key, requestHash, existing);

        } catch (DataAccessException e) {
            // Redis o'chgan — IdempotencyService DB qatlamini ishlatadi
            log.warn("Redis unavailable for idempotency check, falling back to DB. userId={}", userId);
            return Optional.empty();
        }
    }

    @Override
    public void complete(Long userId, String key, String requestHash, int httpStatus, String responseBody) {
        String redisKey = buildKey(userId, key);

        try {
            // COMPLETED har doim to'liq ttl oladi: "qancha vaqt o'tdi" emas, "javob qachon eskiradi" muhim.
            // getExpire() olib tashlandi — lockTtl (1min) dan qolgan vaqt ishlatilsa, javob juda tez eskirardi.
            redisTemplate.opsForValue().set(redisKey, buildCompletedJson(requestHash, httpStatus, responseBody), ttl);

        } catch (DataAccessException e) {
            // Best-effort: DB allaqachon yozdi. Redis muvaffaqiyatsizligi — fatal emas.
            log.warn("Redis unavailable when completing idempotency record. userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to store idempotency response in Redis. userId={}", userId, e);
        }
    }

    // "idem:{userId}:{key}"
    // "idem:" prefix  — fraud rate-limiter va session key'lar bilan namespace to'qnashuvi yo'q.
    // userId qo'shilishi — ikki user bir xil Idempotency-Key yuborganda bir-birini bloklamasin.
    private String buildKey(Long userId, String key) {
        return "idem:" + userId + ":" + key;
    }

    private String buildCompletedJson(String hash, int status, String body) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("hash", hash);
        node.put("status", status);
        node.put("body", body);
        return node.toString();
    }

    private Optional<String> resolveExisting(String key, String requestHash, String existing) {
        if (existing.startsWith(IN_PROGRESS_PREFIX)) {
            String storedHash = existing.substring(IN_PROGRESS_PREFIX.length());
            assertHashMatches(key, requestHash, storedHash);

            // Hash mos, lekin birinchi so'rov hali tugamagan.
            // 409 bo'lmasa ikkinchi parallel so'rov ham operatsiyani ishga tushiradi — pul ikki marta ketadi.
            throw new DuplicateRequestException(key);
        }

        try {
            JsonNode node = objectMapper.readTree(existing);
            assertHashMatches(key, requestHash, node.get("hash").asText());

            ObjectNode cached = objectMapper.createObjectNode();
            cached.put("status", node.get("status").asInt());
            cached.put("body", node.get("body").asText());
            return Optional.of(cached.toString());

        } catch (Exception e) {
            // Buzilgan Redis qiymati — DB qatlamiga o'tkazilsin
            log.warn("Corrupted Redis idempotency value for key={}, treating as cache miss", key);
            return Optional.empty();
        }
    }

    private void assertHashMatches(String key, String incoming, String stored) {
        if (!incoming.equals(stored)) {
            throw new ExceptionWithStatusCode(422,
                    "Idempotency-Key '" + key + "' was previously used with a different request payload");
        }
    }
}
