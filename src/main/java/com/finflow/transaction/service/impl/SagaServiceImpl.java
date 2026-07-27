package com.finflow.transaction.service.impl;

import com.finflow.transaction.domain.SagaStateEntity;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.enums.SagaStatus;
import com.finflow.transaction.enums.SagaStep;
import com.finflow.transaction.repository.SagaStateRepository;
import com.finflow.transaction.service.SagaService;
import com.finflow.transaction.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaServiceImpl implements SagaService {

    private final SagaStateRepository repository;
    private final JsonUtil jsonUtil;

    @Override
    public void start(UUID transactionId, UUID sourceAccountId, UUID targetAccountId, Money amount) {
        SagaStateEntity saga = SagaStateEntity.builder()
                .transactionId(transactionId)
                .status(SagaStatus.STARTED)
                .currentStep(SagaStep.DEBIT_SOURCE)
                // Denormalized: recovery job TransactionEntity'ga qo'shilmasdan
                // (join) ham qaysi account'lar/summa ekanligini bilishi uchun.
                .payload(jsonUtil.toJson(new SagaPayload(sourceAccountId, targetAccountId, amount)))
                .startedAt(Instant.now())
                .build();
        repository.save(saga);
    }

    @Override
    public void markDebited(UUID transactionId) {
        SagaStateEntity saga = getOrThrow(transactionId);
        saga.setStatus(SagaStatus.DEBITED);
        saga.setCurrentStep(SagaStep.CREDIT_TARGET);
        saga.setUpdatedAt(Instant.now());
    }

    @Override
    public void complete(UUID transactionId) {
        SagaStateEntity saga = getOrThrow(transactionId);
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setCurrentStep(SagaStep.FINALIZE);
        saga.setUpdatedAt(Instant.now());
    }

    @Override
    public void startCompensating(UUID transactionId, String reason) {
        SagaStateEntity saga = getOrThrow(transactionId);
        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setCurrentStep(SagaStep.COMPENSATE_REFUND);
        saga.setLastError(reason);
        saga.setUpdatedAt(Instant.now());
    }

    @Override
    public void markCompensated(UUID transactionId) {
        SagaStateEntity saga = getOrThrow(transactionId);
        saga.setStatus(SagaStatus.COMPENSATED);
        saga.setUpdatedAt(Instant.now());
    }

    @Override
    public void markCompensationFailed(UUID transactionId, String reason) {
        SagaStateEntity saga = getOrThrow(transactionId);
        saga.setStatus(SagaStatus.FAILED);
        saga.setRetryCount(saga.getRetryCount() + 1);
        saga.setLastError(reason);
        saga.setUpdatedAt(Instant.now());
        log.error("SAGA COMPENSATION FAILED — pul osilib qolgan bo'lishi mumkin, " +
                "qo'lda aralashuv kerak. transactionId={}, reason={}", transactionId, reason);
    }

    @Override
    public void fail(UUID transactionId, String reason) {
        SagaStateEntity saga = getOrThrow(transactionId);
        saga.setStatus(SagaStatus.FAILED);
        saga.setLastError(reason);
        saga.setUpdatedAt(Instant.now());
    }

    @Override
    public Optional<SagaStateEntity> find(UUID transactionId) {
        return repository.findByTransactionId(transactionId);
    }

    private SagaStateEntity getOrThrow(UUID transactionId) {
        return repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalStateException(
                        "Saga state not found for transaction: " + transactionId));
    }

    private record SagaPayload(UUID sourceAccountId, UUID targetAccountId, Money amount) {
    }
}