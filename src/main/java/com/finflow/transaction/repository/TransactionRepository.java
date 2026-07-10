package com.finflow.transaction.repository;

import com.finflow.transaction.domain.TransactionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<TransactionEntity, UUID>,
                JpaSpecificationExecutor<TransactionEntity> {

    Optional<TransactionEntity> findByReference(String reference);

    Optional<TransactionEntity> findBySourceUserIdAndIdempotencyKey(Long sourceUserId, String idempotencyKey);

    // Row-value (created_at, id) < (:cursorCreatedAt, :cursorId) — PostgreSQL'ga xos sintaksis,
    // JPQL'da yo'q, shuning uchun nativeQuery = true.
    // OR-expansion varianti: (created_at < x) OR (created_at = x AND id < y)
    // — query planner bu ikkita shartni alohida baholab bitmap scan tanlaydi,
    // composite index ix_tx_user_created_id'ni to'liq ishlatmaydi.
    // Row-value esa indexscan'ni bir yo'nalishda haydaydi — to'g'ridan-to'g'ri efficient.
    @Query(value = """
            SELECT * FROM tx_sch.transactions
            WHERE source_user_id = :userId
              AND (created_at, id) < (:cursorCreatedAt, :cursorId)
              AND is_deleted = false
            ORDER BY created_at DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TransactionEntity> findPageAfterCursor(
            @Param("userId") Long userId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT * FROM tx_sch.transactions
            WHERE source_user_id = :userId
              AND is_deleted = false
            ORDER BY created_at DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TransactionEntity> findFirstPage(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    // Pessimistic lock — status o'zgartirishda ikki parallel thread bitta
    // TransactionEntity'ga kirmasin. @Version (optimistic) retry'ga undaydi,
    // lekin "pul ikki marta chiqdi" holati retry'dan OLDIN yuz berishi mumkin.
    // Pessimistic lock ikkinchi threadni birinchisi commit qilguncha kuttirib,
    // bu xavfni to'liq bartaraf etadi. @Version — qo'shimcha sug'urta, uning o'rniga emas.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransactionEntity t WHERE t.id = :id")
    Optional<TransactionEntity> findByIdForUpdate(@Param("id") UUID id);
}