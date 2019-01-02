package com.wind.base.http.page;

import com.wind.base.api.IPageApi;
import com.wind.base.request.PageRequest;
import com.wind.base.response.PageResponse;

import javax.inject.Inject;

/**
 * 分页策略2: startIndex, endIndex
 *
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 2015-09-29 21:54]
 */
public abstract class SePage<Q extends PageRequest, R extends PageResponse> extends AbstractPage<Q,R> {
    public SePage(IPageApi pageApi) {
        super(pageApi);
    }

    @Override
    public int handlePageIndex(int currPageIndex, int pageSize) {
        if (currPageIndex == getStartPageIndex() - 1) // 加载第一页数据(防止第一页使用"上拉加载更多")
        {
            return getStartPageIndex();
        }
        return currPageIndex + pageSize;
    }

    @Override
    public int handlePage(int currPageIndex, int pageSize) {
        return currPageIndex + pageSize - 1;
    }

    /**
     * 起始下标递减
     */
    public void decreaseStartIndex() {
        currPageIndex--;
        checkBound();
    }

    /**
     * 起始下标递减
     */
    public void decreaseStartIndex(int size) {
        currPageIndex -= size;
        checkBound();
    }

    /**
     * 边界检测
     */
    private void checkBound() {
        if (currPageIndex < getStartPageIndex() - pageSize) {
            currPageIndex = getStartPageIndex() - pageSize;
        }
    }
}
