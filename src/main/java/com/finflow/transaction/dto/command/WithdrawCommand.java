package com.finflow.transaction.dto.command;

import com.finflow.transaction.domain.vo.Money;

public record WithdrawCommand(
        Long userId,
        Long sourceAccountId,
        Money amount,
        String description,
        String idempotencyKey,
        String deviceId,
        String ipAddress
) {
}
