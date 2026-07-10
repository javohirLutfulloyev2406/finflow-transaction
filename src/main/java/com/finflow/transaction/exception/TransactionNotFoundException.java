package com.finflow.transaction.exception;

import java.util.UUID;

public class TransactionNotFoundException extends ExceptionWithStatusCode {

    public TransactionNotFoundException(UUID id) {
        super(404, "Transaction not found: " + id);
    }
}
