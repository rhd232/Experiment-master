package com.jz.experiment.module.bluetooth;

import android.content.Context;
import android.text.TextUtils;

import com.jz.experiment.module.expe.bean.UsbDeviceInfo;
import com.wind.base.C;
import com.wind.base.utils.PrefsUtil;

public class DeviceRepo {

    public static class LayzHolder {
        public static final DeviceRepo INSTANCE = new DeviceRepo();
    }

    public static DeviceRepo getInstance() {
        return DeviceRepo.LayzHolder.INSTANCE;
    }

    public void store(Context context, UsbDeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            PrefsUtil.setString(context, C.PREF_KEY.PREF_KEY_USB_DEVICE_NAME, "");

        } else {
            if (!TextUtils.isEmpty(deviceInfo.getDeviceName())){
                PrefsUtil.setString(context, C.PREF_KEY.PREF_KEY_USB_DEVICE_NAME, deviceInfo.getDeviceName());

            }



        }

    }
    public UsbDeviceInfo get(Context context) {
        UsbDeviceInfo info=new UsbDeviceInfo();
        String deviceName=PrefsUtil.getString(context, C.PREF_KEY.PREF_KEY_USB_DEVICE_NAME, "");
        info.setDeviceName(deviceName);

        return info;

    }
}