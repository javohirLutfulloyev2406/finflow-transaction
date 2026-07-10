package com.finflow.transaction.dto.request;

import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.validation.DifferentAccounts;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DifferentAccounts
public class TransferRequest {

    @NotNull(message = "sourceAccountId is required")
    private Long sourceAccountId;

    @NotNull(message = "targetAccountId is required")
    private Long targetAccountId;

    /** Manfiy yoki nol summa DTO darajasidayoq to'xtatiladi. */
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0001", message = "amount must be positive")
    @Digits(integer = 15, fraction = 4, message = "amount must fit NUMERIC(19,4)")
    private BigDecimal amount;

    @NotNull(message = "currency is required")
    private Currency currency;

    @Size(max = 255)
    private String description;
}
