package com.finflow.transaction.repository;

import com.finflow.transaction.domain.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, Long> {

    boolean existsByEventId(String eventId);
}