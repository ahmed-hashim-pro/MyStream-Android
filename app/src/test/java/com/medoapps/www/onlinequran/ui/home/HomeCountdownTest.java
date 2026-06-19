package com.medoapps.www.onlinequran.ui.home;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class HomeCountdownTest {

    private static final long HOUR = 3_600_000L;
    private static final long MIN = 60_000L;
    private static final long DAY = 86_400_000L;

    @Test
    public void remaining_targetInFuture_isSimpleDiff() {
        assertThat(HomeCountdown.remainingMillis(1_000L, 1_000L + 2 * HOUR))
                .isEqualTo(2 * HOUR);
    }

    @Test
    public void remaining_targetPassed_wrapsToNextDay() {
        // target was 1h ago -> should report ~23h until tomorrow's occurrence
        assertThat(HomeCountdown.remainingMillis(5 * HOUR, 4 * HOUR))
                .isEqualTo(DAY - HOUR);
    }

    @Test
    public void format_underOneHour_minutesOnly() {
        assertThat(HomeCountdown.format(47 * MIN)).isEqualTo("47m");
    }

    @Test
    public void format_overOneHour_hoursAndMinutes() {
        assertThat(HomeCountdown.format(2 * HOUR + 14 * MIN)).isEqualTo("2h 14m");
    }

    @Test
    public void format_exactlyOneHour_padsMinutes() {
        assertThat(HomeCountdown.format(HOUR)).isEqualTo("1h 00m");
    }

    @Test
    public void format_zeroOrNegative_isNow() {
        assertThat(HomeCountdown.format(0L)).isEqualTo("now");
        assertThat(HomeCountdown.format(-5L)).isEqualTo("now");
    }
}
