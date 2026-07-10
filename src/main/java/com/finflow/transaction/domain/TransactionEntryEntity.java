package com.finflow.transaction.domain;

import com.finflow.transaction.domain.base.AbstractLongIdEntity;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.enums.EntryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

/**
 * Double-entry ledger satri. Yaratilgandan keyin O'ZGARMAYDI (updatable = false).
 * Buxgalteriyada yozuv tuzatilmaydi — teskari yozuv qo'shiladi.
 *
 * Invariant: har bir transaction uchun SUM(DEBIT) == SUM(CREDIT).
 * ReconciliationJob buni har kuni tekshiradi.
 */
@Entity
@Table(name = "transaction_entries", schema = "tx_sch",
        indexes = {
                @Index(name = "ix_entry_account_id", columnList = "account_id"),
                @Index(name = "ix_entry_transaction_id", columnList = "transaction_id")
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntryEntity extends AbstractLongIdEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_entry_transaction_id"))
    private TransactionEntity transaction;

    @Column(name = "account_id", nullable = false, updatable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, updatable = false, length = 8)
    private EntryType entryType;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, updatable = false, precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, updatable = false, length = 3))
    })
    private Money amount;

    /** Yozuvdan keyingi balans snapshot'i — audit va reconciliation uchun. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "balance_after_amount", precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "balance_after_currency", length = 3))
    })
    private Money balanceAfter;

    @Column(name = "booked_at", nullable = false, updatable = false)
    private Instant bookedAt;
}
