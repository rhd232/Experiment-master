package com.anitoa.io;

import android.os.Handler;

import com.anitoa.cmd.CommandSendable;

public abstract class AnitoaPort implements CommandSendable {

    protected int mDeviceIndex;
    protected String mDeviceName;
    protected boolean mClosePort;
    protected Handler mHandler;
    public AnitoaPort(int deviceIndex, String deviceName, Handler handler){
        mDeviceIndex=deviceIndex;
        this.mDeviceName=deviceName;
        mHandler=handler;
    }

    public abstract void connect();
    public abstract void stop();

    public abstract boolean isConnected();

}
