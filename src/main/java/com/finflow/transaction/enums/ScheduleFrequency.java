package com.finflow.transaction.enums;

public enum ScheduleFrequency {

    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY;

    public boolean isRecurring() {
        return this != ONCE;
    }
}
