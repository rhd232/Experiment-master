package com.anitoa.bean;

public class Device {

    private String deviceName;
    private String addr;

    public Device(String deviceName, String addr) {
        this.deviceName = deviceName;
        this.addr = addr;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
