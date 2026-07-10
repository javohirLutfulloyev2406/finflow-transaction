package com.finflow.transaction.repository;

import com.finflow.transaction.domain.ScheduledPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPaymentEntity, Long> {

    // Quartz job'i: muddati kelgan va hali aktiv bo'lgan rejali to'lovlar.
    // ix_sched_next_execution (is_active, next_execution_at) indeksi ikki shartni birga qoplaydi.
    List<ScheduledPaymentEntity> findByActiveTrueAndNextExecutionAtBefore(Instant now);

    List<ScheduledPaymentEntity> findByUserIdAndActiveTrue(Long userId);
}