package com.finflow.transaction.domain;

import com.finflow.transaction.enums.FraudDecisionType;
import com.finflow.transaction.enums.FraudRuleCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Har bir rule natijasi alohida satr sifatida saqlanadi.
 * Sabab: "nega bu tranzaksiya bloklandi?" savoliga regulyator oldida
 * javob berish kerak bo'ladi. Yagona "blocked=true" flag yetarli emas.
 */
@Entity
@Table(name = "fraud_checks", schema = "tx_sch",
        indexes = @Index(name = "ix_fraud_transaction_id", columnList = "transaction_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_code", nullable = false, length = 32)
    private FraudRuleCode ruleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 16)
    private FraudDecisionType decision;

    @Column(name = "score", precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "checked_at", nullable = false, updatable = false)
    private Instant checkedAt;
}
