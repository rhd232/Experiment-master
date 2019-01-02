package com.wind.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.wind.base.C;
import com.wind.base.response.BaseResponse;
import com.wind.data.base.bean.Location;
import com.wind.data.base.request.LocationRequest;
import com.wind.data.base.response.LocationResponse;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by wind on 2017/3/25.
 */

public class LocationDataStore implements DataStore<LocationRequest,LocationResponse> {
    public static final String DEFAULT_NAME="config";
    private Context mContext;
    @Inject
    public LocationDataStore(Context context){
        this.mContext=context;
    }
    @Override
    public Observable<LocationResponse> get(final LocationRequest request) {
        return Observable.create(new Observable.OnSubscribe<LocationResponse>() {
            @Override
            public void call(Subscriber<? super LocationResponse> subscriber) {
                SharedPreferences sp = mContext.getSharedPreferences(DEFAULT_NAME,
                        Context.MODE_PRIVATE);

                String province=sp.getString(C.PREF_KEY.LOCATION_PROVINCE,"");
                String city=sp.getString(C.PREF_KEY.LOCATION_CITY,"");

                String latitude=sp.getString(C.PREF_KEY.LOCATION_LATITUDE,"0");
                String longitude=sp.getString(C.PREF_KEY.LOCATION_LONGITUDE,"0");

                Location location=new Location();
                location.setProvince(province);
                location.setCity(city);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                LocationResponse response=new LocationResponse();

                response.setErr(BaseResponse.CODE_SUCCESS);
                response.setLocation(location);
                subscriber.onNext(response);
                subscriber.onCompleted();
            }
        });

    }
}
