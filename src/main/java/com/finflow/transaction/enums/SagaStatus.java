package com.finflow.transaction.enums;

public enum SagaStatus {

    STARTED,
    DEBITED,
    CREDITED,
    COMPLETED,

    /** Compensation boshlandi: debit qaytarilmoqda. */
    COMPENSATING,
    COMPENSATED,
    FAILED;

    public boolean isTerminal() {
        return this == COMPLETED || this == COMPENSATED || this == FAILED;
    }
}
