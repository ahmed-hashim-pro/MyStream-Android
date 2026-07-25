package com.medoapps.www.onlinequran.more;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medoapps.www.onlinequran.R;

/**
 * Gaps for a mixed-span grid: tiles need a gutter between columns, full-span items
 * (context card, headers, rows) must not be inset or they stop lining up with the
 * list's own padding. No existing decoration handles both.
 */
public class MoreItemDecoration extends RecyclerView.ItemDecoration {

    private final MoreAdapter adapter;
    private final int gutter;
    private final int rowGap;
    private final Paint dividerPaint = new Paint();
    private final int dividerHeight;
    private final int dividerInset;

    public MoreItemDecoration(Context context, MoreAdapter adapter) {
        this.adapter = adapter;
        this.gutter = context.getResources().getDimensionPixelSize(R.dimen.spacing_sm);
        this.rowGap = context.getResources().getDimensionPixelSize(R.dimen.spacing_sm);
        this.dividerHeight = context.getResources().getDimensionPixelSize(R.dimen.more_hairline);
        // inset so the rule starts under the text, not under the icon chip
        this.dividerInset = context.getResources().getDimensionPixelSize(R.dimen.more_icon_chip_size)
                + context.getResources().getDimensionPixelSize(R.dimen.spacing_md) * 2;
        android.util.TypedValue tv = new android.util.TypedValue();
        context.getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorOutlineVariant, tv, true);
        dividerPaint.setColor(androidx.core.content.ContextCompat.getColor(context, tv.resourceId));
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION || !adapter.isTile(position)) {
            outRect.setEmpty();
            return;
        }
        // Symmetric half-gutters: every tile gets the same visual width, and the
        // grid's outer edge still aligns with the RecyclerView padding.
        outRect.left = gutter / 2;
        outRect.right = gutter / 2;
        outRect.bottom = rowGap;
    }

    /** Hairline between consecutive rows only — never after the last row of a group. */
    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        boolean rtl = parent.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos == RecyclerView.NO_POSITION || !adapter.isRow(pos)) continue;
            if (!adapter.isRow(pos + 1)) continue; // last row in its run
            float top = child.getBottom();
            float left = rtl ? parent.getPaddingLeft() : parent.getPaddingLeft() + dividerInset;
            float right = rtl ? parent.getWidth() - parent.getPaddingRight() - dividerInset
                              : parent.getWidth() - parent.getPaddingRight();
            canvas.drawRect(left, top, right, top + dividerHeight, dividerPaint);
        }
    }
}
