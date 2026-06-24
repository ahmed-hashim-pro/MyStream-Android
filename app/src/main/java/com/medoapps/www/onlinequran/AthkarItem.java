package com.medoapps.www.onlinequran;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One athkar entry (header or counted dhikr). Moved out of AthkarActivity so the
 *  full Athkar screen, the Stories feature, and the floating bubble share one model. */
public class AthkarItem {
    public String text;
    public String count;
    public boolean isHeader;
    public int remainingCount;
    public boolean expanded = true; // for headers only
    public List<AthkarItem> children; // for headers only

    public AthkarItem(String text, boolean isHeader) {
        this.text = text;
        this.isHeader = isHeader;
        this.remainingCount = 0;
        if (isHeader) this.children = new ArrayList<>();
    }

    public AthkarItem(String text, String count, boolean isHeader) {
        this.text = text;
        this.count = count;
        this.isHeader = isHeader;
        this.remainingCount = parseCount(count);
    }

    public static int parseCount(String countStr) {
        if (countStr == null || countStr.isEmpty()) return 1;
        if (countStr.contains("واحدة")) return 1;
        Matcher matcher = Pattern.compile("\\d+").matcher(countStr);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 1;
    }
}
