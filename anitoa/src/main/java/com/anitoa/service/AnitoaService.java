package com.anitoa.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.anitoa.C;
import com.anitoa.bean.Data;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.io.AnitoaDevice;
import com.anitoa.listener.AnitoaConnectionListener;


public class AnitoaService extends Service {
    protected AnitoaConnectionListener mListener;

    public static final int MAX_DEVICE_COUNT=10;
    private AnitoaDevice[] mAnitoaDevices = new AnitoaDevice[MAX_DEVICE_COUNT];

   /* public  Device getConnectedDevice(){

    }
    public  Device[] getConnectedDevices(){

    }
    public  boolean isConnected(String deviceId){

    }
    public  int sendPcrCommand(PcrCommand command){

    }*/

    /**
     * 连接指定设备
     * @param deviceId
     * @param deviceName
     * @return
     */
    public int connectDevice(int deviceId, String deviceName) {
        return mAnitoaDevices[deviceId].openUsbPort(this, deviceId, deviceName, mHandler);
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
}
