package com.medoapps.www.onlinequran.bubble;

import static com.google.common.truth.Truth.assertThat;
import org.junit.Test;

public class BubbleStyleTest {
    @Test public void roundTripCodes() {
        assertThat(BubbleStyle.fromCode("A")).isEqualTo(BubbleStyle.CHAT_HEAD);
        assertThat(BubbleStyle.fromCode("C")).isEqualTo(BubbleStyle.DRAWER);
        assertThat(BubbleStyle.fromCode("D")).isEqualTo(BubbleStyle.PILL);
        assertThat(BubbleStyle.CHAT_HEAD.code()).isEqualTo("A");
    }
    @Test public void unknownDefaultsToChatHead() {
        assertThat(BubbleStyle.fromCode("zzz")).isEqualTo(BubbleStyle.CHAT_HEAD);
        assertThat(BubbleStyle.fromCode(null)).isEqualTo(BubbleStyle.CHAT_HEAD);
    }
}
