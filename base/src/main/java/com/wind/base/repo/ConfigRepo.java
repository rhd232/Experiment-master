package com.wind.base.repo;

import android.content.Context;

import com.wind.base.C;
import com.wind.base.bean.Config;
import com.wind.base.utils.PrefsUtil;


/**
 * Created by wind on 2017/11/30.
 */

public class ConfigRepo {


    public static class LayzHolder {
        public static final ConfigRepo INSTANCE = new ConfigRepo();
    }

    public static ConfigRepo getInstance() {
        return LayzHolder.INSTANCE;
    }

    public void store(Context context, Config config) {
        if (config == null) {
            PrefsUtil.setString(context, C.PREF_KEY.DEVICE_NAME, "");
            PrefsUtil.setString(context, C.PREF_KEY.DEVICE_ADDRESS, "");

        } else {

            PrefsUtil.setString(context, C.PREF_KEY.DEVICE_NAME, config.getBluetoothDeviceName());
            PrefsUtil.setString(context, C.PREF_KEY.DEVICE_ADDRESS, config.getBluetoothDeviceAddress());


        }
    }

    public Config get(Context context) {
        Config config = new Config();
        String deviceName = PrefsUtil.getString(context, C.PREF_KEY.DEVICE_NAME, "");
        String deviceAddr = PrefsUtil.getString(context, C.PREF_KEY.DEVICE_ADDRESS, "");
        config.setBluetoothDeviceName(deviceName);
        config.setBluetoothDeviceAddress(deviceAddr);
        return config;

    }


}
