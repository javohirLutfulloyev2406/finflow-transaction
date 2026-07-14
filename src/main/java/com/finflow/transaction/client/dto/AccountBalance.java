package com.finflow.transaction.client.dto;

import com.finflow.transaction.domain.vo.Money;

public record AccountBalance(

        Long accountId,

        Money balance
) {
}