package com.finflow.transaction.client.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** account-service GET /api/v1/accounts/{id} javobi — balans shu javobning bir maydoni sifatida keladi. */
public record AccountServiceAccountResponse(
        UUID id,
        String accountNumber,
        String currency,
        BigDecimal balance,
        String status,
        boolean isPrimary
) {
}
