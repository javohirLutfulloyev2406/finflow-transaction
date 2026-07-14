package com.finflow.transaction.exception;

import com.finflow.transaction.exception.ExceptionWithStatusCode;

public class AccountServiceUnavailableException extends ExceptionWithStatusCode {

    public AccountServiceUnavailableException() {
        super(503, "Account service is unavailable");
    }
}