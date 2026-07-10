package com.finflow.transaction.dto.request;

import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.enums.ScheduleFrequency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPaymentRequest {

    @NotNull
    private Long sourceAccountId;

    @NotNull
    private Long targetAccountId;

    @NotNull
    @DecimalMin("0.0001")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @NotNull
    private ScheduleFrequency frequency;

    @NotNull
    @Future(message = "startAt must be in the future")
    private Instant startAt;

    private Instant endAt;

    @Size(max = 255)
    private String description;
}
