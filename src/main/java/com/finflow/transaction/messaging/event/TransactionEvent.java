package com.finflow.transaction.messaging.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Barcha transaction domain event'lari uchun umumiy kontrakt.
 * Outbox'ga yozilganda va Kafka orqali consumer'ga yetganda
 * shu interfeys polymorphic (de)serialization uchun ishlatiladi.
 *
 * MUHIM: @JsonTypeInfo "eventType" propertysi orqali ishlaydi (class nomi orqali emas!),
 * chunki consumer tomonida (account-service) boshqa package struktura bo'lishi mumkin.
 * Shu sabab CLASS emas, NAME strategiyasi tanlandi — servicelar orasidagi contract'ni
 * class nomiga bog'lab qo'ymaslik uchun.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TransactionCreatedEvent.class, name = "TRANSACTION_CREATED"),
        @JsonSubTypes.Type(value = TransactionCompletedEvent.class, name = "TRANSACTION_COMPLETED"),
        @JsonSubTypes.Type(value = TransactionFailedEvent.class, name = "TRANSACTION_FAILED"),
        @JsonSubTypes.Type(value = TransactionReversedEvent.class, name = "TRANSACTION_REVERSED")
})
public sealed interface TransactionEvent
        permits TransactionCreatedEvent,
        TransactionCompletedEvent,
        TransactionFailedEvent,
        TransactionReversedEvent {

    UUID getEventId();

    UUID getTransactionId();

    // user-service'dagi AbstractAuditEntity<Long> bilan mos —
    // JWT subject ham shu ID orqali beriladi
    Long getUserId();

    Instant getOccurredAt();

    String getEventType();
}