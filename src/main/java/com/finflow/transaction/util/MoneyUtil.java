package com.finflow.transaction.util;

import com.finflow.transaction.domain.vo.Money;

import java.math.BigDecimal;

public final class MoneyUtil {

    private MoneyUtil() {
    }

    /** DB'dan kelgan yoki tashqi API'dan olingan qiymatni normallashtirish. */
    public static BigDecimal normalize(BigDecimal value) {
        return value == null ? null : value.setScale(Money.SCALE, Money.ROUNDING);
    }
}
