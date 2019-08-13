package com.jz.experiment.chart;

import android.graphics.Color;

import com.anitoa.well.Well;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.jz.experiment.R;
import com.jz.experiment.widget.CtParamInputLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DtChart extends WindChart {

    public DtChart(LineChart chart,int cylingCount) {
        this(chart,cylingCount,null);
    }

    private static float FIRST_AXIS_MAX=5000;
    private String channelStr;
    YAxis yAxis;
    public DtChart(LineChart chart,int cylingCount,FactUpdater factUpdater) {
        super(chart,factUpdater);
        channelStr=chart.getContext().getString(R.string.setup_channel);
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMaximum(cylingCount);

        yAxis=chart.getAxisLeft();
        yAxis.setAxisMinimum(-500);
        yAxis.setAxisMaximum(FIRST_AXIS_MAX);
        yAxis.setDrawGridLines(true);
    }
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam){
        show(ChanList,KSList,dataFile,ctParam,true);
    }
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam,boolean norm){
        InputStream ips;
        try {
            ips = new FileInputStream(dataFile);
            show(ChanList, KSList, ips,ctParam, norm);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private DtData mDtData;

    public DtData getDtData() {
        return mDtData;
    }

    public void show(List<String> ChanList, List<String> KSList, InputStream ips, CtParamInputLayout.CtParam ctParam, boolean norm) {

        //读取图像文件数据
        DataFileReader.getInstance().ReadFileData(ips,mRunning);
        if (mRunning) {
            mFactUpdater.setPcr(true);
            mFactUpdater.updateFact();
            norm=false;//运行时norm始终为false
        }
        CurveReader reader=new CurveReader();
        mDtData=reader.readCurve(ctParam,norm);

        mLegendEntries = new ArrayList<>();
        mLineColors.clear();
        mWellNames.clear();
        mDataSets.clear();

        for (String chan : ChanList) {
            for (String ks : KSList) {
                DrawLine(chan, 4, ks);
            }
        }
       /* Legend legend = mChart.getLegend();
        legend.setCustom(mLegendEntries);*/

        mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
    }

    public void DrawLine(String chan, int ks, String currks) {
        if (!CommData.diclist.keySet().contains(chan) || CommData.diclist.get(chan).size() == 0)
            return;

        List<com.jz.experiment.chart.ChartData> cdlist = CommData.GetChartData(chan, 4, currks);
        if (cdlist.size() == 0) return;

        int currChan = 0;
        int ksindex;


        int color = 0;
        LegendEntry legendEntry;
        switch (chan) {
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

            legendEntry = new LegendEntry(channelStr + (currChan + 1), Legend.LegendForm.LINE,
                    20, 4, null, color);
            if (!contains(channelStr + (currChan + 1))){
                mLegendEntries.add(legendEntry);
            }

        }

        ksindex= Well.getWell().getWellIndex(currks);

        List<Entry> expeData = new ArrayList<>();

        int count = cdlist.size();

        for (int i = 0; i < count; i++) {


            float y = (float) mDtData.m_zData[currChan][ksindex][i];

            if (y>FIRST_AXIS_MAX){
                yAxis.resetAxisMaximum();
            }
            Entry entry = new Entry(i, y);
            expeData.add(entry);

        }

        LineDataSet dataSet = new LineDataSet(expeData, channelStr + (currChan + 1));
        dataSet.setColor(color);
        dataSet.setDrawCircles(false);

        dataSet.setDrawValues(false);
        mLineColors.add(color);
        mWellNames.add(currks);
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


    public static class DtData{

        public double[][][] m_zData;
        public double[][] m_CTValue;
        public boolean [][]m_falsePositive;
    }
}
