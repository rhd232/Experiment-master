package com.wind.location;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by wind on 2018/1/9.
 */

public class BaiduLocationHelper extends AbsLocationHelper {
    private LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private static BaiduLocationHelper instance;
    private Context mContext;
    private LocationListener mListener;
    private BaiduLocationHelper(Context context) {
        this.mContext=context;
    }

    public static BaiduLocationHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (BaiduLocationHelper.class) {
                if (instance == null) {
                    instance = new BaiduLocationHelper(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void stopLocation() {
        if (mLocationClient!=null){
            mLocationClient.stop();
            mLocationClient=null;
        }
    }

    @Override
    public void startLocation(LocationListener listener) {
        mListener=listener;
        mLocationClient = new LocationClient(mContext);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，设置定位模式，默认高精度
//LocationMode.Hight_Accuracy：高精度；
//LocationMode. Battery_Saving：低功耗；
//LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
//可选，设置返回经纬度坐标类型，默认gcj02
//gcj02：国测局坐标；
//bd09ll：百度经纬度坐标；
//bd09：百度墨卡托坐标；
//海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
        if (mLocationOption!=null){
            if (mLocationOption.onceLocation){
                option.setScanSpan(0);
            }else {
                option.setScanSpan((int)mLocationOption.locationInterval);
            }
            option.setIsNeedAddress(mLocationOption.needAddress);
        }else {
            option.setScanSpan(0);
            option.setIsNeedAddress(true);
            //可选，设置发起定位请求的间隔，int类型，单位ms
//如果设置为0，则代表单次定位，即仅定位一次，默认为0
//如果设置非0，需设置1000ms以上才有效
        }


        option.setOpenGps(true);
//可选，设置是否使用gps，默认false
//使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
//可选，定位SDK内部是一个service，并放到了独立进程。
//设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
//可选，设置是否收集Crash信息，默认收集，即参数为false

        //option.setWifiCacheTimeOut(5 * 60 * 1000);
//可选，7.2版本新增能力
//如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGps(false);
//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocationClient.setLocOption(option);
//mLocationClient为第二步初始化过的LocationClient对象
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

        mLocationClient.start();
//mLocationClient为第二步初始化过的LocationClient对象
//调用LocationClient的start()方法，便可发起定位请求
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

          /*  double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            String coorType = location.getCoorType();*/
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            LocationInfo locationInfo=new LocationInfo();
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            locationInfo.setLatitude(latitude);
            locationInfo.setLongitude(longitude);

            String province=location.getProvince();
            String city=location.getCity();
            if (!TextUtils.isEmpty(province)){
                if (province.endsWith("省")||province.endsWith("市")){
                    province=province.substring(0,province.length()-1);
                }

            }
            if (!TextUtils.isEmpty(city)) {
                if (city.endsWith("市")) {
                    city = city.substring(0, city.length() - 1);
                }
            }
            locationInfo.setProvince(province);
            locationInfo.setCity(city);
            mListener.location(locationInfo);

        }
    }
}
