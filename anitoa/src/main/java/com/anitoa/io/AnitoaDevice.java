package com.anitoa.io;

import android.content.Context;
import android.os.Handler;

import com.anitoa.cmd.CommandSendable;
import com.anitoa.cmd.PcrCommand;

public class AnitoaDevice implements CommandSendable {

    private AnitoaPort mPort = null;

    public int openUsbPort(Context context,int deviceId, String deviceName, Handler handler){
        if (mPort!=null){
            mPort.stop();
            mPort=null;
        }
        mPort=new AnitoaUsbPort(context,deviceId,deviceName,handler);
        mPort.connect();
        return 0;
    }


    @Override
    public byte[] sendPcrCommandSync( PcrCommand command) {
        return mPort.sendPcrCommandSync(command);
    }

    @Override
    public int sendPcrCommand(PcrCommand command) {
        return mPort.sendPcrCommand(command);
    }
}
