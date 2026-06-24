package com.medoapps.www.onlinequran;

import static com.google.common.truth.Truth.assertThat;
import java.util.List;
import org.junit.Test;

public class AthkarRepositoryTest {
    @Test public void morning_isNonEmptyAndAllContent() {
        List<AthkarItem> m = AthkarRepository.getMorningItems();
        assertThat(m).isNotEmpty();
        for (AthkarItem it : m) {
            assertThat(it.isHeader).isFalse();
            assertThat(it.text).isNotEmpty();
            assertThat(it.remainingCount).isAtLeast(1);
        }
    }
    @Test public void evening_isNonEmpty() {
        assertThat(AthkarRepository.getEveningItems()).isNotEmpty();
    }
    @Test public void morning_containsTasbih100() {
        boolean has100 = AthkarRepository.getMorningItems().stream().anyMatch(i -> i.remainingCount == 100);
        assertThat(has100).isTrue();
    }
}
