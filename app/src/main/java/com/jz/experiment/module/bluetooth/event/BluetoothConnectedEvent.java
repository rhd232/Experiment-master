package com.jz.experiment.module.bluetooth.event;

public class BluetoothConnectedEvent {
    private String deviceName;
    public BluetoothConnectedEvent(String deviceName){
        this.deviceName=deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
