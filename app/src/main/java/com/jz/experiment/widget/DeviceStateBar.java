package com.jz.experiment.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

        setBackgroundResource(R.drawable.selector_device_state_bar);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    /**
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothDisConnectedEvent(BluetoothDisConnectedEvent event){
        tv_device_name.setText(event.getDeviceName());
        tv_device_state.setText("连接已中断");
        setActivated(true);
    }


}
