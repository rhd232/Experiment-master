package com.anitoa.io;

import android.os.Handler;

import com.anitoa.cmd.CommandSendable;

public abstract class AnitoaPort implements CommandSendable {

    protected int mDeviceId;
    protected String mDeviceName;
    protected boolean mClosePort;
    protected Handler mHandler;
    public AnitoaPort(int deviceId, String deviceName, Handler handler){
        mDeviceId=deviceId;
        this.mDeviceName=deviceName;
        mHandler=handler;
    }

    public abstract void connect();
    public abstract void stop();

}
