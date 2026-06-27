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
    private WindowManager.LayoutParams panelLp;
    private BubbleSession session;
    private BubbleContentController content;
    private int dayOfYear;
    private Context inflationContext;   // themed context used for all overlay inflations
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private View closeTargetView;       // Messenger-style "drop to close" target (shown on long-press)
    private boolean overCloseTarget;    // is the bubble currently hovering the close target?

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
        handler.removeCallbacksAndMessages(null);
        hideCloseTarget();
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
        bubbleLp.x = clamp(prefs.getPosX(screenW() - w - dp(8)), 0, screenW() - w);
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

    // --- drag + tap + long-press-to-close (Messenger-style) ---
    private void attachDrag(final View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY; float downRawX, downRawY; long downT; boolean moved; boolean closeArmed;
            // After a 450ms hold (without a real drag) the close target appears — like Messenger.
            final Runnable longPress = () -> { closeArmed = true; showCloseTarget(); haptic(); updateOverCloseTarget(); };
            @Override public boolean onTouch(View view, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = bubbleLp.x; startY = bubbleLp.y;
                        downRawX = e.getRawX(); downRawY = e.getRawY();
                        downT = System.currentTimeMillis(); moved = false; closeArmed = false;
                        handler.postDelayed(longPress, 450);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - downRawX);
                        int dy = (int) (e.getRawY() - downRawY);
                        if (Math.hypot(dx, dy) > dp(8)) {
                            moved = true;
                            if (!closeArmed) handler.removeCallbacks(longPress); // it's a drag, not a hold
                        }
                        int w = v.getWidth() > 0 ? v.getWidth() : dp(62);
                        bubbleLp.x = clamp(startX + dx, 0, screenW() - w);
                        bubbleLp.y = clamp(startY + dy, dp(40), screenH() - dp(80));
                        wm.updateViewLayout(bubbleView, bubbleLp);
                        if (closeArmed) updateOverCloseTarget();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(longPress);
                        int w2 = v.getWidth() > 0 ? v.getWidth() : dp(62);
                        if (closeArmed) {
                            boolean over = overCloseTarget;
                            hideCloseTarget();
                            closeArmed = false;
                            if (over && e.getActionMasked() == MotionEvent.ACTION_UP) { dismiss(); return true; }
                            // Released without dropping on the target: keep the bubble where it is.
                            prefs.setPosX(bubbleLp.x); prefs.setPosY(bubbleLp.y);
                            prefs.setSide(bubbleLp.x + w2 / 2 < screenW() / 2 ? "left" : "right");
                            return true;
                        }
                        if (!moved && System.currentTimeMillis() - downT < 250) {
                            if (prefs.getStyle() == BubbleStyle.DRAWER) {
                                if (panelView == null) showDrawer(); else removePanel();
                            } else {
                                togglePanel();
                            }
                            return true;
                        }
                        // Free placement: leave the bubble where it was dropped and remember it.
                        prefs.setPosX(bubbleLp.x);
                        prefs.setPosY(bubbleLp.y);
                        prefs.setSide(bubbleLp.x + w2 / 2 < screenW() / 2 ? "left" : "right");
                        return true;
                }
                return false;
            }
        });
    }

    // --- Messenger-style hold-to-close target ---
    private void showCloseTarget() {
        if (closeTargetView != null) return;
        overCloseTarget = false;
        TextView x = new TextView(inflationContext);
        x.setText("✕");
        x.setGravity(Gravity.CENTER);
        x.setTextSize(24); // sp
        x.setTextColor(ContextCompat.getColor(inflationContext, R.color.gold_accent)); // idle: gold ✕
        x.setBackgroundResource(R.drawable.bubble_close_target);                        // idle: navy core, gold ring
        WindowManager.LayoutParams lp = baseParams(dp(84), dp(84));
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.y = dp(80);
        closeTargetView = x;
        try { wm.addView(x, lp); } catch (Exception ignored) { closeTargetView = null; }
    }

    private void hideCloseTarget() {
        overCloseTarget = false;
        if (closeTargetView != null) { try { wm.removeView(closeTargetView); } catch (Exception ignored) {} closeTargetView = null; }
    }

    /** Updates the "is the bubble over the close target" state + the target's hover highlight. */
    private void updateOverCloseTarget() {
        if (closeTargetView == null || bubbleView == null) return;
        int bw = bubbleView.getWidth() > 0 ? bubbleView.getWidth() : dp(62);
        int bh = bubbleView.getHeight() > 0 ? bubbleView.getHeight() : dp(62);
        int bcx = bubbleLp.x + bw / 2;
        int bcy = bubbleLp.y + bh / 2;
        int tcx = screenW() / 2;
        int tcy = screenH() - dp(80) - dp(42); // bottom gravity, y=80 offset, half of 84dp height
        boolean over = Math.hypot(bcx - tcx, bcy - tcy) < dp(72);
        if (over != overCloseTarget) {
            overCloseTarget = over;
            TextView tv = (TextView) closeTargetView;
            tv.setBackgroundResource(over ? R.drawable.bubble_close_target_armed : R.drawable.bubble_close_target);
            tv.setTextColor(ContextCompat.getColor(inflationContext,
                    over ? R.color.text_on_navy : R.color.gold_accent)); // armed: white ✕, idle: gold ✕
            if (over) haptic();
        }
    }

    /** User read the athkar ("Done ✓") or held-to-close: hide the bubble until the next athkar time. */
    private void dismiss() {
        haptic();
        try {
            android.widget.Toast.makeText(service, R.string.bubble_dismissed_toast,
                    android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
        BubbleScheduler.dismissUntilNextSession(service); // sets dismissUntil, stops the service, arms revive alarm
    }

    private void togglePanel() { if (panelView == null) showPanel(); else removePanel(); }

    /** Closes the open panel when the user taps anywhere outside it (needs FLAG_WATCH_OUTSIDE_TOUCH). */
    private void dismissOnOutsideTouch(View root) {
        root.setOnTouchListener((v, e) -> {
            if (e.getActionMasked() == MotionEvent.ACTION_OUTSIDE) { removePanel(); return true; }
            return false;
        });
    }

    /** Lets the user drag the open panel (and the bubble with it) by a handle, e.g. the header. */
    private void makePanelDraggable(View handle) {
        if (handle == null) return;
        handle.setOnTouchListener(new View.OnTouchListener() {
            int pStartX, pStartY, bStartX, bStartY; float downRawX, downRawY;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        pStartX = panelLp.x; pStartY = panelLp.y;
                        bStartX = bubbleLp.x; bStartY = bubbleLp.y;
                        downRawX = e.getRawX(); downRawY = e.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (e.getRawX() - downRawX);
                        int dy = (int) (e.getRawY() - downRawY);
                        int pw = panelView.getWidth() > 0 ? panelView.getWidth() : dp(280);
                        panelLp.x = clamp(pStartX + dx, 0, screenW() - pw);
                        panelLp.y = clamp(pStartY + dy, dp(8), screenH() - dp(120));
                        wm.updateViewLayout(panelView, panelLp);
                        int bw = bubbleView.getWidth() > 0 ? bubbleView.getWidth() : dp(62);
                        bubbleLp.x = clamp(bStartX + dx, 0, screenW() - bw);
                        bubbleLp.y = clamp(bStartY + dy, dp(40), screenH() - dp(80));
                        wm.updateViewLayout(bubbleView, bubbleLp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        prefs.setPosX(bubbleLp.x);
                        prefs.setPosY(bubbleLp.y);
                        return true;
                }
                return false;
            }
        });
    }

    private void showDrawer() {
        panelView = LayoutInflater.from(inflationContext).inflate(R.layout.bubble_drawer, null);
        int bw = (bubbleView != null && bubbleView.getWidth() > 0) ? bubbleView.getWidth() : dp(62);
        boolean left = bubbleLp.x + bw / 2 < screenW() / 2; // dock the drawer to the side the bubble is on
        WindowManager.LayoutParams lp = baseParams(dp(236), WindowManager.LayoutParams.MATCH_PARENT);
        lp.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
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
        View done = panelView.findViewById(R.id.drawer_done);
        if (done != null) done.setOnClickListener(x -> dismiss());
        dismissOnOutsideTouch(panelView);
        wm.addView(panelView, lp);
    }

    private void showPanel() {
        panelView = LayoutInflater.from(inflationContext).inflate(R.layout.bubble_panel_walker, null);
        int pw = dp(280);
        panelLp = baseParams(pw, WindowManager.LayoutParams.WRAP_CONTENT);
        panelLp.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        int bw = (bubbleView != null && bubbleView.getWidth() > 0) ? bubbleView.getWidth() : dp(62);
        // Open the panel next to the bubble's current position (not a fixed edge), clamped on-screen.
        int px = (bubbleLp.x + bw / 2 < screenW() / 2) ? bubbleLp.x : (bubbleLp.x + bw - pw);
        panelLp.gravity = Gravity.TOP | Gravity.START;
        panelLp.x = clamp(px, dp(8), screenW() - pw - dp(8));
        panelLp.y = clamp(bubbleLp.y - dp(40), dp(40), screenH() - dp(500));
        bindPanel();
        panelView.findViewById(R.id.walker_close).setOnClickListener(x -> removePanel());
        panelView.findViewById(R.id.walker_prev).setOnClickListener(x -> { content.jumpTo(content.currentIndex() - 1); bindPanel(); });
        panelView.findViewById(R.id.walker_next).setOnClickListener(x -> { content.jumpTo(content.currentIndex() + 1); bindPanel(); });
        panelView.findViewById(R.id.walker_count).setOnClickListener(x -> onCount());
        // Drag the open panel (and the bubble with it) by its header.
        makePanelDraggable(panelView.findViewById(R.id.walker_header));
        View done = panelView.findViewById(R.id.walker_done);
        if (done != null) done.setOnClickListener(x -> dismiss());
        dismissOnOutsideTouch(panelView);
        wm.addView(panelView, panelLp);
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
        TextView dhikrTv = panelView.findViewById(R.id.walker_dhikr);
        dhikrTv.setText(it.text);
        // Long athkar (e.g. Ayat al-Kursi) exceed the panel; make the dhikr scrollable so the full text is reachable.
        dhikrTv.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        dhikrTv.scrollTo(0, 0);
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
