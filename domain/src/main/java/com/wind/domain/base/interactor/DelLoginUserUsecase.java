package com.wind.domain.base.interactor;

import com.wind.base.usecase.Usecase;
import com.wind.data.base.datastore.LoginUserDbDataStore;
import com.wind.data.base.request.DelLoginUserRequest;
import com.wind.data.base.response.DelLoginUserResponse;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by wind on 2017/3/28.
 */

public class DelLoginUserUsecase extends Usecase<DelLoginUserRequest,DelLoginUserResponse> {
    private LoginUserDbDataStore dataStore;
    @Inject
    public DelLoginUserUsecase(LoginUserDbDataStore dataStore){
        this.dataStore=dataStore;
    }
    @Override
    protected Observable<DelLoginUserResponse> buildUsecaseObservable(DelLoginUserRequest request) {
        //return dataStore.deleleLoginUser();
        return null;
    }
}
