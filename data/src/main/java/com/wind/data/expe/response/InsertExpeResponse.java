package com.wind.data.expe.response;

import com.wind.base.response.BaseResponse;
import com.wind.data.expe.bean.HistoryExperiment;

public class InsertExpeResponse extends BaseResponse {

    private HistoryExperiment experiment;

    public HistoryExperiment getExperiment() {
        return experiment;
    }

    public void setExperiment(HistoryExperiment experiment) {
        this.experiment = experiment;
    }
}
