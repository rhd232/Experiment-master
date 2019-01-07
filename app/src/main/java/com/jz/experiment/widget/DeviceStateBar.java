package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jz.experiment.R;

public class DeviceStateBar extends FrameLayout {

    public DeviceStateBar(@NonNull Context context) {
        super(context);
        init();
    }

    public DeviceStateBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DeviceStateBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    TextView tv_device_name;
    TextView tv_device_state;
    private void init(){
        inflate(getContext(),R.layout.layout_device_state_bar,this);
        tv_device_name=findViewById(R.id.tv_device_name);
        tv_device_state=findViewById(R.id.tv_device_state);
    }

}
