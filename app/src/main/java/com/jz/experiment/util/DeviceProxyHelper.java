package com.jz.experiment.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jz.experiment.module.bluetooth.BluetoothService;

public class DeviceProxyHelper {

    private boolean mBinding;//是否正在bindService
    private static DeviceProxyHelper INSTANCE = null;

    public static DeviceProxyHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DeviceProxyHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DeviceProxyHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    private DeviceProxyHelper(Context context) {
        if (mBluetoothService == null) {
            if (!mBinding) {
                mBinding = true;
                Intent service = new Intent(context.getApplicationContext(), BluetoothService.class);
                context.getApplicationContext().bindService(service, mBluetoothServiceConnection, Context.BIND_AUTO_CREATE);
            }

        }
    }

    private BluetoothService mBluetoothService;
    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    public BluetoothService getBluetoothService() {
        return mBluetoothService;
    }

    public boolean isConnected(){
        return mBluetoothService==null?false:mBluetoothService.isConnected();
    }


}
