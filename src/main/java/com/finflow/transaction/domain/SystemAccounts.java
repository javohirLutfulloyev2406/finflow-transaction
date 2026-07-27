package com.finflow.transaction.domain;

import com.finflow.transaction.enums.Currency;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Deposit/withdraw'da ledger'ning "tashqi dunyo" tomoni — real bank
 * buxgalteriyasidagi "cash/suspense GL account" konsepsiyasi.
 *
 * MUHIM: bu UUID'lar HARDCODED va o'zgarmas bo'lishi SHART.
 * Barcha environment (dev/staging/prod)da bir xil qiymat kerak — aks holda
 * environment'lar orasida reconciliation solishtirilmaydigan holga keladi.
 *
 * Bu account'lar account-service'da MAVJUD EMAS va gRPC orqali HECH QACHON
 * chaqirilmaydi (debit/credit/getBalance) — faqat tx_sch.transaction_entries'da
 * SUM(DEBIT) == SUM(CREDIT) invariantini saqlash uchun ishlatiladi.
 */
public final class SystemAccounts {

    private SystemAccounts() {
    }

    private static final Map<Currency, UUID> SUSPENSE_ACCOUNTS = new EnumMap<>(Currency.class);

    static {
        SUSPENSE_ACCOUNTS.put(Currency.UZS, UUID.fromString("00000000-0000-0000-0000-000000000001"));
        SUSPENSE_ACCOUNTS.put(Currency.USD, UUID.fromString("00000000-0000-0000-0000-000000000002"));
        SUSPENSE_ACCOUNTS.put(Currency.EUR, UUID.fromString("00000000-0000-0000-0000-000000000003"));
        SUSPENSE_ACCOUNTS.put(Currency.RUB, UUID.fromString("00000000-0000-0000-0000-000000000004"));
    }

    public static UUID suspenseAccountFor(Currency currency) {
        UUID accountId = SUSPENSE_ACCOUNTS.get(currency);
        if (accountId == null) {
            throw new IllegalStateException("No suspense account configured for currency: " + currency);
        }
        return accountId;
    }
}