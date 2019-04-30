package com.jz.experiment.module.bluetooth;

import android.app.Service;
import android.os.Handler;
import android.os.Message;

import com.jz.experiment.chart.FlashData;
import com.jz.experiment.module.bluetooth.ble.BluetoothConnectionListener;

public abstract class CommunicationService extends Service {
    protected BluetoothConnectionListener mListener;

    public abstract boolean initialize();
    public abstract Device getConnectedDevice();
    public abstract boolean isConnected();
    public abstract int sendPcrCommand(PcrCommand command);
    public abstract byte[] sendPcrCommandSync(PcrCommand command);

    /**
     * 设备连接成功时调用
     */
    public void onDeviceConnected(){

    }

    /**
     * 设备断开连接时调用
     */
    public void onDeviceDisconnected(){
        //设备断开连接后必须重新读取下位机的trim数据
        FlashData.flash_inited=false;
    }
    public void setNotify(BluetoothConnectionListener listener){
        this.mListener=listener;
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
                case 3: // 接受到数据

                    if (mListener != null) {
                        Data data= (Data) msg.obj;
                        mListener.onReceivedData(data);
                    }

                    break;
            }
        }
    };
}
