package com.jz.experiment.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MeltingChart extends WindChart {

    public MeltingChart(LineChart chart){
        this(chart,null);
    }
    public MeltingChart(LineChart chart,FactUpdater factUpdater) {
        super(chart,factUpdater);


        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMaximum(100);
        YAxis yAxis=chart.getAxisLeft();
        yAxis.setDrawGridLines(true);
    }

    @Override
    public void show(List<String> ChanList, List<String> KSList, File dataFile) {

        InputStream ips = null;
        try {
            ips = new FileInputStream(dataFile);
            //ips = mChart.getContext().getAssets().open("2019_02_15_01_42_12_melting.txt");
            show(ChanList, KSList, ips);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void show(List<String> ChanList, List<String> KSList,InputStream ips) {
        DataFileReader.getInstance().ReadFileData(ips,mRunning);

        if (mRunning) {
            mFactUpdater.updateFact();
        }else {
            CommData.m_factorData=DataFileReader.getInstance().factorValue;
        }

        MeltCurveReader.getInstance().readCurve(CommData.m_factorData);

        mLegendEntries = new ArrayList<>();
        mLineColors.clear();
        mDataSets.clear();

        for (String chan : ChanList) {
            for (String ks : KSList) {
                DrawLine(chan, 4, ks);
            }
        }

        Legend legend = mChart.getLegend();
        legend.setCustom(mLegendEntries);

        mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
    }

    public void DrawLine(String chan, int ks, String currks) {
        if (!CommData.diclist.keySet().contains(chan) || CommData.diclist.get(chan).size() == 0)
            return;



        int color = 0;
        int currChan = 0;
        int ksindex = -1;
        LegendEntry legendEntry;
        switch (chan)
        {
            case "Chip#1":
                currChan = 0;
                color = Color.argb(255, 24, 60, 209);
                break;
            case "Chip#2":
                currChan = 1;
                color = Color.argb(255, 83, 182, 97);
                break;
            case "Chip#3":
                currChan = 2;
                color = Color.argb(255, 245, 195, 66);
                break;
            case "Chip#4":
                currChan = 3;
                color = Color.argb(255, 234, 51, 35);
                break;
        }

        if (mLegendEntries.size() <= currChan) {

            legendEntry = new LegendEntry("通道" + (currChan + 1), Legend.LegendForm.LINE,
                    20, 4, null, color);
            if (!contains("通道" + (currChan + 1))){
                mLegendEntries.add(legendEntry);
            }


        }
        switch (currks)
        {
            case "A1":
                ksindex = 0;
                break;
            case "A2":
                ksindex = 1;
                break;
            case "A3":
                ksindex = 2;
                break;
            case "A4":
                ksindex = 3;
                break;
            case "B1":
                ksindex = 4;
                break;
            case "B2":
                ksindex = 5;
                break;
            case "B3":
                ksindex = 6;
                break;
            case "B4":
                ksindex = 7;
                break;
        }

        List<MeltChartData> cdlist = CommData.GetChartDataByRJQX(chan, 0, currks);

        int count =cdlist.size();
        if (count==0){
            return;
        }

        List<Entry> expeData = new ArrayList<>();

        for (int i=0;i<count;i++){
            float x=Float.parseFloat(cdlist.get(i).x);
            float y = (float) MeltCurveReader.getInstance().m_zData[currChan][ksindex][i];
            Entry entry = new Entry(x, y);
            expeData.add(entry);
        }


        LineDataSet dataSet = new LineDataSet(expeData, "通道" + (currChan + 1));
        dataSet.setColor(color);
        dataSet.setDrawCircles(false);
        // dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        mLineColors.add(color);
        mDataSets.add(dataSet);

    }


    private boolean contains(String s) {
        boolean contains=false;
        for (LegendEntry entry:mLegendEntries){
            if (s.equals(entry.label)){
                contains=true;
                break;
            }
        }
        return contains;
    }
}
