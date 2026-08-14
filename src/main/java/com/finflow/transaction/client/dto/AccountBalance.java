package com.finflow.transaction.client.dto;

import com.finflow.transaction.domain.vo.Money;

import java.util.UUID;

public record AccountBalance(

        UUID accountId,

        Money balance
) {
}