// app/src/main/java/com/medoapps/www/onlinequran/bubble/BubbleOverlayController.java
package com.medoapps.www.onlinequran.bubble;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

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
        loadSession();
        addBubble();
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
        bubbleView = LayoutInflater.from(service).inflate(R.layout.bubble_chathead, null);
        bubbleLp = baseParams(dp(62), dp(62));
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        bubbleLp.x = "left".equals(prefs.getSide()) ? dp(8) : screenW() - dp(70);
        bubbleLp.y = prefs.getPosY(dp(220));
        renderCollapsed();
        attachDrag(bubbleView);
        wm.addView(bubbleView, bubbleLp);
    }

    private void renderCollapsed() {
        ((TextView) bubbleView.findViewById(R.id.bubble_glyph))
                .setText(session == BubbleSession.MORNING ? "☀" : "☾");
        int rem = content.remainingAt(content.currentIndex());
        ((TextView) bubbleView.findViewById(R.id.bubble_badge)).setText(rem <= 0 ? "✓" : String.valueOf(rem));
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
                        if (!moved && System.currentTimeMillis() - downT < 250) { togglePanel(); return true; }
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

    private void showPanel() {
        panelView = LayoutInflater.from(service).inflate(R.layout.bubble_panel_walker, null);
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
