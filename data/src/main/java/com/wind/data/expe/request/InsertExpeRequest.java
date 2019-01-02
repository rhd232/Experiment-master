package com.wind.data.expe.request;

import com.wind.base.request.BaseRequest;
import com.wind.data.expe.bean.HistoryExperiment;

public class InsertExpeRequest extends BaseRequest {

    private HistoryExperiment experiment;

    public HistoryExperiment getExperiment() {
        return experiment;
    }

    public void setExperiment(HistoryExperiment experiment) {
        this.experiment = experiment;
    }
}
