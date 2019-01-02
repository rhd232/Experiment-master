package com.wind.base.http.page;

import com.wind.base.response.BaseResponse;
import com.wind.base.request.PageRequest;

import rx.Observable;

/**
 * Created by wind on 2017/2/27.
 */

public interface IPage<Q extends PageRequest,R extends BaseResponse> {

    Observable<R> loadPage(Q q);

    /**
     * 根据分页策略,处理第一个分页参数
     *
     * @param currPageIndex
     * @param pageSize
     * @return
     */
    int handlePageIndex(int currPageIndex, int pageSize);

    /**
     * 根据分页策略,处理第二个分页参数
     *
     * @param currPageIndex
     * @param pageSize
     * @return
     */
    int handlePage(int currPageIndex, int pageSize);
}
