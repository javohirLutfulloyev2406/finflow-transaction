package com.finflow.transaction.enums;

/**
 * Qo'llab-quvvatlanadigan valyutalar (TZ 4.3).
 * scale — pulning minimal birligi. UZS uchun tiyin amalda ishlatilmaydi,
 * lekin DB'da barcha valyuta uchun bir xil NUMERIC(19,4) saqlanadi:
 * konvertatsiya oralig'ida aniqlik yo'qolmasligi uchun.
 */
public enum Currency {

    UZS("860", "O'zbek so'mi", 2),
    USD("840", "US Dollar", 2),
    EUR("978", "Euro", 2),
    RUB("643", "Russian Ruble", 2);

    private final String numericCode;
    private final String displayName;
    private final int scale;

    Currency(String numericCode, String displayName, int scale) {
        this.numericCode = numericCode;
        this.displayName = displayName;
        this.scale = scale;
    }

    public String getNumericCode() {
        return numericCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getScale() {
        return scale;
    }
}
