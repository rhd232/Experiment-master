package com.jz.experiment.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.jz.experiment.R;
import com.wind.base.utils.DisplayUtil;

public class SlidingConflictScrollView extends NestedScrollView {
    public SlidingConflictScrollView(Context context) {
        super(context);
    }

    public SlidingConflictScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingConflictScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private SlidingConfilctRecyclerView recyclerView;
    private int height;
    private Rect rect;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        recyclerView = findViewById(R.id.rv);
        height = DisplayUtil.dip2px(getContext(), 135);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
      /*  int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            System.out.println("onInterceptTouchEvent:false");
            return false;

        }

        boolean flag = super.onInterceptTouchEvent(ev);
        int recyclerTop = recyclerView.getTop();

        rect = new Rect(0, recyclerTop, getWidth(), recyclerTop + height);
        if (rect.contains((int) ev.getX(), (int) ev.getY())) {
             return true;
        }else {
            return false;
        }*/

      /*  System.out.println("SlidingConflictScrollView intercept:" + flag);
        return flag;*/

    }
}
