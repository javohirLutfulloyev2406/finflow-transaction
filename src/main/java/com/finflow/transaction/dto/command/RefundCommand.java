package com.finflow.transaction.dto.command;

/**
 * amount YO'Q ataylab: refund har doim to'liq — original tranzaksiyaning
 * summasi service qatlamida transactionId orqali topilgan yozuvdan olinadi,
 * client tomonidan yuborilmaydi (qisman qaytarish qo'llab-quvvatlanmaydi).
 */
public record RefundCommand(
        Long userId,
        String reason,
        String idempotencyKey,
        String deviceId,
        String ipAddress
) {
}