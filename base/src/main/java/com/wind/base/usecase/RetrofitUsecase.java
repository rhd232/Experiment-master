package com.wind.base.usecase;

import com.wind.base.request.BaseRequest;
import com.wind.base.response.BaseResponse;

import retrofit2.Retrofit;

/**
 * Created by wind on 2018/5/19.
 */

public abstract class RetrofitUsecase<Q extends BaseRequest,R extends BaseResponse> extends Usecase<Q,R> {

    protected Retrofit mRetrofit;
    public RetrofitUsecase(Retrofit retrofit){
        this.mRetrofit=retrofit;
    }
}
