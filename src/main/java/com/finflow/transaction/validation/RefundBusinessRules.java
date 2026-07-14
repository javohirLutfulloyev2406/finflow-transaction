package com.finflow.transaction.validation;

import com.finflow.transaction.domain.TransactionEntity;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import org.springframework.stereotype.Component;

@Component
public class RefundBusinessRules {

    public void validate(TransactionEntity transaction) {

        if (transaction == null) {
            throw new ExceptionWithStatusCode(
                    404,
                    "Transaction not found"
            );
        }

        if (!transaction.isRefundable()) {
            throw new ExceptionWithStatusCode(
                    409,
                    "Transaction cannot be refunded"
            );
        }
    }
}