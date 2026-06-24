package com.medoapps.www.onlinequran.bubble;

/** Picks the athkar session for "now": morning while in [Fajr, Asr), evening otherwise. */
public final class BubbleSessionSelector {
    private BubbleSessionSelector() {}
    public static BubbleSession select(long nowMillis, long fajrMillis, long asrMillis) {
        return (nowMillis >= fajrMillis && nowMillis < asrMillis)
                ? BubbleSession.MORNING : BubbleSession.EVENING;
    }
}
