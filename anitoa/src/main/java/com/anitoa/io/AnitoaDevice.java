package com.anitoa.io;

import android.content.Context;
import android.os.Handler;

import com.anitoa.cmd.CommandSendable;
import com.anitoa.cmd.PcrCommand;

public class AnitoaDevice implements CommandSendable {
    private String mDeviceName;
    private int mDeviceIndex;
    private AnitoaPort mPort = null;

    public int openUsbPort(Context context,int deviceIndex, String deviceName, Handler handler){
        if (mPort!=null){
            mPort.stop();
            mPort=null;
        }
        mDeviceName=deviceName;
        mDeviceIndex=deviceIndex;
        mPort=new AnitoaUsbPort(context,deviceIndex,deviceName,handler);
        mPort.connect();
        return 0;
    }

    public void closePort(){
        if (this.mPort != null) {
            this.mPort.stop();
            this.mPort = null;
        }

    }

    public boolean isConnected(){
        boolean connected=false;
        if (mPort!=null){
            connected= mPort.isConnected();
        }
        return connected;
    }

    @Override
    public byte[] sendPcrCommandSync( PcrCommand command) {
        return mPort.sendPcrCommandSync(command);
    }

    @Override
    public int sendPcrCommand(PcrCommand command) {
        return mPort.sendPcrCommand(command);
    }


    public String getDeviceName() {
        return mDeviceName;
    }

    public int getDeviceIndex() {
        return mDeviceIndex;
    }
}
