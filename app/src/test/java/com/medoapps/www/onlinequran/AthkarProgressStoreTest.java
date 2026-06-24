package com.medoapps.www.onlinequran;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class AthkarProgressStoreTest {
    @Test public void key_isStableAndDistinct() {
        assertThat(AthkarProgressStore.key(175, "MORNING", 3)).isEqualTo("done_175_MORNING_3");
        assertThat(AthkarProgressStore.key(175, "EVENING", 3))
                .isNotEqualTo(AthkarProgressStore.key(175, "MORNING", 3));
    }
}
