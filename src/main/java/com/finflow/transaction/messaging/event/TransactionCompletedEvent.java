package com.finflow.transaction.messaging.event;

import com.finflow.transaction.domain.vo.Money;

import com.finflow.transaction.enums.TransactionType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

/**
 * Tranzaksiya muvaffaqiyatli yakunlanganda (COMPLETED holatga o'tganda) yuboriladigan event.
 * Bu event kelganda account-service balansni allaqachon yangilagan bo'ladi —
 * shuning uchun bu yerda balansAfter emas, faqat tasdiqlangan fakt (completedAt) beriladi.
 */
@Value
@Builder
@Jacksonized
public class TransactionCompletedEvent implements TransactionEvent {

    UUID eventId;
    UUID transactionId;
    Long userId;
    Instant occurredAt;

    UUID sourceAccountId;
    UUID targetAccountId;

    Money amount;
    TransactionType transactionType;

    // Tranzaksiya necha marta retry qilinganini bilish uchun —
    // fraud/monitoring tomonida anomaliya sifatida ko'rish mumkin
    int attemptCount;

    @Override
    public String getEventType() {
        return "TRANSACTION_COMPLETED";
    }
}