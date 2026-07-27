package com.finflow.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.enums.EntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEntryResponse(
        Long id,
        UUID accountId,
        EntryType entryType,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal amount,

        Currency currency,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal balanceAfter,

        Instant bookedAt
) {
}
