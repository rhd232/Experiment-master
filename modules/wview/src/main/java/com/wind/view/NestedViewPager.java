package com.wind.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wind on 2018/3/15.
 * 解决viewpager嵌套viewpager导致滑动问题
 * 1，外层viewpager能滑动时，内层的viewpager将得不到滑动
 * 2，外层viewpager设置setTouchListener 返回true，禁止外层viewpager滑动，内层viewpager滑动时会滑出边界
 */

public class NestedViewPager extends ViewPager {
    public NestedViewPager(Context context) {
        super(context);
    }

    public NestedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //告诉parentView不要阻止我的事件
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
