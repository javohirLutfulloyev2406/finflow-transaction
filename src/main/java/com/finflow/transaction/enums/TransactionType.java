package com.finflow.transaction.enums;

public enum TransactionType {

    /** Ikki hisob orasida: source DEBIT, target CREDIT. Saga talab qiladi. */
    TRANSFER,

    /** Tashqaridan hisobga: faqat target CREDIT. */
    DEPOSIT,

    /** Hisobdan tashqariga: faqat source DEBIT. */
    WITHDRAW,

    /** Tugallangan tranzaksiyani teskarisiga qaytarish. Original tx'ga bog'lanadi. */
    REFUND;

    public boolean requiresSourceAccount() {
        return this == TRANSFER || this == WITHDRAW || this == REFUND;
    }

    public boolean requiresTargetAccount() {
        return this == TRANSFER || this == DEPOSIT || this == REFUND;
    }
}
