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
 * INBOX. Kafka at-least-once beradi — bitta event ikki marta kelishi mumkin.
 * event_id UNIQUE constraint'i ikkinchi urinishni DB darajasida to'xtatadi.
 * Faqat outbox yozish yarim ish: consumer tarafda dedup bo'lmasa saga ikki marta yuradi.
 */
@Entity
@Table(name = "processed_events", schema = "tx_sch",
        uniqueConstraints = @UniqueConstraint(name = "uq_processed_event_id", columnNames = "event_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEventEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, updatable = false, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "source", nullable = false, length = 64)
    private String source;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;
}
