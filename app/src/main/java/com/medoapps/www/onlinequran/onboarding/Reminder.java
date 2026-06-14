package com.medoapps.www.onlinequran.onboarding;

/**
 * The seven optional daily reminders the user can toggle during onboarding.
 * Default hour/minute mirror each scheduler's existing defaults so an enabled
 * reminder lands at the same time the in-app Notification Settings screen uses.
 */
public enum Reminder {
    DAILY_AYAH(7, 0),
    MORNING_ATHKAR(6, 0),
    EVENING_ATHKAR(17, 0),
    DUA_HISN(20, 0),
    DAILY_HADITH(8, 0),
    ASMAUL_HUSNA(9, 0),
    SUHOOR_FASTING(4, 0);

    public final int defaultHour;
    public final int defaultMinute;

    Reminder(int defaultHour, int defaultMinute) {
        this.defaultHour = defaultHour;
        this.defaultMinute = defaultMinute;
    }
}
