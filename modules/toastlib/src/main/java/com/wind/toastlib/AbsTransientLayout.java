package com.wind.toastlib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by wind on 2018/1/19.
 */

public abstract class  AbsTransientLayout extends FrameLayout implements ContentViewCallback {

    private OnLayoutChangeListener mOnLayoutChangeListener;
    public AbsTransientLayout(@NonNull Context context) {
        super(context);
    }

    public AbsTransientLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsTransientLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mOnLayoutChangeListener != null) {
            mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
        }
    }

    public interface OnLayoutChangeListener{
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    public void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
        this.mOnLayoutChangeListener = onLayoutChangeListener;
    }
}
