package com.finflow.transaction.domain;

import com.finflow.transaction.domain.base.AbstractLongIdEntity;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.enums.ScheduleFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "scheduled_payments", schema = "tx_sch",
        indexes = @Index(name = "ix_sched_next_execution", columnList = "is_active, next_execution_at"))
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPaymentEntity extends AbstractLongIdEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "source_account_id", nullable = false)
    private Long sourceAccountId;

    @Column(name = "target_account_id", nullable = false)
    private Long targetAccountId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, length = 3))
    })
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 16)
    private ScheduleFrequency frequency;

    @Column(name = "next_execution_at", nullable = false)
    private Instant nextExecutionAt;

    @Column(name = "last_execution_at")
    private Instant lastExecutionAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "execution_count", nullable = false)
    @Builder.Default
    private Integer executionCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Quartz clustered: job'ni topish/o'chirish uchun. */
    @Column(name = "quartz_job_key", length = 128)
    private String quartzJobKey;

    @Column(name = "description", length = 255)
    private String description;
}
