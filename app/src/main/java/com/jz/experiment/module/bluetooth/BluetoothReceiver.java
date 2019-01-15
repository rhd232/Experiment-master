package com.jz.experiment.module.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jz.experiment.module.bluetooth.ble.BluetoothConnectionListener;

public class BluetoothReceiver extends BroadcastReceiver {
    public static String TAG = "BluetoothReceiver";
    Data data;
    private BluetoothConnectionListener mBluetoothConnetListenr;//实现接口
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // 连接成功
                    if (mBluetoothConnetListenr != null) {
                        mBluetoothConnetListenr.onConnectSuccess();
                    }
                    break;
                case 1: // 链接中断
                    if (mBluetoothConnetListenr != null) {
                        mBluetoothConnetListenr.onConnectCancel();
                    }
                    break;
                case 2: // 可以进行数据通信

                    if (mBluetoothConnetListenr != null) {
                        mBluetoothConnetListenr.onDoThing();
                    }
                    break;
                case 3: // 接受到数据

                    if (mBluetoothConnetListenr != null) {
                        mBluetoothConnetListenr.onReceivedData(data);
                    }

                    break;
            }
        }
    };

    public void setBluetoothConnectInteface(BluetoothConnectionListener m) {
        mBluetoothConnetListenr = m;
    }

    @Override
    public void onReceive(Context arg0, Intent intent) {
        // TODO Auto-generated method stub

        final String action = intent.getAction();
        Log.d(TAG, "Action==" + action);
        if (BluetoothService.ACTION_DEVICE_CONNECTED.equals(action)) { //连接成功
            Log.e(TAG, "Only gatt, just wait");

            mHandler.sendEmptyMessage(0);

        } else if (BluetoothService.ACTION_DEVICE_CONNECT_FAILED.equals(action)) { //连接失败
            mHandler.sendEmptyMessage(1);

        } else if (BluetoothService.ACTION_DEVICE_COMMUNICATION_ENABLED
                .equals(action)) //可以通信
        {
            mHandler.sendEmptyMessage(2);

        } else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) { //接受到数据
            Log.e(TAG, "RECV DATA");

            data = intent.getParcelableExtra(BluetoothService.EXTRA_DATA);
            mHandler.sendEmptyMessage(3);
        }
    }

}
