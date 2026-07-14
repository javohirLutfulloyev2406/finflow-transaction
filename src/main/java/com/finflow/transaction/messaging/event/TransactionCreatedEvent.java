package com.finflow.transaction.messaging.event;

import com.finflow.transaction.domain.vo.Money;

import com.finflow.transaction.enums.TransactionType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

/**
 * Tranzaksiya yaratilganda (PENDING holatga o'tganda) yuboriladigan event.
 * Outbox jadvaliga yoziladi va keyin Kafka producer orqali "transaction.events" topic'iga chiqadi.
 */
@Value
@Builder
@Jacksonized
public class TransactionCreatedEvent implements TransactionEvent {

    UUID eventId;
    UUID transactionId;
    Long userId;
    Instant occurredAt;

    UUID sourceAccountId;
    UUID targetAccountId;   // TRANSFER bo'lmasa null (deposit/withdraw holatlarida)

    Money amount;
    TransactionType transactionType;
    String idempotencyKey;

    @Override
    public String getEventType() {
        return "TRANSACTION_CREATED";
    }
}