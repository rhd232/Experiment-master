package com.jz.experiment.module.expe.bean;

public class UsbDeviceInfo {

    private String deviceName;//小票打印机
    private String labelDeviceName;//标签打印机


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getLabelDeviceName() {
        return labelDeviceName;
    }

    public void setLabelDeviceName(String labelDeviceName) {
        this.labelDeviceName = labelDeviceName;
    }
}
