package com.finflow.transaction.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finflow.transaction.domain.IdempotencyRecordEntity;
import com.finflow.transaction.exception.DuplicateRequestException;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import com.finflow.transaction.repository.IdempotencyRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class IdempotencyService {

    private final IdempotencyStore redisStore;
    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public IdempotencyService(
            IdempotencyStore redisStore,
            IdempotencyRecordRepository repository,
            ObjectMapper objectMapper,
            @Value("${finflow.idempotency.ttl:PT24H}") String ttlConfig) {
        this.redisStore = redisStore;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.ttl = Duration.parse(ttlConfig);
    }

    /**
     * Redis → tez yo'l. DB → durable fallback (Redis o'chganda).
     * REQUIRES_NEW: IN_PROGRESS commit concurrent request'lar tomonidan darhol ko'rinishi uchun.
     *
     * @return empty   → yangi so'rov, operatsiyani davom ettir
     *         present → takror so'rov, JSON {"status":N,"body":"..."} — clientga qaytar
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<String> tryAcquire(Long userId, String key, String requestHash) {
        // 1. Tez yo'l: Redis
        Optional<String> redisResult = redisStore.tryAcquire(userId, key, requestHash);
        if (redisResult.isPresent()) {
            return redisResult;
        }
        // redisResult.isEmpty() = Redis lock oldi YOKI Redis o'chgan (ikkalasi uchun bir xil yo'l)

        // 2. DB tekshiruvi (Redis miss yoki Redis o'chgan)
        Optional<IdempotencyRecordEntity> existing = repository.findByUserIdAndIdempotencyKey(userId, key);
        if (existing.isPresent()) {
            return resolveDbRecord(key, requestHash, existing.get());
        }

        // 3. Yangi so'rov — DB ga yoz (unique constraint race'dan himoya qiladi)
        try {
            repository.save(IdempotencyRecordEntity.builder()
                    .userId(userId)
                    .idempotencyKey(key)
                    .requestHash(requestHash)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(ttl))
                    .build());
            return Optional.empty();

        } catch (DataIntegrityViolationException e) {

            // uq_idem_user_key buzildi: Redis o'chganda concurrent duplicate DB da ushlandi.
            // Redis mavjud bo'lsa bu hol bo'lmaydi — bu shu scenariyning YAGONA himoya qatlami.
            throw new DuplicateRequestException(key);
        }
    }

    /**
     * DB ni avval yangilaydi (REQUIRES_NEW → commit), so'ng Redis ni afterCommit'da yangilaydi.
     * afterCommit qoidasi: DB commit muvaffaqiyatsiz bo'lsa Redis'da noto'g'ri COMPLETED qolmaydi.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long userId, String key, String requestHash, int httpStatus, String responseBody) {
        repository.findByUserIdAndIdempotencyKey(userId, key).ifPresentOrElse(
                record -> {
                    record.setHttpStatus(httpStatus);
                    record.setResponseBody(responseBody);
                    // JPA dirty checking — @Transactional ichida save() shart emas
                },
                () -> log.warn("Idempotency record not found for completion. userId={}, key={}", userId, key)
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisStore.complete(userId, key, requestHash, httpStatus, responseBody);
            }
        });
    }

    private Optional<String> resolveDbRecord(String key, String requestHash, IdempotencyRecordEntity record) {
        // Redis o'chganda ham 422 ishlashi uchun DB qatlamida ham hash tekshiruvi — mustaqil himoya
        if (!requestHash.equals(record.getRequestHash())) {
            throw new ExceptionWithStatusCode(422,
                    "Idempotency-Key '" + key + "' was previously used with a different request payload");
        }

        if (record.getResponseBody() == null) {
            // DB da IN_PROGRESS: Redis o'chganda ham 409 ishlaydi
            throw new DuplicateRequestException(key);
        }

        ObjectNode cached = objectMapper.createObjectNode();
        cached.put("status", record.getHttpStatus());
        cached.put("body", record.getResponseBody());
        return Optional.of(cached.toString());
    }
}
