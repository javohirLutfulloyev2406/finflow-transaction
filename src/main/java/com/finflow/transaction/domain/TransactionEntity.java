package com.finflow.transaction.domain;

import com.finflow.transaction.domain.base.AbstractAuditEntity;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.enums.TransactionStatus;
import com.finflow.transaction.enums.TransactionType;
import com.finflow.transaction.exception.InvalidStatusTransitionException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root.
 *
 * PK — UUID v7 (@UuidGenerator style=TIME): vaqt bo'yicha tartiblangan.
 *  - sequential BIGINT tashqariga chiqsa, kunlik tranzaksiya hajmi oshkor bo'ladi
 *  - tasodifiy UUID v4 B-tree indeksini parchalaydi (random insert)
 *  - v7 ikkalasini ham hal qiladi va (created_at, id) cursor pagination'ni to'g'ri ishlatadi
 *
 * status uchun public setter YO'Q. Faqat transitionTo() orqali.
 */
@Entity
@Table(name = "transactions", schema = "tx_sch",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tx_reference", columnNames = "reference"),
                @UniqueConstraint(name = "uq_tx_user_idempotency",
                        columnNames = {"source_user_id", "idempotency_key"})
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity extends AbstractAuditEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Odam o'qiydigan ID: TXN-20260710-A7F3K2. Support bilan gaplashish uchun. */
    @Column(name = "reference", nullable = false, updatable = false, length = 32)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 16)
    private TransactionType type;

    /** Setter private — tashqaridan o'zgartirib bo'lmaydi. */
    @Setter(AccessLevel.PRIVATE)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TransactionStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, length = 3))
    })
    private Money amount;

    @Column(name = "source_account_id")
    private Long sourceAccountId;

    @Column(name = "target_account_id")
    private Long targetAccountId;

    @Column(name = "source_user_id")
    private Long sourceUserId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
    private String idempotencyKey;

    /** REFUND uchun: qaysi tranzaksiyani qaytaryapmiz. */
    @Column(name = "original_transaction_id")
    private UUID originalTransactionId;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "failure_reason", length = 512)
    private String failureReason;

    @Column(name = "initiated_at", nullable = false, updatable = false)
    private Instant initiatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /** Fraud detection konteksti. */
    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Builder.Default
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TransactionEntryEntity> entries = new ArrayList<>();

    // ---------------------------------------------------------------
    // Domen mantiqi
    // ---------------------------------------------------------------

    /**
     * Yagona yo'l status o'zgartirish uchun.
     * Noto'g'ri o'tish — 409 Conflict, jimgina o'tib ketmaydi.
     */
    public void transitionTo(TransactionStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(this.status, target);
        }
        this.status = target;
        if (target.isSettled()) {
            this.completedAt = Instant.now();
        }
    }

    public void markFailed(String reason) {
        transitionTo(TransactionStatus.FAILED);
        this.failureReason = reason;
    }

    public void addEntry(TransactionEntryEntity entry) {
        entry.setTransaction(this);
        this.entries.add(entry);
    }

    public boolean isCancellable() {
        return this.status.canTransitionTo(TransactionStatus.CANCELLED);
    }

    public boolean isRefundable() {
        return this.status.canTransitionTo(TransactionStatus.REFUNDED);
    }
}
