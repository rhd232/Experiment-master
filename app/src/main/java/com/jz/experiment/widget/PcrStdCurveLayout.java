package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.jz.experiment.R;

public class PcrStdCurveLayout extends FrameLayout {
    public PcrStdCurveLayout(@NonNull Context context) {
        this(context,null);
    }

    public PcrStdCurveLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PcrStdCurveLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.layout_pcr_std_curve,this);

    }
}
