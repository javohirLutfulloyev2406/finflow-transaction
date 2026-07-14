package com.finflow.transaction.messaging.event;

import com.finflow.transaction.domain.vo.Money;

import com.finflow.transaction.enums.TransactionType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

/**
 * Tranzaksiya muvaffaqiyatsiz yakunlanganda (FAILED holatga o'tganda) yuboriladigan event.
 * failureReason enum emas, String — chunki xato manbai turlicha bo'lishi mumkin:
 * gRPC timeout, INSUFFICIENT_FUNDS, fraud rule bloklashi va h.k. Har birini alohida
 * enum qilib TransactionEvent'ga bog'lab qo'yish contract'ni haddan tashqari qattiqlashtiradi.
 */
@Value
@Builder
@Jacksonized
public class TransactionFailedEvent implements TransactionEvent {

    UUID eventId;
    UUID transactionId;
    Long userId;
    Instant occurredAt;

    UUID sourceAccountId;
    UUID targetAccountId;

    Money amount;
    TransactionType transactionType;

    String failureCode;      // masalan: "INSUFFICIENT_FUNDS", "ACCOUNT_FROZEN", "GRPC_TIMEOUT"
    String failureReason;    // inson o'qiy oladigan tavsif (log/notification uchun)

    @Override
    public String getEventType() {
        return "TRANSACTION_FAILED";
    }
}