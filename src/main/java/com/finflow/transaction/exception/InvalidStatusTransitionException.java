package com.finflow.transaction.exception;

import com.finflow.transaction.enums.TransactionStatus;

public class InvalidStatusTransitionException extends ExceptionWithStatusCode {

    public InvalidStatusTransitionException(TransactionStatus from, TransactionStatus to) {
        super(409, "Illegal status transition: %s -> %s".formatted(from, to));
    }
}
