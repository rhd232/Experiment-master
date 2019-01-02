package com.wind.base.http.loader;

import com.wind.base.api.IItemApi;
import com.wind.base.request.BaseRequest;
import com.wind.base.response.BaseResponse;

import rx.Observable;

/**
 * Created by wind on 2017/2/28.
 */

public abstract class ItemLoader<Q extends BaseRequest,R extends BaseResponse> implements ILoader<Q,R> {

    private IItemApi mItemApi;
    public ItemLoader(IItemApi api){
        this.mItemApi=api;
    }
    @Override
    public Observable<R> load(Q request) {
        return mItemApi.get(request);
    }
}
