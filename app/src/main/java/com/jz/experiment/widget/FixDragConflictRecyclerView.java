package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.jz.experiment.R;
import com.wind.base.widget.VernierDragLayout;

public class FixDragConflictRecyclerView extends RecyclerView {


    public FixDragConflictRecyclerView(Context context) {
        super(context);
    }

    public FixDragConflictRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixDragConflictRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    int mLastXIntercept,mLastYIntercept;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean intercepted=false;
        int x= (int) e.getX();
        int y= (int) e.getY();
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                intercepted=false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX=x-mLastXIntercept;
                int deltaY=y-mLastYIntercept;
                //判断是否按住了vernierview上
                View view=findChildViewUnder(e.getX(),e.getY());
                boolean consumeDrag=false;
                if (view!=null){
                    Log.i("TouchEvent",view.getClass().getSimpleName());
                    VernierDragLayout dragLayout=view.findViewById(R.id.vernier_drag_layout);
                    if (dragLayout!=null){
                        consumeDrag=dragLayout.consumeDrag();
                    }
                }

                if (consumeDrag){
                    intercepted=false;
                }else {
                    if (Math.abs(deltaX) > Math.abs(deltaY) * 2) {
                        intercepted = true;
                    } else {
                        intercepted = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted=false;
                break;
        }

        mLastXIntercept=x;
        mLastYIntercept=y;
        return intercepted;
    }
}
