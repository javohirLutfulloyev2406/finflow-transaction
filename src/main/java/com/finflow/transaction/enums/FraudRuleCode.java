package com.finflow.transaction.enums;

public enum FraudRuleCode {

    /** Redis sliding window: 1 daqiqada N martadan ko'p urinish. */
    VELOCITY,

    DAILY_LIMIT,
    MONTHLY_LIMIT,

    /** Tunda (00:00-05:00) katta summa. */
    NIGHT_LARGE_AMOUNT,

    NEW_DEVICE,

    /** Spring AI hook — kelajakda. */
    ML_SCORE
}
