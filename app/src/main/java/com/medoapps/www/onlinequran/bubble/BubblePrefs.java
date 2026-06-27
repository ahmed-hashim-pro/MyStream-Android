package com.medoapps.www.onlinequran.bubble;

import android.content.Context;
import android.content.SharedPreferences;

/** Persisted bubble settings: on/off, style, show-mode, last on-screen position. */
public class BubblePrefs {
    public static final String PREFS = "bubble_prefs";
    private final SharedPreferences p;
    public BubblePrefs(Context c) { p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }

    public boolean isEnabled() { return p.getBoolean("bubble_enabled", false); }
    public void setEnabled(boolean v) { p.edit().putBoolean("bubble_enabled", v).apply(); }

    public BubbleStyle getStyle() { return BubbleStyle.fromCode(p.getString("bubble_style", "A")); }
    public void setStyle(BubbleStyle s) { p.edit().putString("bubble_style", s.code()).apply(); }

    /** true = always on; false = only during the day/night windows (default). */
    public boolean isAlwaysOn() { return p.getBoolean("bubble_always", false); }
    public void setAlwaysOn(boolean v) { p.edit().putBoolean("bubble_always", v).apply(); }

    /** "right" (default) or "left". */
    public String getSide() { return p.getString("bubble_side", "right"); }
    public void setSide(String s) { p.edit().putString("bubble_side", s).apply(); }

    public int getPosX(int def) { return p.getInt("bubble_pos_x", def); }
    public void setPosX(int x) { p.edit().putInt("bubble_pos_x", x).apply(); }

    public int getPosY(int def) { return p.getInt("bubble_pos_y", def); }
    public void setPosY(int y) { p.edit().putInt("bubble_pos_y", y).apply(); }

    /** Epoch millis until which the bubble stays hidden after the user reads/dismisses it
     *  ("Done ✓" or hold-to-close); 0 = not dismissed. Revived by the Fajr/Asr boundary alarm. */
    public long getDismissUntil() { return p.getLong("bubble_dismiss_until", 0L); }
    public void setDismissUntil(long t) { p.edit().putLong("bubble_dismiss_until", t).apply(); }

    /** Play a short sound when the bubble appears (athkar time / when enabled). Default on. */
    public boolean isSoundOn() { return p.getBoolean("bubble_sound", true); }
    public void setSoundOn(boolean v) { p.edit().putBoolean("bubble_sound", v).apply(); }
}
