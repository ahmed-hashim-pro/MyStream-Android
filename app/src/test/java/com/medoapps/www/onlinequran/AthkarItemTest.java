package com.medoapps.www.onlinequran;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class AthkarItemTest {
    @Test public void parseCount_digits() { assertThat(AthkarItem.parseCount("100 مرة")).isEqualTo(100); }
    @Test public void parseCount_singleWord() { assertThat(AthkarItem.parseCount("مرة واحدة")).isEqualTo(1); }
    @Test public void parseCount_nullOrEmpty() {
        assertThat(AthkarItem.parseCount(null)).isEqualTo(1);
        assertThat(AthkarItem.parseCount("")).isEqualTo(1);
    }
    @Test public void contentCtor_setsRemainingFromCount() {
        AthkarItem it = new AthkarItem("سُبْحَانَ اللهِ وَبِحَمْدِهِ", "100 مرة", false);
        assertThat(it.isHeader).isFalse();
        assertThat(it.remainingCount).isEqualTo(100);
    }
    @Test public void headerCtor_hasChildrenAndZeroRemaining() {
        AthkarItem h = new AthkarItem("الصباح", true);
        assertThat(h.isHeader).isTrue();
        assertThat(h.children).isNotNull();
        assertThat(h.remainingCount).isEqualTo(0);
    }
}
