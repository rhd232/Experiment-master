package com.wind.base.response;

import com.wind.base.adapter.DisplayItem;

import java.util.List;

/**
 * Created by wind on 2017/2/27.
 */

public class PageResponse<T extends DisplayItem> extends BaseResponse {

    private boolean isFirstPage;
    private List<T> items;

    public boolean isFirstPage() {
        return isFirstPage;
    }
    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
