package com.wind.base.request;

import com.wind.base.C;

/**
 * Created by wind on 16/5/19.
 */
public class BaseRequest {
    public BaseRequest(){
        //利用反射获取context
       /* try {
            Class appClass=Class.forName("com.xkd.dinner.App");
            Method getMethod=appClass.getDeclaredMethod("get");
            Application app= (Application) getMethod.invoke(appClass);
            //获取经纬度
            SharedPreferences sp = app.getSharedPreferences(C.Value.SP_FILENAME,
                    Context.MODE_PRIVATE);
            latitude=sp.getString(C.PREF_KEY.LOCATION_LATITUDE,"0");
            longitude=sp.getString(C.PREF_KEY.LOCATION_LONGITUDE,"0");
            brand=AppUtil.getDeviceBrand()+"-"+AppUtil.getDeviceModel();
            os=AppUtil.getOsVersion();
            //获取umeng类AnalyticsConfig
            Class analyticsConfigClass=Class.forName("com.umeng.analytics.AnalyticsConfig");
            Method getChannelMethod=analyticsConfigClass.getDeclaredMethod("getChannel",Context.class);
            app_id=channel= (String) getChannelMethod.invoke(analyticsConfigClass,app);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
    public static final int LOAD_FROM_WEB=0;
    public static final int LOAD_FROM_LOCAL=1;
    protected int loadFrom;

    protected String token;

    public static final String DEVICE="android";
    protected String device=DEVICE;
    /**
     * 渠道号
     */
    protected String channel;
    /**
     * 请求的api版本号
     */
    protected String api_version;

    protected String app_id;

    /**
     * app版本号
     */
    protected String app_version;

    /**
     * 网络类型WIFI/2G/3G/4G
     */
    protected String network;
    /**
     * 终端品牌
     */
    protected String brand;
    /**
     * 操作系统版本
     */
    protected String os;

    /**
     * 屏幕分辨率
     */
    protected String screen;
    /**
     * 经度
     */
    protected String longitude;
    /**
     * 纬度
     */
    protected String latitude;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLoadFrom() {
        return loadFrom;
    }

    public void setLoadFrom(int loadFrom) {
        this.loadFrom = loadFrom;
    }


    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getApi_version() {
        return C.Version.API_VERSION;
    }

    public void setApi_version(String api_version) {
        this.api_version = api_version;
    }

    public String getApp_version() {
        return C.Version.APP_VERSION;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }
}
