package com.wind.toastlib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by wind on 2018/1/19.
 */

public class ToastTransientLayout extends AbsTransientLayout {
    public ToastTransientLayout(@NonNull Context context,String text) {
        super(context);
        init(text);
    }
    public ToastTransientLayout(@NonNull Context context) {
        super(context);
        init("");
    }

    public ToastTransientLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init("");
    }

    public ToastTransientLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init("");
    }

    private TextView tvMsg;
    private void init(String text){
        inflate(getContext(), R.layout.layout_toast,this);
        tvMsg=findViewById(R.id.tv);
        tvMsg.setText(text);
    }


    @Override
    public void animateContentIn(int delay, int duration) {
        tvMsg.setAlpha(0f);
        tvMsg.animate()
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(delay)
                .start();
    }

    @Override
    public void animateContentOut(int delay, int duration) {
        tvMsg.setAlpha(1f);
        tvMsg.animate()
                .alpha(0f)
                .setDuration(duration)
                .setStartDelay(delay)
                .start();
    }
}
