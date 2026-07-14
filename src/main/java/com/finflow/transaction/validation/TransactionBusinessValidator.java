package com.finflow.transaction.validation;

import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import org.springframework.stereotype.Component;

@Component
public class TransactionBusinessValidator {

    protected void validateMoney(Money money) {

        if (money == null) {
            throw new ExceptionWithStatusCode(400, "Amount is required");
        }

        if (!money.isPositive()) {
            throw new ExceptionWithStatusCode(400, "Amount must be positive");
        }
    }

    protected void validateUser(Long userId) {

        if (userId == null) {
            throw new ExceptionWithStatusCode(401, "User is required");
        }
    }

    protected void validateIdempotencyKey(String key) {

        if (key == null || key.isBlank()) {
            throw new ExceptionWithStatusCode(
                    400,
                    "Idempotency-Key is required"
            );
        }
    }

    protected void validateDescription(String description) {

        if (description != null && description.length() > 255) {
            throw new ExceptionWithStatusCode(
                    400,
                    "Description is too long"
            );
        }
    }
}