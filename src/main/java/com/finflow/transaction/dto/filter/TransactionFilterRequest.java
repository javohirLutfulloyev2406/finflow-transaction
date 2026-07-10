package com.finflow.transaction.dto.filter;

import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.enums.TransactionStatus;
import com.finflow.transaction.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Specification API uchun. Barcha maydonlar optional. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterRequest {

    private TransactionStatus status;
    private TransactionType type;
    private Currency currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Instant dateFrom;
    private Instant dateTo;
    private Long accountId;
}
