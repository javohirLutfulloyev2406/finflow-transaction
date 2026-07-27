package com.finflow.transaction.exception;

import java.util.UUID;

public class InsufficientFundsException extends ExceptionWithStatusCode {

    public InsufficientFundsException(UUID accountId) {
        super(422, "Insufficient funds on account: " + accountId);
    }
}
