package com.wind.data.expe.bean;

import java.util.Objects;

public class DeviceInfo {

    private String name;
    private boolean connected;
    private String address;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,address);
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
