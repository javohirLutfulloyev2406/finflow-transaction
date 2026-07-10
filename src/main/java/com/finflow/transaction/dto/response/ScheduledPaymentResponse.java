package com.finflow.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.enums.ScheduleFrequency;

import java.math.BigDecimal;
import java.time.Instant;

public record ScheduledPaymentResponse(
        Long id,
        Long sourceAccountId,
        Long targetAccountId,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal amount,

        Currency currency,
        ScheduleFrequency frequency,
        Instant nextExecutionAt,
        Instant lastExecutionAt,
        Instant endAt,
        Integer executionCount,
        boolean active,
        String description
) {
}
