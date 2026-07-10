package com.finflow.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finflow.transaction.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceResponse(
        Long accountId,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal balance,

        Currency currency,
        Instant asOf
) {
}
