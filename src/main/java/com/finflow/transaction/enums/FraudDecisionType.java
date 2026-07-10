package com.finflow.transaction.enums;

public enum FraudDecisionType {

    ALLOW,

    /** Tranzaksiya davom etadi, lekin admin ko'rib chiqish uchun belgilanadi. */
    REVIEW,

    BLOCK;

    /** Chain'da eng qattiq qaror g'olib bo'ladi. */
    public FraudDecisionType strictest(FraudDecisionType other) {
        return this.ordinal() >= other.ordinal() ? this : other;
    }
}
