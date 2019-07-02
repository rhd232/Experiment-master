package com.jz.experiment.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.wind.base.utils.A4Util;
import com.wind.base.utils.AppUtil;

public class A4PageLayout extends LinearLayout {

    public A4PageLayout(@NonNull Context context) {
        this(context,null);
    }

    public A4PageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public A4PageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int a4Width=A4Util.getA4Width(getContext());

        int screenWidth=AppUtil.getScreenWidth((Activity) getContext());

        float scale=screenWidth/(float)a4Width;
        setPivotX(0);
        setPivotY(0);
        setScaleX(scale);
        setScaleY(scale);



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int a4Width=A4Util.getA4Width(getContext());
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(a4Width,
                MeasureSpec.EXACTLY);
        int a4Height=A4Util.getA4Height(getContext());
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(a4Height,
                MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
