package com.anitoa.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.anitoa.C;
import com.anitoa.bean.Data;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.event.AnitoaDisConnectedEvent;
import com.anitoa.io.AnitoaDevice;
import com.anitoa.listener.AnitoaConnectionListener;

import org.greenrobot.eventbus.EventBus;


public class AnitoaService extends Service {
    protected AnitoaConnectionListener mListener;

    public static final int MAX_DEVICE_COUNT=3;
    private AnitoaDevice[] mAnitoaDevices = new AnitoaDevice[MAX_DEVICE_COUNT];

    @Override
    public void onCreate() {
        super.onCreate();

        registerUsbEventReceiver();
        for (int i=0;i<MAX_DEVICE_COUNT;i++){
            mAnitoaDevices[i]=new AnitoaDevice();
        }

    }

    /* public  Device getConnectedDevice(){

    }
    public  Device[] getConnectedDevices(){

    }

    public  int sendPcrCommand(PcrCommand command){

    }*/
    public  boolean isConnected(int deviceIndex){
        AnitoaDevice device=mAnitoaDevices[deviceIndex];
        return device.isConnected();
    }
    /**
     * 连接指定设备
     * @param deviceIndex
     * @param deviceName
     * @return
     */
    public int connectDevice(int deviceIndex, String deviceName) {
        return mAnitoaDevices[deviceIndex].openUsbPort(this, deviceIndex, deviceName, mHandler);
    }

    public byte[] sendPcrCommandSync(int deviceId, PcrCommand command) {
        return mAnitoaDevices[deviceId].sendPcrCommandSync(command);
    }
    public int sendPcrCommand(int deviceId, PcrCommand command) {
        return mAnitoaDevices[deviceId].sendPcrCommand(command);
    }

    /**
     * deviceId指定的设备连接成功时调用
     */
    public void onDeviceConnected(int deviceId) {

    }

    /**
     * deviceId指定的设备断开连接时调用
     */
    public void onDeviceDisconnected(int deviceId) {
        //设备断开连接后必须重新读取下位机的trim数据
        //TODO 多设备情况下每台设备得有自己的FlashData
        //FlashData.flash_inited = false;
    }

    public void setNotify(AnitoaConnectionListener listener) {
        this.mListener = listener;
    }

    protected final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // 连接成功
                    if (mListener != null) {
                        mListener.onConnectSuccess();
                    }
                    break;
                case 1: // 链接中断
                    if (mListener != null) {
                        mListener.onConnectCancel();
                    }
                    break;
                case 2: // 可以进行数据通信

                    if (mListener != null) {
                        mListener.onDoThing();
                    }
                    break;
                case C.WHAT.RECEIVED_DATA: // 接受到数据
                    if (mListener != null) {
                        Data data = (Data) msg.obj;
                        mListener.onReceivedData(data);
                    }

                    break;
            }
        }
    };


    public void registerUsbEventReceiver(){
        //注册USB事件通知
        IntentFilter usbEventFilter = new IntentFilter();
        usbEventFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbEventFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbEventReceiver, usbEventFilter);
    }

    private final BroadcastReceiver mUsbEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                //TODO usb设备插上了
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                //TODO usb设备拔出了

                String name = "HID";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    name = device.getProductName();
                }
                String deviceName=device.getDeviceName();
                //发送设备拔出通知给上层
                AnitoaDisConnectedEvent event = new AnitoaDisConnectedEvent(name,deviceName);
                EventBus.getDefault().post(event);
                //清理关闭的设备
                AnitoaDevice anitoaDevice=findByDeviceName(deviceName);
                anitoaDevice.closePort();

                //TODO 清除从下位机读取的trim
                onDeviceDisconnected(anitoaDevice.getDeviceIndex());
            }
        }
    };


    public AnitoaDevice findByDeviceName(String deviceName){
        AnitoaDevice target=null;
        for (int i=0;i<mAnitoaDevices.length;i++){
            AnitoaDevice device=mAnitoaDevices[i];
            if (device!=null &&
                    deviceName.equals(device.getDeviceName())){
                target=device;
                break;
            }
        }
        return target;
    }


    public class LocalBinder extends Binder {
        public AnitoaService getService() {
            return AnitoaService.this;
        }
    }

    private final IBinder mBinder = new AnitoaService.LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbEventReceiver);
    }
}
