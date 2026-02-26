package com.medoapps.www.onlinequran.hashimyoutubeplayer;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewOnClickListener3 implements RecyclerView.OnItemTouchListener {

    private RecyclerViewOnClickListener3.OnItemClickListener mListener3;
    private GestureDetector mGestureDetector3;

    public interface OnItemClickListener {
        void onItemClick(View view3, int position3);
    }

    public
    RecyclerViewOnClickListener3(Context context3, RecyclerViewOnClickListener3.OnItemClickListener listener) {
        mListener3 = listener;
        mGestureDetector3 = new GestureDetector(context3, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view3, MotionEvent e) {
        View childView = view3.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener3 != null && mGestureDetector3.onTouchEvent(e)) {
            mListener3.onItemClick(childView, view3.getChildPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view3, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

}