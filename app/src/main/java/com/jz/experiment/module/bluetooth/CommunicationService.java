package com.jz.experiment.module.bluetooth;

import android.app.Service;

import com.jz.experiment.module.bluetooth.ble.BluetoothConnectionListener;

public abstract class CommunicationService extends Service {
    protected BluetoothConnectionListener mListener;

    public abstract boolean initialize();
    public abstract Device getConnectedDevice();
    public abstract boolean isConnected();
    public abstract int sendPcrCommand(PcrCommand command);
    public abstract byte[] sendPcrCommandSync(PcrCommand command);


    public void setNotify(BluetoothConnectionListener listener){
        this.mListener=listener;
    }
}
