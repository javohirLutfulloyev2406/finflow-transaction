package com.finflow.transaction.repository;

import com.finflow.transaction.domain.SagaStateEntity;
import com.finflow.transaction.enums.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaStateRepository extends JpaRepository<SagaStateEntity, Long> {

    Optional<SagaStateEntity> findByTransactionId(UUID transactionId);

    // StuckTransactionJob uchun: belgilangan vaqtdan oshib ketgan STARTED/DEBITED
    // statusdagi saga'larni topib, compensation boshlaydi.
    List<SagaStateEntity> findByStatusInAndStartedAtBefore(Collection<SagaStatus> statuses, Instant before);
}