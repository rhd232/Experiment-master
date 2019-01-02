package com.jz.experiment.module.expe.event;

import com.wind.data.expe.bean.DeviceInfo;

public class ConnectRequestEvent {

    private DeviceInfo deviceInfo;
    public ConnectRequestEvent(DeviceInfo deviceInfo){
        this.deviceInfo=deviceInfo;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

}
