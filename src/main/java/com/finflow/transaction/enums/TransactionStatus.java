package com.finflow.transaction.enums;

import java.util.Set;

/**
 * Status machine — o'tish qoidalari enum ICHIDA.
 * Sabab: service'lar bo'ylab `if (status == PENDING)` tarqalib ketsa,
 * yangi status qo'shilganda qaysi joyni yangilashni hech kim bilmaydi.
 * Bu yerda bitta joyda, unit-test bilan qoplanadi.
 *
 * INITIATED -> PENDING -> COMPLETED | FAILED | CANCELLED
 * COMPLETED -> REFUNDED
 *
 * Eslatma: enum konstruktorida boshqa konstantalarga murojaat qilib bo'lmaydi
 * (ular hali initsializatsiya qilinmagan). Shuning uchun switch-expression
 * ishlatilgan — EnumSet static bloki emas. Kompilyator yangi konstanta
 * qo'shilganda exhaustiveness'ni tekshiradi.
 */
public enum TransactionStatus {

    INITIATED,
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED;

    public Set<TransactionStatus> allowedTransitions() {
        return switch (this) {
            case INITIATED -> Set.of(PENDING, FAILED);
            case PENDING -> Set.of(COMPLETED, FAILED, CANCELLED);
            case COMPLETED -> Set.of(REFUNDED);
            case FAILED, CANCELLED, REFUNDED -> Set.of();
        };
    }

    public boolean canTransitionTo(TransactionStatus target) {
        return target != null && allowedTransitions().contains(target);
    }

    /** Terminal holat — bu yerdan hech qayerga yo'l yo'q. */
    public boolean isTerminal() {
        return allowedTransitions().isEmpty();
    }

    /** Pul haqiqatan ko'chganmi? Reconciliation job shuni sanaydi. */
    public boolean isSettled() {
        return this == COMPLETED || this == REFUNDED;
    }
}
