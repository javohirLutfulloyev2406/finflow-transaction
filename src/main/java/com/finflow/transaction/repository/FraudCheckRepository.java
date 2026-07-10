package com.finflow.transaction.repository;

import com.finflow.transaction.domain.FraudCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudCheckRepository extends JpaRepository<FraudCheckEntity, Long> {

    List<FraudCheckEntity> findByTransactionId(UUID transactionId);
}