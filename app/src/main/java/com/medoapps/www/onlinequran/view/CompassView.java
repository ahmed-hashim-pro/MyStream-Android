package com.medoapps.www.onlinequran.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.medoapps.www.onlinequran.R;

public class CompassView extends View {

    private Paint circlePaint;
    private Paint tickPaint;
    private Paint tickMinorPaint;
    private Paint textPaint;
    private Paint northPaint;
    private Paint degreePaint;
    private float rotationAngle = 0f;
    private int accentColor;
    private int circleColor;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        accentColor = ContextCompat.getColor(getContext(), R.color.gold_accent);
        circleColor = accentColor;

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(3f * getResources().getDisplayMetrics().density);
        circlePaint.setColor(circleColor);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(2.5f * getResources().getDisplayMetrics().density);
        tickPaint.setColor(Color.WHITE);

        tickMinorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickMinorPaint.setStyle(Paint.Style.STROKE);
        tickMinorPaint.setStrokeWidth(1f * getResources().getDisplayMetrics().density);
        tickMinorPaint.setColor(Color.argb(100, 255, 255, 255));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        northPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        northPaint.setTextAlign(Paint.Align.CENTER);
        northPaint.setColor(Color.parseColor("#FF5252"));
        northPaint.setTypeface(Typeface.DEFAULT_BOLD);

        degreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        degreePaint.setTextAlign(Paint.Align.CENTER);
        degreePaint.setColor(Color.argb(150, 255, 255, 255));
    }

    public void setCompassRotation(float angle) {
        this.rotationAngle = angle;
        invalidate();
    }

    public void setCircleColor(int color) {
        circleColor = color;
        circlePaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float density = getResources().getDisplayMetrics().density;
        float radius = Math.min(cx, cy) - 4 * density;

        canvas.save();
        canvas.rotate(rotationAngle, cx, cy);

        // Outer circle
        canvas.drawCircle(cx, cy, radius, circlePaint);

        // Inner circle (subtle)
        Paint innerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCircle.setStyle(Paint.Style.STROKE);
        innerCircle.setStrokeWidth(1f * density);
        innerCircle.setColor(Color.argb(40, 255, 255, 255));
        canvas.drawCircle(cx, cy, radius * 0.75f, innerCircle);

        float textSize = radius * 0.18f;
        textPaint.setTextSize(textSize);
        northPaint.setTextSize(textSize);
        degreePaint.setTextSize(radius * 0.1f);

        // Draw tick marks and labels
        for (int i = 0; i < 360; i += 5) {
            canvas.save();
            canvas.rotate(i, cx, cy);

            if (i % 30 == 0) {
                // Major tick
                float tickLen = radius * 0.12f;
                canvas.drawLine(cx, cy - radius + 1, cx, cy - radius + tickLen, tickPaint);

                // Cardinal / intercardinal labels
                canvas.save();
                canvas.translate(cx, cy - radius + tickLen + textSize * 0.9f);
                canvas.rotate(-i - rotationAngle);

                String label = getLabelForDegree(i);
                if (label != null) {
                    Paint p = (i == 0) ? northPaint : textPaint;
                    canvas.drawText(label, 0, 0, p);
                } else {
                    // Show degree number for 30° intervals without a label
                    canvas.drawText(String.valueOf(i), 0, 0, degreePaint);
                }
                canvas.restore();
            } else if (i % 10 == 0) {
                // Medium tick
                float tickLen = radius * 0.08f;
                canvas.drawLine(cx, cy - radius + 1, cx, cy - radius + tickLen, tickMinorPaint);
            } else {
                // Minor tick
                float tickLen = radius * 0.04f;
                Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                dotPaint.setColor(Color.argb(50, 255, 255, 255));
                dotPaint.setStrokeWidth(1f * density);
                canvas.drawLine(cx, cy - radius + 1, cx, cy - radius + tickLen, dotPaint);
            }

            canvas.restore();
        }

        canvas.restore();
    }

    private String getLabelForDegree(int degree) {
        switch (degree) {
            case 0: return "N";
            case 90: return "E";
            case 180: return "S";
            case 270: return "W";
            case 30: return "30";
            case 60: return "60";
            case 120: return "120";
            case 150: return "150";
            case 210: return "210";
            case 240: return "240";
            case 300: return "300";
            case 330: return "330";
            default: return null;
        }
    }
}
