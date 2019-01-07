package com.jz.experiment.module.bluetooth.ble;

public interface BluetoothConnectionListener {

    void onConnectSuccess();
    void onConnectCancel();
    void onDoThing();
    void onReceivedData(String data);
}
