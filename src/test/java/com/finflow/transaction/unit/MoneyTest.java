package com.finflow.transaction.unit;

import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.enums.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void scaleIsAlwaysFour() {
        assertThat(Money.of("10", Currency.UZS).getAmount())
                .isEqualByComparingTo(new BigDecimal("10.0000"));
    }

    @Test
    void equalsIgnoresScale() {
        // Klassik bank bug'i: BigDecimal.equals("10.00", "10.0") -> false
        assertThat(Money.of("10.00", Currency.USD))
                .isEqualTo(Money.of("10.0000", Currency.USD));
    }

    @Test
    void hashCodeConsistentWithEquals() {
        assertThat(Money.of("10.00", Currency.USD).hashCode())
                .isEqualTo(Money.of("10.0000", Currency.USD).hashCode());
    }

    @Test
    void halfEvenRounding() {
        // 0.00005 -> 0.0000 (juft tomonga), 0.00015 -> 0.0002
        assertThat(Money.of("0.00005", Currency.USD).getAmount()).isEqualByComparingTo("0.0000");
        assertThat(Money.of("0.00015", Currency.USD).getAmount()).isEqualByComparingTo("0.0002");
    }

    @Test
    void currencyMismatchIsRejected() {
        assertThatThrownBy(() -> Money.of("1", Currency.USD).add(Money.of("1", Currency.EUR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    void arithmeticIsImmutable() {
        Money original = Money.of("100", Currency.UZS);
        original.add(Money.of("50", Currency.UZS));
        assertThat(original.getAmount()).isEqualByComparingTo("100.0000");
    }
}
