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

    /** Controller SecurityUtil orqali qo'yadi — client'dan qabul qilinmaydi (o'zganing tranzaksiyasini so'ramasin). */
    private Long userId;

    private TransactionStatus status;
    private TransactionType type;
    private Currency currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Instant dateFrom;
    private Instant dateTo;
    private Long accountId;
}
