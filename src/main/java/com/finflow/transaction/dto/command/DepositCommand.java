package com.finflow.transaction.dto.command;

import com.finflow.transaction.domain.vo.Money;

public record DepositCommand(
        Long userId,
        Long targetAccountId,
        Money amount,
        String description,
        String idempotencyKey,
        String deviceId,
        String ipAddress
) {
}
