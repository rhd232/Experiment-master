package com.wind.data.list;

import com.wind.base.api.IPageApi;
import com.wind.base.utils.MapTransformer;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by wind on 2018/9/5.
 */

public class ListPageApiProxy implements IPageApi<ListRequest,ListResponse> {
    private ListPageApi api;
    @Inject
    public ListPageApiProxy(ListPageApi api){
        this.api=api;
    }
    @Override
    public Observable<ListResponse> get(ListRequest request) {
        return api.get(MapTransformer.transformObject2Map(request));
    }
}
