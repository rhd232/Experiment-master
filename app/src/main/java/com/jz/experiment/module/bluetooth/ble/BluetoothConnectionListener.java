package com.jz.experiment.module.bluetooth.ble;

import com.jz.experiment.module.bluetooth.Data;

public interface BluetoothConnectionListener {

    void onConnectSuccess();
    void onConnectCancel();
    void onDoThing();
    void onReceivedData(Data data);
}
