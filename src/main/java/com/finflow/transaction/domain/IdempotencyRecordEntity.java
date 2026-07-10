package com.finflow.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Idempotency'ning IKKINCHI qatlami. Redis — tez, lekin durable emas.
 * Redis o'chib qolsa yoki TTL noto'g'ri ishlasa, pul ikki marta ketmasligi
 * uchun haqiqiy himoya shu yerda: UNIQUE(user_id, idempotency_key).
 *
 * request_hash — bir xil kalit bilan BOSHQA payload kelsa 422 qaytariladi.
 * Aks holda client xatosi jimgina yashiriladi.
 */
@Entity
@Table(name = "idempotency_records", schema = "tx_sch",
        uniqueConstraints = @UniqueConstraint(name = "uq_idem_user_key",
                columnNames = {"user_id", "idempotency_key"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "text")
    private String responseBody;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 24 soat (TZ). Cleanup job eskilarini o'chiradi. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
