package com.medoapps.www.onlinequran.bubble;

import com.medoapps.www.onlinequran.AthkarItem;
import java.util.List;

/** Pure model for a guided athkar session: remaining counts per dhikr, current pointer,
 *  progress fraction. No Android dependencies so it is unit-tested on the JVM. */
public class BubbleContentController {
    private final List<AthkarItem> items;
    private final int[] remaining;
    private int index;

    public BubbleContentController(List<AthkarItem> items) {
        this.items = items;
        this.remaining = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            remaining[i] = Math.max(1, items.get(i).remainingCount);
        }
        this.index = firstUnfinished();
    }

    private int firstUnfinished() {
        for (int i = 0; i < remaining.length; i++) if (remaining[i] > 0) return i;
        return remaining.length - 1;
    }

    public int size() { return items.size(); }
    public int currentIndex() { return index; }
    public AthkarItem currentItem() { return items.get(index); }
    public int remainingAt(int i) { return remaining[i]; }
    public int targetAt(int i) { return Math.max(1, items.get(i).remainingCount); }

    public int doneCount() {
        int n = 0;
        for (int r : remaining) if (r <= 0) n++;
        return n;
    }
    public float fraction() { return remaining.length == 0 ? 0f : (float) doneCount() / remaining.length; }
    public boolean isAllDone() { return doneCount() == remaining.length; }

    /** Decrement the current dhikr; returns true iff it just reached 0. Auto-advances on completion. */
    public boolean countCurrent() {
        if (remaining[index] > 0) remaining[index]--;
        if (remaining[index] == 0) {
            int next = firstUnfinished();
            boolean completed = true;
            if (next != index) index = next;
            return completed;
        }
        return false;
    }

    public void jumpTo(int i) { if (i >= 0 && i < remaining.length) index = i; }

    public AthkarItem currentItemAt(int i) { return items.get(i); }

    /** Decrement dhikr i directly (drawer tapping); returns true iff it just hit 0. */
    public boolean countAt(int i) {
        if (remaining[i] > 0) remaining[i]--;
        return remaining[i] == 0;
    }
}
