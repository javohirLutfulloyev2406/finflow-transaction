package com.finflow.transaction.domain.vo;

import com.finflow.transaction.enums.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Pul — immutable value object.
 *
 * Qoidalar (buzilmaydi):
 *  - BigDecimal(19,4), RoundingMode.HALF_EVEN (banker's rounding — statistik siljish bermaydi)
 *  - double/float taqiqlangan
 *  - Har amalda valyuta mosligi tekshiriladi
 *  - equals() BigDecimal.compareTo orqali: 10.00 va 10.0 bir xil pul, lekin
 *    BigDecimal.equals() ularni FARQLI deb hisoblaydi (scale'ni ham solishtiradi).
 *    Bu klassik bank bug'i.
 *
 * Setter YO'Q. Hibernate maydonlarga refleksiya orqali yozadi — shuning uchun
 * protected no-arg konstruktor yetarli.
 */
@Embeddable
public final class Money implements Serializable, Comparable<Money> {

    private static final long serialVersionUID = 1L;

    public static final int SCALE = 4;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    protected Money() {
        // JPA uchun
    }

    private Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "amount must not be null")
                .setScale(SCALE, ROUNDING);
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: %s vs %s".formatted(this.currency, other.currency));
        }
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return this.currency == other.currency
                && this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        // stripTrailingZeros: 10.0000 va 10.00 bir xil hash bersin (equals bilan mos)
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
