package com.medoapps.www.onlinequran.ui.home;

import java.util.Locale;

/** Pure helpers for the home next-prayer countdown. No Android dependencies. */
public final class HomeCountdown {

    private static final long DAY_MILLIS = 86_400_000L;
    private static final long HOUR_MILLIS = 3_600_000L;
    private static final long MINUTE_MILLIS = 60_000L;

    private HomeCountdown() {}

    /** Millis from now until target; wraps to the next day if target already passed. */
    public static long remainingMillis(long nowMillis, long targetMillis) {
        long diff = targetMillis - nowMillis;
        if (diff < 0) {
            diff += DAY_MILLIS;
        }
        return diff;
    }

    /** "now" / "47m" / "2h 14m". */
    public static String format(long millisRemaining) {
        if (millisRemaining <= 0) {
            return "now";
        }
        long hours = millisRemaining / HOUR_MILLIS;
        long minutes = (millisRemaining % HOUR_MILLIS) / MINUTE_MILLIS;
        if (hours == 0) {
            return minutes + "m";
        }
        return String.format(Locale.US, "%dh %02dm", hours, minutes);
    }
}
