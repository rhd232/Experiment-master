package com.wind.base.http.loader;

import com.wind.base.http.page.IPage;
import com.wind.base.request.PageRequest;
import com.wind.base.response.PageResponse;

import javax.inject.Inject;

import rx.Observable;

/**分页加载对象
 * Created by wind on 2017/2/27.
 */

public class PageLoader<Q extends PageRequest,R extends PageResponse> implements ILoader<Q,R> {

    private IPage mPage;
    @Inject
    public PageLoader(IPage page){
        this.mPage=page;
    }
    @Override
    public Observable<R> load(Q request) {
        return mPage.loadPage(request);
    }
}
