package com.medoapps.www.onlinequran.ui.home;
import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;
public class RingMathTest {
  @Test public void midway_isHalf() {
    assertThat(RingMath.sweepFraction(0L, 1000L, 500L)).isWithin(1e-4f).of(0.5f);
  }
  @Test public void beforeStart_clampsToZero() {
    assertThat(RingMath.sweepFraction(100L, 1000L, 50L)).isEqualTo(0f);
  }
  @Test public void afterEnd_clampsToOne() {
    assertThat(RingMath.sweepFraction(0L, 1000L, 2000L)).isEqualTo(1f);
  }
  @Test public void nonPositiveInterval_isOne() {
    assertThat(RingMath.sweepFraction(1000L, 1000L, 1000L)).isEqualTo(1f);
  }
}
