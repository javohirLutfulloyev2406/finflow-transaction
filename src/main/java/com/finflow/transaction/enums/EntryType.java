package com.finflow.transaction.enums;

/**
 * Double-entry bookkeeping (TZ 11-bo'lim).
 * Har bir tranzaksiya kamida bitta DEBIT va bitta CREDIT satr yaratadi.
 */
public enum EntryType {

    DEBIT,
    CREDIT;

    public EntryType opposite() {
        return this == DEBIT ? CREDIT : DEBIT;
    }
}
