package com.finflow.transaction.exception;

public class DuplicateRequestException extends ExceptionWithStatusCode {

    public DuplicateRequestException(String idempotencyKey) {
        super(409, "Duplicate request for Idempotency-Key: " + idempotencyKey);
    }
}
