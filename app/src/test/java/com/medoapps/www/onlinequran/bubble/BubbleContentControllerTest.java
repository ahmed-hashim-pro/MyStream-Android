package com.medoapps.www.onlinequran.bubble;

import static com.google.common.truth.Truth.assertThat;
import com.medoapps.www.onlinequran.AthkarItem;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class BubbleContentControllerTest {
    private BubbleContentController make() {
        List<AthkarItem> items = Arrays.asList(
                new AthkarItem("a", "مرة واحدة", false),  // target 1
                new AthkarItem("b", "3 مرة", false),      // target 3
                new AthkarItem("c", "100 مرة", false));   // target 100
        return new BubbleContentController(items);
    }
    @Test public void startsAtFirst() {
        BubbleContentController c = make();
        assertThat(c.currentIndex()).isEqualTo(0);
        assertThat(c.remainingAt(0)).isEqualTo(1);
        assertThat(c.fraction()).isEqualTo(0f);
    }
    @Test public void countingOneCountItem_completesAndAdvances() {
        BubbleContentController c = make();
        assertThat(c.countCurrent()).isTrue();      // a 1->0, completed
        assertThat(c.doneCount()).isEqualTo(1);
        assertThat(c.currentIndex()).isEqualTo(1);  // auto-advanced to b
    }
    @Test public void multiCount_needsAllTaps() {
        BubbleContentController c = make();
        c.countCurrent();                            // finish a
        assertThat(c.countCurrent()).isFalse();      // b 3->2
        assertThat(c.countCurrent()).isFalse();      // b 2->1
        assertThat(c.countCurrent()).isTrue();       // b 1->0 done
        assertThat(c.currentIndex()).isEqualTo(2);
    }
    @Test public void fractionAndAllDone() {
        BubbleContentController c = make();
        c.jumpTo(2);
        for (int i = 0; i < 100; i++) c.countCurrent();
        assertThat(c.remainingAt(2)).isEqualTo(0);
        assertThat(c.fraction()).isWithin(0.001f).of(1f / 3f);
        assertThat(c.isAllDone()).isFalse();
    }
    @Test public void countAt_targetsSpecificIndex() {
        BubbleContentController c = make();
        assertThat(c.countAt(2)).isFalse();          // c 100->99
        assertThat(c.remainingAt(2)).isEqualTo(99);
        assertThat(c.currentIndex()).isEqualTo(0);   // pointer unchanged
    }
}
