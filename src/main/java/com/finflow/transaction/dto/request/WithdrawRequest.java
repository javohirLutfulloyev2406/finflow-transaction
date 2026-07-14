package com.finflow.transaction.dto.request;

import com.finflow.transaction.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    @NotNull
    private UUID sourceAccountId;

    @NotNull
    @DecimalMin("0.0001")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @Size(max = 255)
    private String description;
}
