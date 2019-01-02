package com.wind.base.http.page;

import com.wind.base.api.IPageApi;
import com.wind.base.request.PageRequest;
import com.wind.base.response.PageResponse;

import javax.inject.Inject;

/**
 * 分页策略1: pageIndex, pageSize
 *
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 2015-09-29 21:54]
 */
public abstract class PsPage<Q extends PageRequest, R extends PageResponse> extends AbstractPage<Q,R> {


    public PsPage(IPageApi pageApi) {
        super(pageApi);
    }

    @Override
    public int handlePageIndex(int currPageIndex, int pageSize) {
        return ++currPageIndex;
    }

    @Override
    public int handlePage(int currPageIndex, int pageSize) {
        return pageSize;
    }
}
