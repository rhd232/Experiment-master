package com.anitoa.event;

public class AnitoaDisConnectedEvent {

    /**
     * 设备显示的名字,对应UsbDevice的 productName
     */
    private String productName;
    /**
     * 设置文件路径，对应UsbDevice的 deviceName
     */
    private String deviceFilePath;
    public AnitoaDisConnectedEvent(String productName,String deviceFilePath){
        this.productName=productName;
        this.deviceFilePath=deviceFilePath;
    }

    public String getDeviceName() {
        return productName;
    }

    public String getDeviceFilePath() {
        return deviceFilePath;
    }
}
