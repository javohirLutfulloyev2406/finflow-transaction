package com.finflow.transaction.repository;

import com.finflow.transaction.domain.TransactionEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntryEntity, Long> {

    // Spring Data path traversal: transaction (ManyToOne) → id
    List<TransactionEntryEntity> findByTransactionId(UUID transactionId);

    // Reconciliation invariant: SUM(DEBIT) - SUM(CREDIT) = 0 har bir tranzaksiya uchun.
    // COALESCE — entry'lar yo'q bo'lsa NULL emas 0 qaytarsin (ReconciliationJob null tekshirmaydi).
    @Query(value = """
            SELECT COALESCE(SUM(CASE WHEN entry_type = 'DEBIT' THEN amount ELSE -amount END), 0)
            FROM tx_sch.transaction_entries
            WHERE transaction_id = :transactionId
            """, nativeQuery = true)
    BigDecimal sumDebitMinusCredit(@Param("transactionId") UUID transactionId);
}