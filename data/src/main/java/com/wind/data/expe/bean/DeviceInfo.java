package com.wind.data.expe.bean;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.util.Objects;

public class DeviceInfo {
    private BluetoothDevice device;

    private boolean connected;
    private String address;
    public String getName() {
        String name=device.getName();
        if (TextUtils.isEmpty(name)){
            name=device.getAddress();
        }
        return name;

    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getAddress() {
        return device.getAddress();
    }



    @Override
    public int hashCode() {
        return Objects.hash(getName(),getAddress());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj==this){
          return true;
      }
      if (obj instanceof DeviceInfo){
          DeviceInfo other= (DeviceInfo) obj;
          if (!other.getAddress().equals(getAddress())){
              return false;
          }
          if (other.getName()==null && getName()!=null){
              return false;
          }
          if (other.getName()==null && getName()==null){
              return true;
          }
          if (!other.getName().equals(getName())){
              return false;
          }
          return true;
      }
      return false;
    }
}
