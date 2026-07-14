package com.finflow.transaction.service;

import com.finflow.transaction.domain.IdempotencyRecordEntity;

import java.util.Optional;

public interface IdempotencyService {

    Optional<IdempotencyRecordEntity> find(
            Long userId,
            String idempotencyKey
    );

    void save(
            Long userId,
            String idempotencyKey,
            String requestHash,
            String responseBody,
            Integer httpStatus
    );

}