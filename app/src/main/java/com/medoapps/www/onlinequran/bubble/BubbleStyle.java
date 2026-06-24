package com.medoapps.www.onlinequran.bubble;

public enum BubbleStyle {
    CHAT_HEAD("A"), DRAWER("C"), PILL("D");
    private final String code;
    BubbleStyle(String code) { this.code = code; }
    public String code() { return code; }
    public static BubbleStyle fromCode(String code) {
        if (code != null) for (BubbleStyle s : values()) if (s.code.equals(code)) return s;
        return CHAT_HEAD;
    }
}
