package com.finflow.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.enums.TransactionStatus;
import com.finflow.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * amount STRING sifatida serialize qilinadi (@JsonFormat).
 * Sabab: JavaScript Number 2^53 dan katta/aniq qiymatlarni yo'qotadi.
 * 12345678901234.5678 -> 12345678901234.568. Bank API'sida qabul qilinmaydi.
 */
public record TransactionResponse(
        UUID id,
        String reference,
        TransactionType type,
        TransactionStatus status,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal amount,

        Currency currency,
        Long sourceAccountId,
        Long targetAccountId,
        String description,
        String failureReason,
        Instant initiatedAt,
        Instant completedAt,
        Instant createdAt
) {
}
