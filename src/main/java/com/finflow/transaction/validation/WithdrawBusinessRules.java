package com.finflow.transaction.validation;

import com.finflow.transaction.dto.command.WithdrawCommand;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import org.springframework.stereotype.Component;

@Component
public class WithdrawBusinessRules extends TransactionBusinessValidator {

    public void validate(WithdrawCommand command) {

        validateUser(command.userId());
        validateMoney(command.amount());
        validateIdempotencyKey(command.idempotencyKey());
        validateDescription(command.description());

        if (command.sourceAccountId() == null) {
            throw new ExceptionWithStatusCode(
                    400,
                    "Source account is required"
            );
        }
    }
}