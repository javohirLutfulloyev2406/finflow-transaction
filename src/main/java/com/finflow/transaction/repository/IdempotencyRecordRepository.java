package com.finflow.transaction.repository;

import com.finflow.transaction.domain.IdempotencyRecordEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, Long> {

    Optional<IdempotencyRecordEntity> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    // Bulk DELETE — derived deleteBy entity'larni bittama-bittа load qilib o'chiradi
    // (N+1). @Query + @Modifying bitta SQL DELETE bilan bir vaqtda o'chiradi.
    // CleanupJob tungi cron'da ishlaydi, jadval yuz minglab eskirgan yozuvni tutishi mumkin.
    @Modifying
    @Query("DELETE FROM IdempotencyRecordEntity i WHERE i.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") Instant now);
}