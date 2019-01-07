package com.jz.experiment.module.expe.event;

import com.wind.data.expe.bean.HistoryExperiment;

public class ToExpeSettingsEvent {

    private HistoryExperiment experiment;
    public ToExpeSettingsEvent(HistoryExperiment experiment){
        this.experiment=experiment;
    }

    public HistoryExperiment getExperiment() {
        return experiment;
    }
}
