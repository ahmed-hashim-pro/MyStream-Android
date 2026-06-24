package com.medoapps.www.onlinequran.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Bayt al-Noor (A) countdown ring: a FILLED gold sector sweeping over a faint
 * white remainder, with a navy inner disc punched out of the middle — i.e. a
 * gold/white progress band around a navy hole. The 3-line centre text
 * (label · prayer name · countdown) is composed as TextViews layered on top in
 * the layout, so this view draws geometry only.
 *
 * Usage:
 *   view.setProgress(fraction);   // 0..1 elapsed since the previous prayer
 */
public class PrayerCountdownRingView extends View {

    // Faint white remainder (A: rgba(255,255,255,.12))
    private static final int COLOR_REMAINDER = 0x1FFFFFFF;
    // Solid gold sweep (A conic #D4A44C)
    private static final int COLOR_GOLD = 0xFFD4A44C;
    // Navy inner disc radial stops (A radial #243150 -> #1b2740)
    private static final int COLOR_NAVY_TOP = 0xFF243150;
    private static final int COLOR_NAVY_BOTTOM = 0xFF1B2740;

    private static final int DEFAULT_SIZE_DP = 128;
    /** Inner navy disc radius as a fraction of the outer radius (A band ≈ 14%). */
    private static final float INNER_RADIUS_RATIO = 0.85f;

    private final Paint remainderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint navyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalRect = new RectF();

    private float progress = 0f;   // 0..1
    private float cx, cy, innerRadius;

    public PrayerCountdownRingView(Context context) {
        this(context, null);
    }

    public PrayerCountdownRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrayerCountdownRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        remainderPaint.setStyle(Paint.Style.FILL);
        remainderPaint.setColor(COLOR_REMAINDER);
        goldPaint.setStyle(Paint.Style.FILL);
        goldPaint.setColor(COLOR_GOLD);
        navyPaint.setStyle(Paint.Style.FILL);
    }

    /** Returns the current sweep progress (0..1). */
    public float getProgress() {
        return progress;
    }

    /** Set the sweep progress (elapsed fraction). Clamped to [0,1]. */
    public void setProgress(float fraction) {
        progress = Math.max(0f, Math.min(1f, fraction));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        float density = getResources().getDisplayMetrics().density;
        int defaultPx = (int) (DEFAULT_SIZE_DP * density);
        int w = resolveSize(defaultPx, widthSpec);
        int h = resolveSize(defaultPx, heightSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float margin = getResources().getDisplayMetrics().density; // 1dp
        ovalRect.set(margin, margin, w - margin, h - margin);
        cx = w / 2f;
        cy = h / 2f;
        float outerRadius = (Math.min(w, h) / 2f) - margin;
        innerRadius = outerRadius * INNER_RADIUS_RATIO;
        // Navy radial highlight centred slightly above middle (A: 50% 35%).
        navyPaint.setShader(new RadialGradient(
                cx, cy - outerRadius * 0.15f, innerRadius,
                COLOR_NAVY_TOP, COLOR_NAVY_BOTTOM, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ovalRect.isEmpty()) return;

        // 1. Faint white remainder fills the whole disc.
        canvas.drawArc(ovalRect, -90f, 360f, true, remainderPaint);

        // 2. Gold sector from the top, clockwise, for the elapsed fraction.
        float sweep = progress * 360f;
        if (sweep > 0f) {
            canvas.drawArc(ovalRect, -90f, sweep, true, goldPaint);
        }

        // 3. Navy inner disc punches the band into a ring.
        canvas.drawCircle(cx, cy, innerRadius, navyPaint);
    }
}
