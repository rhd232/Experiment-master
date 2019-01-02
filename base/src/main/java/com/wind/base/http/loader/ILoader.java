package com.wind.base.http.loader;

import com.wind.base.request.BaseRequest;
import com.wind.base.response.BaseResponse;

import rx.Observable;

/**
 * Created by wind on 2017/2/27.
 */

public interface ILoader<Q extends BaseRequest,R extends BaseResponse> {

    Observable<R> load(Q request);
}
