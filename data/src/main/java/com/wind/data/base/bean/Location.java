package com.wind.data.base.bean;

/**
 * Created by wind on 2017/3/25.
 */

public class Location {
    /**
     * 纬度
     */
    private String latitude;
    /**
     * 经度
     */
    private String longitude;
    private String province;
    private String city;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
