package com.finflow.transaction.domain;

import com.finflow.transaction.enums.OutboxStatus;
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

/**
 * Outbox pattern: event DB'ga tranzaksiya ICHIDA yoziladi, keyin alohida
 * poller uni Kafka'ga uzatadi. "Pul ko'chdi, lekin event yo'qoldi" holati imkonsiz.
 *
 * AbstractAuditEntity'dan meros olmaydi: bu jadval juda tez o'sadi,
 * version/deleted/updated_by ustunlari bekorga joy egallaydi.
 */
@Entity
@Table(name = "outbox_events", schema = "tx_sch",
        indexes = @Index(name = "ix_outbox_status_created", columnList = "status, created_at"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** Kafka partition key manbai — bir aggregate eventlari tartibda keladi. */
    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    /** Consumer tarafda dedup uchun: bu ID inbox'ga yoziladi. */
    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers", columnDefinition = "jsonb")
    private String headers;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.NEW;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "last_error", length = 1024)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;
}
