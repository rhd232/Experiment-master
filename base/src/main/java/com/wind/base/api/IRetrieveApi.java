package com.wind.base.api;

import com.wind.base.request.BaseRequest;
import com.wind.base.response.BaseResponse;

import rx.Observable;

/**
 * Created by wind on 16/5/20.
 */
public interface IRetrieveApi<Q extends BaseRequest,R extends BaseResponse> {

    Observable<R> get(Q request);


}
