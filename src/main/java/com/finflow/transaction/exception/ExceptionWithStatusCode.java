package com.finflow.transaction.exception;

import lombok.Getter;

/** finflow-user bilan bir xil kontrakt. */
@Getter
public class ExceptionWithStatusCode extends RuntimeException {

    private final int statusCode;

    public ExceptionWithStatusCode(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
