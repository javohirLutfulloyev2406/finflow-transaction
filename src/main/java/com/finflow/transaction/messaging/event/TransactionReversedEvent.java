package com.finflow.transaction.messaging.event;

import com.finflow.transaction.domain.vo.Money;

import com.finflow.transaction.enums.TransactionType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

/**
 * Tranzaksiya bekor qilinib (reversal), qarshi yozuv (compensating entry) orqali
 * ortga qaytarilganda yuboriladigan event. Bu COMPLETED bo'lgan tranzaksiyani
 * "yo'qotish" emas — yangi qarshi tranzaksiya sifatida qayd etiladi (audit trail buzilmasin uchun).
 */
@Value
@Builder
@Jacksonized
public class TransactionReversedEvent implements TransactionEvent {

    UUID eventId;
    UUID transactionId;          // reverse qilinayotgan asl tranzaksiya ID'si
    UUID reversalTransactionId;  // yangi yaratilgan qarshi yozuv (compensating) tranzaksiya ID'si
    Long userId;
    Instant occurredAt;

    UUID sourceAccountId;
    UUID targetAccountId;

    Money amount;
    TransactionType transactionType;

    String reversalReason;   // masalan: "SAGA_COMPENSATION", "MANUAL_ADMIN_REVERSAL", "FRAUD_ROLLBACK"

    @Override
    public String getEventType() {
        return "TRANSACTION_REVERSED";
    }
}