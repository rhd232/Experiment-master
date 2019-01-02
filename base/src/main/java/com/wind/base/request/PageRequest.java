package com.wind.base.request;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by wind on 2017/2/27.
 */

public class PageRequest extends BaseRequest {
    /**
     * 是否是第一页
     */
    private boolean isFirstPage;
    /**
     * 开始页码
     */
    @JSONField(name = "page")
    private int startPageIndex;
    /**
     * 每页大小
     */
    @JSONField(name = "count")
    private int pageSize;


    @JSONField(name = "max_time")
    private String maxTime;

    public int getStartPageIndex() {
        return startPageIndex;
    }

    public void setStartPageIndex(int startPageIndex) {
        this.startPageIndex = startPageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }
}
