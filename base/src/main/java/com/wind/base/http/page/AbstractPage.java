package com.wind.base.http.page;

import com.wind.base.api.IPageApi;
import com.wind.base.request.PageRequest;
import com.wind.base.response.PageResponse;

import rx.Observable;
import rx.functions.Action1;

/**
 * 分页
 *
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 16/8/25 17:33]
 */
public abstract class AbstractPage<Q extends PageRequest, R extends PageResponse> implements IPage<Q, R> {
    // 默认起始页下标
    public static final int DEFAULT_START_PAGE_INDEX = 1;
    // 默认分页大小
    public static final int DEFAULT_PAGE_SIZE = 20;

    protected int currPageIndex; // 当前页下标
    int lastPageIndex; // 记录上一次的页下标
    protected int pageSize; // 分页大小
    boolean isLoading; // 是否正在加载
    Object lock = new Object(); // 锁

    private IPageApi mApi;
    public AbstractPage(IPageApi pageApi) {
        initPageConfig();
        this.mApi=pageApi;
    }

    /**
     * 加载分页数据
     * 分页策略1:[param1, param2] = [pageIndex, pageSize]
     * 分页策略2:[param1, param2] = [startIndex, endIndex]
     *
     * @param param1
     * @param param2
     */


    /**
     * 初始化分页参数
     */
    private void initPageConfig() {
        currPageIndex = getStartPageIndex() - 1;
        lastPageIndex = currPageIndex;
        pageSize = getPageSize();
        isLoading = false;
    }

    /**
     * 返回起始页下标
     *
     * @return
     */
    protected int getStartPageIndex() {
        return DEFAULT_START_PAGE_INDEX;
    }

    /**
     * 返回分页大小
     *
     * @return
     */
    protected int getPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    /**
     * 设置起始页下标
     *
     * @param startPageIndex
     * @return
     */
    public AbstractPage setStartPageIndex(int startPageIndex) {
        currPageIndex = startPageIndex - 1;
        return this;
    }

    /**
     * 设置分页大小
     *
     * @param pageSize
     * @return
     */
    public AbstractPage setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 当前是否是第一页数据
     *
     * @return
     */
    public boolean isFirstPage() {
        return currPageIndex <= getStartPageIndex();
    }


    /*
       分页加载数据
     * 可能会抛出异常，请确认数据加载结束后，你已经调用了finishLoad(boolean success)方法
     */
    @Override
    public Observable<R> loadPage(Q request) {
        synchronized (lock) {
            if (isLoading) // 如果正在加载数据，则抛出异常
            {
                throw new RuntimeException();
            } else {
                isLoading = true;
            }
        }
        if (request.isFirstPage()) // 加载第一页数据
        {
            currPageIndex = getStartPageIndex();
        } else {
            currPageIndex = handlePageIndex(currPageIndex, pageSize);
        }
        request.setStartPageIndex(currPageIndex);
        request.setPageSize(pageSize);
        return load(request);
    }

    public Observable<R> load(final Q request) {
        return mApi.get(request).doOnNext(new Action1<R>() {
            @Override
            public void call(R response) {
                response.setFirstPage(request.isFirstPage());
                finishLoad(true);
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                finishLoad(false);
            }
        });
    }

    /**
     * 加载结束
     *
     * @param success true：加载成功  false：失败(无数据)
     */
    public void finishLoad(boolean success) {
        synchronized (lock) {
            isLoading = false;
        }
        if (success) {
            lastPageIndex = currPageIndex;
        } else {
            currPageIndex = lastPageIndex;
        }
    }

    /**
     * 起始下标递减
     */
    public void decreaseStartIndex() {
    }

    /**
     * 起始下标递减
     */
    public void decreaseStartIndex(int size) {
    }
}
