package com.jz.experiment.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.bluetooth.UsbService;
import com.jz.experiment.module.bluetooth.event.BluetoothConnectedEvent;
import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;
import com.jz.experiment.util.DeviceProxyHelper;
import com.wind.base.utils.SystemUiUtil;

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
       // BluetoothService service=DeviceProxyHelper.getInstance(getContext()).getBluetoothService();
        UsbService service=DeviceProxyHelper.getInstance(getContext()).getUsbService();
        String name="";
        if (service!=null){
            if (service.getConnectedDevice()!=null){
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    name = service.getConnectedDevice().getProductName();
                }else {
                    name="HID";
                }
                /*if (TextUtils.isEmpty(name)){
                    name=service.getConnectedDevice().getAddress();
                }*/
            }

        }

        tv_device_name.setText(name);
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
     * 蓝牙设备连接成功
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothConnectedEvent(BluetoothConnectedEvent event){
        tv_device_name.setText(event.getDeviceName());
        tv_device_state.setText("连接中");
        setActivated(false);
        setStatusbarColor(getContext().getResources().getColor(R.color.color1F4E99));
    }

    private void setStatusbarColor(int color){
        if(getContext() instanceof Activity){
            Activity activity= (Activity) getContext();
            SystemUiUtil.setStatusBarColor(activity, color);
        }
    }
    /**
     *蓝牙设备断开连接
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothDisConnectedEvent(BluetoothDisConnectedEvent event){
        tv_device_name.setText(event.getDeviceName());
        tv_device_state.setText("连接已中断");
        setActivated(true);
        setStatusbarColor(getContext().getResources().getColor(R.color.colorD54646));
    }


}
