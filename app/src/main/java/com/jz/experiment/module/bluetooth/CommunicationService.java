package com.jz.experiment.module.bluetooth;

import android.app.Service;

public abstract class CommunicationService extends Service {

    public abstract boolean initialize();
    public abstract Device getConnectedDevice();
    public abstract boolean isConnected();

    public abstract int sendPcrCommand(PcrCommand command);
    public abstract byte[] sendPcrCommandSync(PcrCommand command);
}
