package com.medoapps.www.onlinequran.bubble;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class BubbleSessionSelectorTest {
    private static final long FAJR = 5  * 3600_000L;  // 05:00
    private static final long ASR  = 16 * 3600_000L;  // 16:00
    @Test public void betweenFajrAndAsr_isMorning() {
        assertThat(BubbleSessionSelector.select(9 * 3600_000L, FAJR, ASR)).isEqualTo(BubbleSession.MORNING);
    }
    @Test public void afterAsr_isEvening() {
        assertThat(BubbleSessionSelector.select(19 * 3600_000L, FAJR, ASR)).isEqualTo(BubbleSession.EVENING);
    }
    @Test public void beforeFajr_isEvening() {
        assertThat(BubbleSessionSelector.select(3 * 3600_000L, FAJR, ASR)).isEqualTo(BubbleSession.EVENING);
    }
    @Test public void exactlyFajr_isMorning() {
        assertThat(BubbleSessionSelector.select(FAJR, FAJR, ASR)).isEqualTo(BubbleSession.MORNING);
    }
}
