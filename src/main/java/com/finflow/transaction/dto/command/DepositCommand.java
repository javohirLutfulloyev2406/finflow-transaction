package com.finflow.transaction.dto.command;

import com.finflow.transaction.domain.vo.Money;

import java.util.UUID;

public record DepositCommand(
        Long userId,
        UUID targetAccountId,
        Money amount,
        String description,
        String idempotencyKey,
        String deviceId,
        String ipAddress
) {
}