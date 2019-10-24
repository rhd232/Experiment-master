package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SlidingConfilctRecyclerView extends RecyclerView {
    public SlidingConfilctRecyclerView(Context context) {
        this(context, null);
    }

    public SlidingConfilctRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingConfilctRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    int mLastXIntercept;
    int mLastYIntercept;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {

        boolean intercepted = false;
        int x = (int) e.getX();
        int y = (int) e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted=false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX=x-mLastXIntercept;
                int deltaY=x-mLastYIntercept;
                if (Math.abs(deltaX) >= 2*Math.abs(deltaY)) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }

                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;

        }
        mLastXIntercept=x;
        mLastYIntercept=y;
       // System.out.println("SlidingConfilctintercepted:"+intercepted);
        if (intercepted) {
            return super.onInterceptTouchEvent(e);
        }

        return intercepted;

    }
}
