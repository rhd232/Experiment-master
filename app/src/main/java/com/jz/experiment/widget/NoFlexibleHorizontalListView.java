package com.jz.experiment.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoFlexibleHorizontalListView extends HorizontalListView{




    public NoFlexibleHorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}