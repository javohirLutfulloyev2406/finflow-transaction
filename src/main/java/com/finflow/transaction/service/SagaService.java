package com.finflow.transaction.service;

import com.finflow.transaction.domain.SagaStateEntity;
import com.finflow.transaction.domain.vo.Money;

import java.util.Optional;
import java.util.UUID;

public interface SagaService {

    void start(UUID transactionId, UUID sourceAccountId, UUID targetAccountId, Money amount);

    void markDebited(UUID transactionId);

    void complete(UUID transactionId);

    void startCompensating(UUID transactionId, String reason);

    void markCompensated(UUID transactionId);

    /** Kompensatsiya HAM muvaffaqiyatsiz — eng og'ir holat, qo'lda aralashuv kerak. */
    void markCompensationFailed(UUID transactionId, String reason);

    /** DEBIT o'ziyoq muvaffaqiyatsiz — kompensatsiya kerak emas. */
    void fail(UUID transactionId, String reason);

    Optional<SagaStateEntity> find(UUID transactionId);
}