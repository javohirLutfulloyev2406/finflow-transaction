package com.finflow.transaction.validation;

import com.finflow.transaction.dto.command.DepositCommand;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import org.springframework.stereotype.Component;

@Component
public class DepositBusinessRules extends TransactionBusinessValidator {

    public void validate(DepositCommand command) {

        validateUser(command.userId());
        validateMoney(command.amount());
        validateIdempotencyKey(command.idempotencyKey());
        validateDescription(command.description());

        if (command.targetAccountId() == null) {
            throw new ExceptionWithStatusCode(
                    400,
                    "Target account is required"
            );
        }
    }
}