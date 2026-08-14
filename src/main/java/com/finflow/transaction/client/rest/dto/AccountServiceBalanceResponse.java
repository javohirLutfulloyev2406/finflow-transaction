package com.finflow.transaction.client.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * account-service POST /api/v1/accounts/deposit/{id} va /withdraw/{id}
 * muvaffaqiyatli javobi (200 OK). Kontrakt hozircha operationId qaytarmaydi —
 * shuning uchun AccountOperationResult.operationId() to'ldirilmaydi (TODO).
 */
public record AccountServiceBalanceResponse(
        UUID accountId,
        String accountNumber,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String operationType,
        LocalDateTime timestamp
) {
}
