package com.medoapps.www.onlinequran.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view that draws a gold countdown ring with a centered two-line text label.
 *
 * Usage:
 *   view.setProgress(fraction);              // 0..1 elapsed
 *   view.setCenterText("2:14:30", "باقي");  // big countdown, small label
 */
public class PrayerCountdownRingView extends View {

    // Gold track colour (faint)
    private static final int COLOR_TRACK = 0x33D4A44C;
    // Gold sweep colours
    private static final int COLOR_GOLD_START = 0xFFD4A44C;
    private static final int COLOR_GOLD_END   = 0xFFB8860B;
    private static final int COLOR_WHITE      = 0xFFFFFFFF;

    private static final int DEFAULT_SIZE_DP = 96;

    private final Paint trackPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sweepPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bigTextPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalRect = new RectF();

    private float progress = 0f;   // 0..1
    private String bigText   = "";
    private String smallText = "";

    // ---------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------

    public PrayerCountdownRingView(Context context) {
        this(context, null);
    }

    public PrayerCountdownRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrayerCountdownRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ---------------------------------------------------------------------------
    // Init
    // ---------------------------------------------------------------------------

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        float strokeWidth = 6f * density;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(COLOR_TRACK);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        sweepPaint.setStyle(Paint.Style.STROKE);
        sweepPaint.setStrokeWidth(strokeWidth);
        sweepPaint.setStrokeCap(Paint.Cap.ROUND);
        // Gradient is applied in onSizeChanged when we know dimensions.

        float smallSp = 12f * density;
        float bigSp   = 20f * density;

        smallTextPaint.setColor(COLOR_WHITE);
        smallTextPaint.setTextSize(smallSp);
        smallTextPaint.setTextAlign(Paint.Align.CENTER);

        bigTextPaint.setColor(COLOR_WHITE);
        bigTextPaint.setTextSize(bigSp);
        bigTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    /** Set the sweep progress (elapsed fraction). Clamped to [0,1]. */
    public void setProgress(float fraction) {
        progress = Math.max(0f, Math.min(1f, fraction));
        invalidate();
    }

    /**
     * Set the two-line centre text.
     *
     * @param big   The large value shown below, e.g. "2:14:30".
     * @param small The small label shown above, e.g. "باقي".
     */
    public void setCenterText(String big, String small) {
        bigText   = big   != null ? big   : "";
        smallText = small != null ? small : "";
        invalidate();
    }

    // ---------------------------------------------------------------------------
    // Measure
    // ---------------------------------------------------------------------------

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        float density = getResources().getDisplayMetrics().density;
        int defaultPx = (int) (DEFAULT_SIZE_DP * density);

        int w = resolveSize(defaultPx, widthSpec);
        int h = resolveSize(defaultPx, heightSpec);
        setMeasuredDimension(w, h);
    }

    // ---------------------------------------------------------------------------
    // Size change — rebuild gradient
    // ---------------------------------------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rebuildGradient(w, h);
        updateOvalRect(w, h);
    }

    private void rebuildGradient(int w, int h) {
        // Sweep from top-left to bottom-right for a visible gradient effect.
        sweepPaint.setShader(new LinearGradient(
                0, 0, w, h,
                COLOR_GOLD_START, COLOR_GOLD_END,
                Shader.TileMode.CLAMP));
    }

    private void updateOvalRect(int w, int h) {
        float density = getResources().getDisplayMetrics().density;
        float inset = (6f * density) / 2f + 1f * density; // half stroke + 1dp margin
        ovalRect.set(inset, inset, w - inset, h - inset);
    }

    // ---------------------------------------------------------------------------
    // Draw
    // ---------------------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Faint track (full circle)
        canvas.drawArc(ovalRect, -90f, 360f, false, trackPaint);

        // 2. Gold sweep arc (from top, clockwise)
        float sweep = progress * 360f;
        if (sweep > 0f) {
            canvas.drawArc(ovalRect, -90f, sweep, false, sweepPaint);
        }

        // 3. Centre text
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        float bigAscent  = bigTextPaint.ascent();
        float bigDescent = bigTextPaint.descent();
        float bigHeight  = bigDescent - bigAscent;

        float smallAscent  = smallTextPaint.ascent();
        float smallDescent = smallTextPaint.descent();
        float smallHeight  = smallDescent - smallAscent;

        float gap       = 4f * getResources().getDisplayMetrics().density;
        float totalH    = smallHeight + gap + bigHeight;
        float topOffset = cy - totalH / 2f;

        // Small label (e.g. "باقي") above
        float smallBaseline = topOffset - smallAscent; // ascent is negative
        canvas.drawText(smallText, cx, smallBaseline, smallTextPaint);

        // Big countdown (e.g. "2:14:30") below
        float bigBaseline = smallBaseline + smallDescent + gap - bigAscent;
        canvas.drawText(bigText, cx, bigBaseline, bigTextPaint);
    }
}
