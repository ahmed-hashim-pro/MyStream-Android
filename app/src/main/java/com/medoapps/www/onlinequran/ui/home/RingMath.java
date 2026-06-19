package com.medoapps.www.onlinequran.ui.home;

public final class RingMath {
    private RingMath() {}

    public static float sweepFraction(long prevPrayerMillis, long nextPrayerMillis, long nowMillis) {
        long interval = nextPrayerMillis - prevPrayerMillis;
        if (interval <= 0) return 1f;
        float f = (float) (nowMillis - prevPrayerMillis) / (float) interval;
        if (f < 0f) return 0f;
        if (f > 1f) return 1f;
        return f;
    }
}
