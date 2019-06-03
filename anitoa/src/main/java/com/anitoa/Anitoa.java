package com.anitoa;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.anitoa.service.BluetoothService;
import com.anitoa.service.CommunicationService;
import com.anitoa.service.UsbService;

public class Anitoa {

    private boolean mBinding;//是否正在bindService
    private static Anitoa sInstance = null;

    public static Anitoa getInstance(Context context) {
        if (sInstance == null) {
            synchronized (Anitoa.class) {
                if (sInstance == null) {
                    sInstance = new Anitoa(context);
                }
            }
        }
        return sInstance;
    }

    public void unbindService(Context context){
        if (mBinding){
            try {
                context.getApplicationContext().unbindService(mUsbServiceConnection);
                mUsbServiceConnection=null;
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                context.getApplicationContext().unbindService(mBluetoothServiceConnection);
                mBluetoothServiceConnection=null;
            }catch (Exception e){
                e.printStackTrace();
            }

            onDestroy();
        }
    }

    public void onDestroy(){
        if (mUsbService!=null){
            mUsbService.stopReadThread();
            mUsbService.onDestroy();
            mUsbService=null;
        }

        if (mBluetoothService!=null){
            mBluetoothService.cancel();
            mBluetoothService=null;
        }
        sInstance=null;
    }
    private Anitoa(Context context) {
        if (mBluetoothService == null) {
            if (!mBinding) {
                mBinding = true;
                Intent service = new Intent(context.getApplicationContext(), BluetoothService.class);
                context.getApplicationContext().bindService(service, mBluetoothServiceConnection, Context.BIND_AUTO_CREATE);

                Intent usbService = new Intent(context.getApplicationContext(), UsbService.class);
                context.getApplicationContext().bindService(usbService, mUsbServiceConnection, Context.BIND_AUTO_CREATE);

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
    private UsbService mUsbService;
    private ServiceConnection mUsbServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UsbService.LocalBinder binder = (UsbService.LocalBinder) service;
            mUsbService = binder.getService();
            mUsbService.initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUsbService = null;
        }
    };
    public BluetoothService getBluetoothService() {
        return mBluetoothService;
    }
    public UsbService getUsbService() {
        return mUsbService;
    }

    public CommunicationService getCommunicationService(){
        if (mBluetoothService!=null&& mBluetoothService.isConnected()){
            if (mUsbService!=null){
                mUsbService.stopReadThread();
                mUsbService.onDestroy();
                mUsbService=null;
            }
            return mBluetoothService;
        }

        if (mUsbService!=null&&mUsbService.isConnected()){
            if (mBluetoothService!=null){
                mBluetoothService.cancel();
                mBluetoothService=null;
            }
            return mUsbService;
        }

        return mUsbService;
    }

    public enum ConnectMode{
        BLUETOOTH,USB
    }





}
