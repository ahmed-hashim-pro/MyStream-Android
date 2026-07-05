package com.medoapps.www.onlinequran.onboarding;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Java port of the ViewPager2 sample's NestedScrollableHost: lets a horizontal
 * child (the print carousel) scroll inside a horizontal ViewPager2 parent (the
 * welcome pager). The parent only gets the drag once the child can no longer
 * scroll in that direction.
 */
public class NestedScrollableHost extends FrameLayout {

    private int touchSlop;
    private float initialX;
    private float initialY;

    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Nullable
    private ViewPager2 findParentViewPager() {
        ViewParent parent = getParent();
        while (parent instanceof View && !(parent instanceof ViewPager2)) {
            parent = parent.getParent();
        }
        return parent instanceof ViewPager2 ? (ViewPager2) parent : null;
    }

    private boolean childCanScroll(int orientation, float delta) {
        View child = getChildCount() > 0 ? getChildAt(0) : null;
        if (child == null) {
            return false;
        }
        int direction = (int) -Math.signum(delta);
        return orientation == ViewPager2.ORIENTATION_HORIZONTAL
                ? child.canScrollHorizontally(direction)
                : child.canScrollVertically(direction);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private void handleInterceptTouchEvent(MotionEvent ev) {
        ViewPager2 parentPager = findParentViewPager();
        if (parentPager == null) {
            return;
        }
        int orientation = parentPager.getOrientation();

        // early return if the child can't scroll in the pager's direction at all
        if (!childCanScroll(orientation, -1f) && !childCanScroll(orientation, 1f)) {
            return;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = ev.getX();
            initialY = ev.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = ev.getX() - initialX;
            float dy = ev.getY() - initialY;
            boolean isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL;

            // assuming ViewPager2 touch-slop is 2x the child's
            float scaledDx = Math.abs(dx) * (isVpHorizontal ? 0.5f : 1f);
            float scaledDy = Math.abs(dy) * (isVpHorizontal ? 1f : 0.5f);

            if (scaledDx > touchSlop || scaledDy > touchSlop) {
                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    // gesture is perpendicular to the pager: let the pager handle it
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // gesture is parallel: keep it while the child can still scroll
                    getParent().requestDisallowInterceptTouchEvent(
                            childCanScroll(orientation, isVpHorizontal ? dx : dy));
                }
            }
        }
    }
}
