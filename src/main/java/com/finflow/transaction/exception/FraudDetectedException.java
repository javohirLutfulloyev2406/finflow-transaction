package com.finflow.transaction.exception;

public class FraudDetectedException extends ExceptionWithStatusCode {

    public FraudDetectedException(String reason) {
        super(403, "Transaction blocked by fraud engine: " + reason);
    }
}
