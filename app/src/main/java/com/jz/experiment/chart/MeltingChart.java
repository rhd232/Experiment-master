package com.jz.experiment.chart;

import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.util.List;

public class MeltingChart extends WindChart {

    public MeltingChart(LineChart chart){
        super(chart);
    }
    public MeltingChart(LineChart chart,FactUpdater factUpdater) {
        super(chart,factUpdater);
    }

    @Override
    public void show(List<String> ChanList, List<String> KSList, File dataFile) {

    }
}
