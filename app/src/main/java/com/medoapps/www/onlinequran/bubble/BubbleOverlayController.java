// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleOverlayController.java
package com.medoapps.www.onlinequran.bubble;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.AthkarItem;
import com.medoapps.www.onlinequran.AthkarProgressStore;
import com.medoapps.www.onlinequran.AthkarRepository;
import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.athan.PrayerSettings;
import com.medoapps.www.onlinequran.athan.PrayerTimeEngine;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Owns the floating overlay view: add/remove, drag, snap-to-edge,
 *  and (style A) the tap-to-expand walker panel. Styles C/D extend renderExpanded(). */
public class BubbleOverlayController {
    private final Service service;
    private final WindowManager wm;
    private final BubblePrefs prefs;
    private final AthkarProgressStore progress;
    private final Vibrator vibrator;

    private View bubbleView;            // collapsed chat-head
    private View panelView;             // expanded walker (style A)
    private WindowManager.LayoutParams bubbleLp;
    private BubbleSession session;
    private BubbleContentController content;
    private int dayOfYear;
    private Context inflationContext;   // themed context used for all overlay inflations

    public BubbleOverlayController(Service service) {
        this.service = service;
        this.wm = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.prefs = new BubblePrefs(service);
        this.progress = new AthkarProgressStore(service);
        this.vibrator = (Vibrator) service.getSystemService(Context.VIBRATOR_SERVICE);
    }

    static int overlayType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    public void show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(service)) return;
        if (bubbleView != null) return;
        inflationContext = themedContext();
        loadSession();
        addBubble();
    }

    /**
     * Returns a Context whose Configuration reflects the app's chosen night-mode setting,
     * so overlays inflated from a Service (which has no Activity theme) resolve
     * theme-dependent colors correctly on cold-start (e.g. launched from an alarm receiver).
     *
     * Only overrides when the user has picked an explicit light or dark mode; when the app
     * follows the system setting (or the pref is absent) the service context is returned
     * unchanged so the device uiMode continues to govern — same behaviour as before.
     */
    private Context themedContext() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(service);
        int savedMode = prefs.getInt("currentThemeMode", -1);
        Context base;
        if (savedMode == AppCompatDelegate.MODE_NIGHT_YES) {
            int nightBits = Configuration.UI_MODE_NIGHT_YES;
            Configuration cfg = new Configuration(service.getResources().getConfiguration());
            cfg.uiMode = (cfg.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightBits;
            base = service.createConfigurationContext(cfg);
        } else if (savedMode == AppCompatDelegate.MODE_NIGHT_NO) {
            int nightBits = Configuration.UI_MODE_NIGHT_NO;
            Configuration cfg = new Configuration(service.getResources().getConfiguration());
            cfg.uiMode = (cfg.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightBits;
            base = service.createConfigurationContext(cfg);
        } else {
            // Follow-system or unknown — let the device decide.
            base = service;
        }
        // Wrap with the app theme so ?attr/... references (e.g. selectableItemBackground)
        // resolve correctly when inflating overlays from a Service context.
        return new android.view.ContextThemeWrapper(base, R.style.AppTheme);
    }

    public void hide() {
        removePanel();
        if (bubbleView != null) { try { wm.removeView(bubbleView); } catch (Exception ignored) {} bubbleView = null; }
    }

    private void loadSession() {
        dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        long now = System.currentTimeMillis();
        long fajr = now, asr = now;
        try {
            Date[] t = PrayerTimeEngine.getTodayTimes(service);
            if (t != null) { fajr = t[PrayerSettings.PRAYER_FAJR].getTime(); asr = t[PrayerSettings.PRAYER_ASR].getTime(); }
        } catch (Exception ignored) {}
        session = BubbleSessionSelector.select(now, fajr, asr);
        List<AthkarItem> items = (session == BubbleSession.MORNING)
                ? AthkarRepository.getMorningItems() : AthkarRepository.getEveningItems();
        content = new BubbleContentController(items);
    }

    private void addBubble() {
        BubbleStyle style = prefs.getStyle();
        int layout = style == BubbleStyle.PILL ? R.layout.bubble_pill : R.layout.bubble_chathead;
        int w = style == BubbleStyle.PILL ? dp(240) : dp(62);
        bubbleView = LayoutInflater.from(inflationContext).inflate(layout, null);
        bubbleLp = baseParams(w, dp(62));
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        bubbleLp.x = "left".equals(prefs.getSide()) ? dp(8) : screenW() - w - dp(8);
        bubbleLp.y = prefs.getPosY(dp(220));
        renderCollapsed();
        attachDrag(bubbleView);
        // pill has its own count + list buttons:
        View count = bubbleView.findViewById(R.id.pill_count);
        if (count != null) count.setOnClickListener(x -> onCount());
        View list = bubbleView.findViewById(R.id.pill_list);
        if (list != null) list.setOnClickListener(x -> togglePanel());
        wm.addView(bubbleView, bubbleLp);
    }

    private void renderCollapsed() {
        int rem = content.remainingAt(content.currentIndex());
        String glyph = session == BubbleSession.MORNING ? "☀" : "☾";
        TextView g = bubbleView.findViewById(R.id.bubble_glyph);
        if (g != null) { // chat-head
            g.setText(glyph);
            ((TextView) bubbleView.findViewById(R.id.bubble_badge)).setText(rem <= 0 ? "✓" : String.valueOf(rem));
        }
        TextView pg = bubbleView.findViewById(R.id.pill_glyph);
        if (pg != null) { // pill
            pg.setText(glyph);
            ((TextView) bubbleView.findViewById(R.id.pill_dhikr)).setText(content.currentItem().text);
            ((TextView) bubbleView.findViewById(R.id.pill_meta))
                    .setText(content.currentItem().count + " · " + (rem <= 0 ? "✓" : rem));
            ((TextView) bubbleView.findViewById(R.id.pill_count)).setText(rem <= 0 ? "✓" : "+");
        }
    }

    // --- drag + tap (Correction A: cleaner move detection with touch-slop) ---
    private void attachDrag(final View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY; float downRawX, downRawY; long downT; boolean moved;
            @Override public boolean onTouch(View view, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = bubbleLp.x; startY = bubbleLp.y;
                        downRawX = e.getRawX(); downRawY = e.getRawY();
                        downT = System.currentTimeMillis(); moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - downRawX);
                        int dy = (int) (e.getRawY() - downRawY);
                        if (Math.hypot(dx, dy) > dp(8)) moved = true;
                        int w = v.getWidth() > 0 ? v.getWidth() : dp(62);
                        bubbleLp.x = clamp(startX + dx, 0, screenW() - w);
                        bubbleLp.y = clamp(startY + dy, dp(40), screenH() - dp(80));
                        wm.updateViewLayout(bubbleView, bubbleLp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved && System.currentTimeMillis() - downT < 250) {
                            if (prefs.getStyle() == BubbleStyle.DRAWER) {
                                if (panelView == null) showDrawer(); else removePanel();
                            } else {
                                togglePanel();
                            }
                            return true;
                        }
                        int w2 = v.getWidth() > 0 ? v.getWidth() : dp(62);
                        boolean left = bubbleLp.x + w2 / 2 < screenW() / 2;
                        bubbleLp.x = left ? dp(8) : screenW() - w2 - dp(8);
                        prefs.setSide(left ? "left" : "right");
                        prefs.setPosY(bubbleLp.y);
                        wm.updateViewLayout(bubbleView, bubbleLp);
                        return true;
                }
                return false;
            }
        });
    }

    private void togglePanel() { if (panelView == null) showPanel(); else removePanel(); }

    private void showDrawer() {
        panelView = LayoutInflater.from(inflationContext).inflate(R.layout.bubble_drawer, null);
        boolean left = service.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        WindowManager.LayoutParams lp = baseParams(dp(236), WindowManager.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.TOP | (left ? Gravity.START : Gravity.END);
        ((TextView) panelView.findViewById(R.id.drawer_title))
                .setText(session == BubbleSession.MORNING ? R.string.athkar_section_morning : R.string.athkar_section_evening);
        ((TextView) panelView.findViewById(R.id.drawer_sub))
                .setText(service.getString(R.string.bubble_today_done, content.doneCount(), content.size()));
        panelView.findViewById(R.id.drawer_back).setOnClickListener(x -> removePanel());
        android.widget.LinearLayout listView = panelView.findViewById(R.id.drawer_list);
        for (int i = 0; i < content.size(); i++) {
            final int idx = i;
            View row = LayoutInflater.from(inflationContext).inflate(R.layout.bubble_drawer_row, listView, false);
            ((TextView) row.findViewById(R.id.row_text)).setText(content.currentItemAt(idx).text);
            TextView c = row.findViewById(R.id.row_count);
            c.setText(content.remainingAt(idx) <= 0 ? "✓" : String.valueOf(content.remainingAt(idx)));
            row.setOnClickListener(v -> {
                boolean done = content.countAt(idx);
                haptic();
                if (done) progress.markDone(dayOfYear, session.name(), idx);
                c.setText(content.remainingAt(idx) <= 0 ? "✓" : String.valueOf(content.remainingAt(idx)));
                renderCollapsed();
            });
            listView.addView(row);
        }
        wm.addView(panelView, lp);
    }

    private void showPanel() {
        panelView = LayoutInflater.from(inflationContext).inflate(R.layout.bubble_panel_walker, null);
        WindowManager.LayoutParams lp = baseParams(dp(280), WindowManager.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | ("left".equals(prefs.getSide()) ? Gravity.START : Gravity.END);
        lp.x = dp(12);
        lp.y = clamp(bubbleLp.y - dp(120), dp(40), screenH() - dp(420));
        bindPanel();
        panelView.findViewById(R.id.walker_close).setOnClickListener(x -> removePanel());
        panelView.findViewById(R.id.walker_prev).setOnClickListener(x -> { content.jumpTo(content.currentIndex() - 1); bindPanel(); });
        panelView.findViewById(R.id.walker_next).setOnClickListener(x -> { content.jumpTo(content.currentIndex() + 1); bindPanel(); });
        panelView.findViewById(R.id.walker_count).setOnClickListener(x -> onCount());
        wm.addView(panelView, lp);
    }

    private void onCount() {
        int idx = content.currentIndex();
        boolean completed = content.countCurrent();
        haptic();
        if (completed) progress.markDone(dayOfYear, session.name(), idx);
        renderCollapsed();
        bindPanel();
    }

    private void bindPanel() {
        if (panelView == null) return;
        AthkarItem it = content.currentItem();
        int rem = content.remainingAt(content.currentIndex());
        ((TextView) panelView.findViewById(R.id.walker_title))
                .setText(session == BubbleSession.MORNING ? R.string.athkar_section_morning : R.string.athkar_section_evening);
        ((TextView) panelView.findViewById(R.id.walker_sub))
                .setText((content.currentIndex() + 1) + "/" + content.size());
        ((TextView) panelView.findViewById(R.id.walker_dhikr)).setText(it.text);
        ((TextView) panelView.findViewById(R.id.walker_ref)).setText(it.count);
        // Correction B: set text + fixed white-on-navy color so it stays legible in both themes
        TextView countTv = panelView.findViewById(R.id.walker_count);
        countTv.setText(rem <= 0 ? "✓" : String.valueOf(rem));
        countTv.setTextColor(ContextCompat.getColor(service, R.color.text_on_navy));
        ((TextView) panelView.findViewById(R.id.walker_today))
                .setText(service.getString(R.string.bubble_today_done, content.doneCount(), content.size()));
    }

    private void removePanel() {
        if (panelView != null) { try { wm.removeView(panelView); } catch (Exception ignored) {} panelView = null; }
    }

    private void haptic() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
        else vibrator.vibrate(20);
    }

    // --- window helpers ---
    private WindowManager.LayoutParams baseParams(int w, int h) {
        return new WindowManager.LayoutParams(w, h, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
    }
    private int dp(int v) { return Math.round(v * service.getResources().getDisplayMetrics().density); }
    private int screenW() { return service.getResources().getDisplayMetrics().widthPixels; }
    private int screenH() { return service.getResources().getDisplayMetrics().heightPixels; }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}
