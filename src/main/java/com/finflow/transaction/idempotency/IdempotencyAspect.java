package com.finflow.transaction.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.transaction.domain.IdempotencyRecordEntity;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import com.finflow.transaction.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

/**
 * Ikki qatlamli idempotency: avval DB (haqiqat manbai) tekshiriladi,
 * keyin Redis — parallel duplicate so'rovlarni bloklash uchun lock sifatida.
 *
 * Nega DB avval? Chunki agar Redis TTL tugab ketgan bo'lsa-yu, so'rov haqiqatan
 * ilgari muvaffaqiyatli bajarilgan bo'lsa, faqat DB shuni biladi. Redis
 * yo'qolgan bo'lishi mumkin — DB har doim ishonchli.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyRecordRepository repository;
    private final IdempotencyRedisService redisService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.finflow.transaction.idempotency.Idempotent)")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
        IdempotentCommand command = extractCommand(joinPoint);
        Long userId = command.userId();
        String idempotencyKey = command.idempotencyKey();
        String requestHash = hash(objectMapper.writeValueAsString(command));

        // 1) DB — haqiqat manbai
        Optional<IdempotencyRecordEntity> existing =
                repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);

        if (existing.isPresent()) {
            return handleExisting(existing.get(), requestHash, joinPoint);
        }

        // 2) Redis lock — parallel duplicate'larni bloklash
        if (!redisService.tryLock(userId, idempotencyKey)) {
            throw new ExceptionWithStatusCode(409,
                    "Bu so'rov allaqachon qayta ishlanmoqda. Biroz kuting va qayta urinib ko'ring.");
        }

        try {
            Object result = joinPoint.proceed();
            persist(userId, idempotencyKey, requestHash, result);
            redisService.cacheResponse(userId, idempotencyKey, objectMapper.writeValueAsString(result));
            return result;
        } catch (DataIntegrityViolationException e) {
            // Race condition: Redis lock o'tgan bo'lsa-da, boshqa instance
            // xuddi shu paytda DB'ga yozib ulgurdi (masalan Redis flush bo'lgan holatda).
            // UNIQUE(user_id, idempotency_key) buni tutadi — endi DB'dan haqiqiy natijani o'qiymiz.
            log.warn("Idempotency race condition detected for user={}, key={}", userId, idempotencyKey);
            IdempotencyRecordEntity record = repository
                    .findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                    .orElseThrow(() -> e);
            return handleExisting(record, requestHash, joinPoint);
        } finally {
            redisService.releaseLock(userId, idempotencyKey);
        }
    }

    private Object handleExisting(IdempotencyRecordEntity record, String requestHash,
                                  ProceedingJoinPoint joinPoint) throws Exception {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new ExceptionWithStatusCode(422,
                    "Idempotency-Key allaqachon boshqa so'rov tanasi bilan ishlatilgan.");
        }

        Class<?> returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType();
        return objectMapper.readValue(record.getResponseBody(), returnType);
    }

    private void persist(Long userId, String idempotencyKey, String requestHash, Object result) {
        try {
            IdempotencyRecordEntity record = IdempotencyRecordEntity.builder()
                    .userId(userId)
                    .idempotencyKey(idempotencyKey)
                    .requestHash(requestHash)
                    .responseBody(objectMapper.writeValueAsString(result))
                    .httpStatus(200)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                    .build();
            repository.save(record);
        } catch (Exception e) {
            throw new IllegalStateException("Idempotency record saqlashda xato", e);
        }
    }

    private IdempotentCommand extractCommand(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(IdempotentCommand.class::isInstance)
                .map(IdempotentCommand.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "@Idempotent metod argumentlaridan birontasi IdempotentCommand emas"));
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Hash hisoblashda xato", e);
        }
    }
}