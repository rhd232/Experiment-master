package com.wind.location;

import android.content.Context;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import static com.amap.api.location.AMapLocation.LOCATION_SUCCESS;

/**
 * Created by wind on 2018/1/9.
 */

public class GaoDeLocationHelper extends AbsLocationHelper {


    private static GaoDeLocationHelper instance;
    private Context mContext;
    private LocationListener mListener;
    private AMapLocationClient mLocationClient;

    private GaoDeLocationHelper(Context context) {
        this.mContext = context;
    }

    public static GaoDeLocationHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (GaoDeLocationHelper.class) {
                if (instance == null) {
                    instance = new GaoDeLocationHelper(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void stopLocation() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient = null;
        }
    }

    @Override
    public void startLocation(LocationListener listener) {
        mListener = listener;
        mLocationClient = new AMapLocationClient(mContext);
        AMapLocationClientOption mLocationClientOption = new AMapLocationClientOption();
        mLocationClient.setLocationListener(new MyLocationListener());
        mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        if (mLocationOption != null) {
            mLocationClientOption.setOnceLocation(mLocationOption.onceLocation);
            mLocationClientOption.setNeedAddress(mLocationOption.needAddress);
            mLocationClientOption.setInterval(mLocationOption.locationInterval);
        } else {
            mLocationClientOption.setOnceLocation(true);//只定位一次
            mLocationClientOption.setNeedAddress(true);//需要解析地址
        }


        //设置定位参数
        mLocationClient.setLocationOption(mLocationClientOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mLocationClient.startLocation();
    }

    public class MyLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location.getErrorCode() == LOCATION_SUCCESS) {
                LocationInfo locationInfo = new LocationInfo();
                double latitude = location.getLatitude();    //获取纬度信息
                double longitude = location.getLongitude();    //获取经度信息
                locationInfo.setLatitude(latitude);
                locationInfo.setLongitude(longitude);

                String province = location.getProvince();
                String city = location.getCity();
                if (!TextUtils.isEmpty(province)) {
                    if (province.endsWith("省") || province.endsWith("市")) {
                        province = province.substring(0, province.length() - 1);
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

}
