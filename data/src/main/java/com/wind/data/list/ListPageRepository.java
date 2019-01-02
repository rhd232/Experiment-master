package com.wind.data.list;

import com.wind.base.repository.PageRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by wind on 2018/9/5.
 */

public class ListPageRepository implements PageRepository<ListRequest,ListResponse> {
    ListPageApiProxy apiProxy;
    @Inject
    public ListPageRepository(ListPageApiProxy apiProxy){
        this.apiProxy=apiProxy;
    }
    @Override
    public Observable<ListResponse> loadPage(ListRequest listRequest) {
        return apiProxy.get(listRequest);
    }
}
