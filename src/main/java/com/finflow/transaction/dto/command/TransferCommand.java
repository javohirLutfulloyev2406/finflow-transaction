package com.finflow.transaction.dto.command;

import com.finflow.transaction.domain.vo.Money;

import java.util.UUID;

/**
 * Service qatlamining internal modeli.
 *
 * Nega alohida? Controller DTO'si HTTP dunyosiga tegishli (validation annotatsiyalari,
 * JSON nomlari). Agar u service'ga kirsa, service HTTP'ga bog'lanib qoladi va
 * xuddi shu metodni Quartz job yoki Kafka consumer'dan chaqirib bo'lmaydi.
 * ScheduledPaymentJob aynan shu TransferCommand'ni yasab, xuddi shu service'ni chaqiradi.
 */
public record TransferCommand(
        Long userId,
        UUID sourceAccountId,
        UUID targetAccountId,
        Money amount,
        String description,
        String idempotencyKey,
        String deviceId,
        String ipAddress
) {
}