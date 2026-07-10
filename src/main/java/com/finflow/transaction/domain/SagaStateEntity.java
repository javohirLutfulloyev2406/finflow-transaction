package com.finflow.transaction.domain;

import com.finflow.transaction.enums.SagaStatus;
import com.finflow.transaction.enums.SagaStep;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Saga holati PERSISTENT bo'lishi shart. Choreography'da hech kim
 * "umumiy rasm"ni ko'rmaydi — service qayta ishga tushsa, yarim qolgan
 * saga'ni faqat shu jadval orqali topib, compensate qilish mumkin.
 * StuckTransactionJob shu yerdan o'qiydi.
 */
@Entity
@Table(name = "saga_states", schema = "tx_sch",
        uniqueConstraints = @UniqueConstraint(name = "uq_saga_transaction_id", columnNames = "transaction_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true, updatable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private SagaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 24)
    private SagaStep currentStep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_error", length = 1024)
    private String lastError;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
