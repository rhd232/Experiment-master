package com.wind.view;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wind on 2018/4/12.
 */

public class NestedTabLayout extends TabLayout {
    public NestedTabLayout(Context context) {
        super(context);
    }

    public NestedTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //告诉parentView不要阻止我的事件
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
