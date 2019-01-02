package com.wind.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by wind on 2018/3/22.
 * 解决viewpager嵌套webview导致滑动问题
 * 1，外层viewpager能滑动时，内层的webview内的活动组件将得不到滑动
 * 2，外层viewpager设置setTouchListener 返回true，禁止外层viewpager滑动，内层webview滑动时会导致调用外层viewpager滑出边界
 */
public class NestedWebView extends WebView {
    public NestedWebView(Context context) {
        super(context);
    }

    public NestedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //告诉parentView不要阻止我的事件
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
