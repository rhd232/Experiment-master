package com.anitoa.event;

public class AnitoaDisConnectedEvent {

    private String deviceName;
    public AnitoaDisConnectedEvent(String deviceName){
        this.deviceName=deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
