package com.finflow.transaction.dto.command;

import com.finflow.transaction.domain.vo.Money;

public record RefundCommand(
        Long userId,
        Money amount,
        String reason,
        String idempotencyKey,
        String deviceId,
        String ipAddress
) {
}