package com.jz.experiment.module.bluetooth.event;

public class BluetoothDisConnectedEvent {

    private String deviceName;
    public BluetoothDisConnectedEvent(String deviceName){
        this.deviceName=deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
