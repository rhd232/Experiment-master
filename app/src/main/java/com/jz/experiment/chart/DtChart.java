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
import java.util.Map;

public class DtChart extends WindChart {

    public DtChart(LineChart chart,int cylingCount) {
        this(chart,cylingCount,null);
    }
    public DtChart(LineChart chart,int cylingCount,FactUpdater factUpdater) {
        super(chart,factUpdater);

        XAxis xAxis = chart.getXAxis();
      //  xAxis.setLabelCount(cylingCount + 4, true);
        xAxis.setAxisMaximum(60);

        YAxis yAxis=chart.getAxisLeft();
      //  yAxis.setAxisMinimum(-500);
        yAxis.setDrawGridLines(true);
    }

    public void show(List<String> ChanList, List<String> KSList,File dataFile){
        InputStream ips = null;
        try {
            ips = new FileInputStream(dataFile);
            //ips = mChart.getContext().getAssets().open("2019_02_13_04_11_17_dt.txt");
            show(ChanList, KSList, ips);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void show(List<String> ChanList, List<String> KSList,InputStream ips) {

        //读取图像文件数据
        DataFileReader.getInstance().ReadFileData(ips,mRunning);
        if (mRunning) {
            mFactUpdater.updateFact();
        }else {
            CommData.m_factorData=DataFileReader.getInstance().factorValue;
        }
        CurveReader.getInstance().readCurve(CommData.m_factorData);

        // Map<Integer,List<List<String>>> chanMap= DataParser.parseDtData(dataFile);
        //请求服务器，解析图像版返回的原始数据

        mLegendEntries = new ArrayList<>();
        mLineColors.clear();
        mDataSets.clear();

        for (String chan : ChanList) {
            for (String ks : KSList) {
                DrawLine(chan, 4, ks);
                // DrawLineFromServer(chan,ks,chanMap);
            }
        }

        Legend legend = mChart.getLegend();
        legend.setCustom(mLegendEntries);

        mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
    }

    public void DrawLineFromServer(String chan,String currks, Map<Integer,List<List<String>>> chanMap) {
        int currChan = 0;
        int ksindex = -1;

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
            legendEntry = new LegendEntry("通道" + (currChan + 1), Legend.LegendForm.LINE,
                    20, 4, null, color);
            if (!contains("通道" + (currChan + 1))){
                mLegendEntries.add(legendEntry);
            }
        }




        switch (currks) {
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
        List<String> lineData=chanMap.get(currChan).get(ksindex);

        List<Entry> expeData = new ArrayList<>();

        int count = lineData.size();

        for (int i = 0; i < count; i++) {

            float y = Float.parseFloat(lineData.get(i));
            Entry entry = new Entry(i, y);
            expeData.add(entry);

        }

        LineDataSet dataSet = new LineDataSet(expeData, "通道" + (currChan + 1));
        dataSet.setColor(color);
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        mLineColors.add(color);
        mDataSets.add(dataSet);

    }
    public void DrawLine(String chan, int ks, String currks) {
        if (!CommData.diclist.keySet().contains(chan) || CommData.diclist.get(chan).size() == 0)
            return;

        List<com.jz.experiment.chart.ChartData> cdlist = CommData.GetChartData(chan, 4, currks);
        if (cdlist.size() == 0) return;

       /* double y_max_value=Collections.max(cdlist, new Comparator<ChartData>() {
            @Override
            public int compare(ChartData o1, ChartData o2) {
                return o1.y-o2.y;
            }
        }).y;
        double y_min_value=Collections.max(cdlist, new Comparator<ChartData>() {
            @Override
            public int compare(ChartData o1, ChartData o2) {
                return o1.y-o2.y;
            }
        }).y;*/
        int currChan = 0;
        int ksindex = -1;


        int color = 0;
        LegendEntry legendEntry;
        switch (chan) {
            case "Chip#1":
                currChan = 0;
                color = Color.argb(255, 24, 60, 209);
                // color = new Color.valueOf(Color.FromRgb(24, 60, 209));

                break;
            case "Chip#2":
                currChan = 1;
                color = Color.argb(255, 83, 182, 97);
                //  dxcLs1.Brush = new SolidColorBrush(Color.FromRgb(83, 182, 97));

                break;
            case "Chip#3":
                currChan = 2;
                color = Color.argb(255, 245, 195, 66);
                // dxcLs1.Brush = new SolidColorBrush(Color.FromRgb(245, 195, 66));

                break;
            case "Chip#4":
                currChan = 3;
                color = Color.argb(255, 234, 51, 35);
                // dxcLs1.Brush = new SolidColorBrush(Color.FromRgb(234, 51, 35));
                break;
        }
        if (mLegendEntries.size() <= currChan) {

            legendEntry = new LegendEntry("通道" + (currChan + 1), Legend.LegendForm.LINE,
                    20, 4, null, color);
            if (!contains("通道" + (currChan + 1))){
                mLegendEntries.add(legendEntry);
            }


        }




        switch (currks) {
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
        List<Entry> expeData = new ArrayList<>();

        int count = cdlist.size();

        for (int i = 0; i < count; i++) {


            float y = (float) CurveReader.getInstance().m_zData[currChan][ksindex][i];
            Entry entry = new Entry(i, y);

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
