package com.finflow.transaction.validation;

import com.finflow.transaction.dto.command.TransferCommand;
import com.finflow.transaction.exception.ExceptionWithStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferBusinessRules extends TransactionBusinessValidator {

    public void validate(TransferCommand command) {

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

        if (command.targetAccountId() == null) {
            throw new ExceptionWithStatusCode(
                    400,
                    "Target account is required"
            );
        }

        if (command.sourceAccountId().equals(command.targetAccountId())) {
            throw new ExceptionWithStatusCode(
                    400,
                    "Source and target account must be different"
            );
        }
    }
}