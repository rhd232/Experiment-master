package com.anitoa.event;

public class AnitoaConnectedEvent {
    private String deviceName;
    public AnitoaConnectedEvent(String deviceName){
        this.deviceName=deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
