package com.wind.base.usecase;

import com.wind.base.repository.PageRepository;
import com.wind.base.request.PageRequest;
import com.wind.base.response.PageResponse;

import rx.Observable;

/**
 * Created by wind on 2017/2/27.
 */

public abstract class PageUsecase<Q extends PageRequest,R extends PageResponse> extends
        Usecase<Q,R> {


    private PageRepository<Q, R> mPageRepository;

    public PageUsecase(PageRepository<Q, R> pageRepository) {
        this.mPageRepository = pageRepository;
    }

    @Override
    protected Observable<R> buildUsecaseObservable(Q request) {
        return mPageRepository.loadPage(request);
    }
}
