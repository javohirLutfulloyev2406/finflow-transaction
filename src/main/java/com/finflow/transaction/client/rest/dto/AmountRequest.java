package com.finflow.transaction.client.rest.dto;

import java.math.BigDecimal;

/** account-service'ning deposit/withdraw request shakli — faqat amount, valyuta account'ning o'zidan olinadi. */
public record AmountRequest(BigDecimal amount) {
}
