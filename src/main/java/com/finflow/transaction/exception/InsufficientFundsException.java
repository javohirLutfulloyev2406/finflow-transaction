package com.finflow.transaction.exception;

public class InsufficientFundsException extends ExceptionWithStatusCode {

    public InsufficientFundsException(Long accountId) {
        super(422, "Insufficient funds on account: " + accountId);
    }
}
