package com.wind.data.expe.request;

import com.wind.base.response.BaseResponse;
import com.wind.data.expe.bean.HistoryExperiment;

public class FindExpeByIdResponse extends BaseResponse {

    private HistoryExperiment data;

    public HistoryExperiment getData() {
        return data;
    }

    public void setData(HistoryExperiment data) {
        this.data = data;
    }
}
