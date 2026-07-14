package com.finflow.transaction.service;

import com.finflow.transaction.domain.TransactionEntity;
import com.finflow.transaction.dto.response.FraudCheckResponse;

public interface FraudService {

    FraudCheckResponse check(TransactionEntity transaction);

}