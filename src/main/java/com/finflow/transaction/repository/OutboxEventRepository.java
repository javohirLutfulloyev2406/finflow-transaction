package com.finflow.transaction.repository;

import com.finflow.transaction.domain.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    // SKIP LOCKED: bir nechta OutboxPoller instance parallel ishlaydi.
    // Oddiy FOR UPDATE bo'lganda hamma poller birinchi instance tugaguncha kutardi —
    // bu throughput'ni yo'q qiladi va bir eventni ikki marta yuborishga yo'l ochadi.
    // SKIP LOCKED esa locked satrlarni o'tkazib yuboradi: har bir instance o'z
    // batch'ida ishlaydi, bir-biriga tegmaydi.
    @Query(value = """
            SELECT * FROM tx_sch.outbox_events
            WHERE status = 'NEW'
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEventEntity> findBatchForProcessing(@Param("batchSize") int batchSize);
}