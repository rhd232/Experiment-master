package com.wind.base.repository;

import com.wind.base.request.BaseRequest;
import com.wind.base.response.BaseResponse;

import rx.Observable;

/**
 * Created by wind on 2018/3/20.
 */

public interface PageRepository<Q extends BaseRequest,R extends BaseResponse> {
    Observable<R> loadPage(Q q);
}
