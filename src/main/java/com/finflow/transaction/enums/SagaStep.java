package com.finflow.transaction.enums;

public enum SagaStep {

    DEBIT_SOURCE,
    CREDIT_TARGET,
    FINALIZE,

    /** CREDIT muvaffaqiyatsiz bo'lsa: DEBIT'ni teskarisiga qaytarish. */
    COMPENSATE_REFUND
}
