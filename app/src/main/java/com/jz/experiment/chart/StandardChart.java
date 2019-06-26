package com.jz.experiment.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.jz.experiment.widget.CtParamInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StandardChart extends WindChart {
    public StandardChart(LineChart chart) {
        super(chart);

        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(20);

        chart.setTouchEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
    }

    @Override
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam, boolean norm) {

    }

    @Override
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam) {

    }


    public void addPoints(double [] xx, double []yy,double [] sourceXX,double[] sourceYY) {
        mDataSets.clear();



        int count = xx.length;
        List<Entry> expeData = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Entry entry = new Entry((float) xx[i],(float) yy[i]);
            expeData.add(entry);
        }

        LineDataSet dataSet = new LineDataSet(expeData,"");
        dataSet.setColor(Color.parseColor("#1EA0DC"));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.enableDashedLine(10,4,0);
        mDataSets.add(dataSet);


       /* List<Entry> pointList = new ArrayList<>();
        for (int i = 0; i < sourceXX.length; i++) {
            Entry entry = new Entry((float) sourceXX[i],(float) sourceYY[i]);
            pointList.add(entry);
        }
        LineDataSet dataSetPoint = new LineDataSet(pointList,"点");
        dataSetPoint.setColor(Color.parseColor("#ffff8800"));
        dataSetPoint.setDrawCircles(true);
        dataSetPoint.setDrawValues(false);
        mDataSets.add(dataSetPoint);*/

        mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
    }
}
