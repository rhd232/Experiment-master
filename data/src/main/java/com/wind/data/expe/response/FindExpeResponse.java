package com.wind.data.expe.response;

import com.wind.base.response.BaseResponse;
import com.wind.data.expe.bean.HistoryExperiment;

import java.util.List;

public class FindExpeResponse extends BaseResponse {

    private List<HistoryExperiment> items;

    public List<HistoryExperiment> getItems() {
        return items;
    }

    public void setItems(List<HistoryExperiment> items) {
        this.items = items;
    }
}
