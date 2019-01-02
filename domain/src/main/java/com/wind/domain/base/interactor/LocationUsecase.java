package com.wind.domain.base.interactor;

import com.wind.base.usecase.Usecase;
import com.wind.data.LocationDataStore;
import com.wind.data.base.request.LocationRequest;
import com.wind.data.base.response.LocationResponse;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by wind on 2017/3/25.
 */

public class LocationUsecase extends Usecase<LocationRequest,LocationResponse> {

    private LocationDataStore mStore;
    @Inject
    public LocationUsecase(LocationDataStore store){
        this.mStore=store;
    }

    @Override
    protected Observable<LocationResponse> buildUsecaseObservable(LocationRequest request) {
        return mStore.get(request);
    }
}
