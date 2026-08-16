package com.finflow.transaction.exception;

public class UserServiceUnavailableException extends ExceptionWithStatusCode {

    public UserServiceUnavailableException() {
        super(503, "User service is unavailable");
    }
}