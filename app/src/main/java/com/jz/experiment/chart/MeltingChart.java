package com.jz.experiment.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.anitoa.well.Well;
import com.jz.experiment.widget.CtParamInputLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam) {
        show(ChanList,KSList,dataFile,ctParam,true);
    }

    @Override
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam,boolean norm) {

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
      //  if (!mRunning) {//读取历史文件的时候拟合曲线
            MeltCurveReader.getInstance().readCurve(CommData.m_factorData);
        //}

        mLegendEntries = new ArrayList<>();
        mLineColors.clear();
        mDataSets.clear();

        for (String chan : ChanList) {
            for (String ks : KSList) {
                DrawLine(chan, 4, ks);
            }
        }

      /*  Legend legend = mChart.getLegend();
        legend.setCustom(mLegendEntries);*/

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
        ksindex= Well.getWell().getWellIndex(currks);


        List<MeltChartData> cdlist = CommData.GetChartDataByRJQX(chan, 0, currks);
        //cdlist=avgDuplicate(cdlist);


        int count =cdlist.size();
        if (count==0){
            return;
        }

        List<Entry> expeData = new ArrayList<>();
        //从第三个数据开始绘制，前两个温度数据可能存在问题
        Set<String> xSet=new HashSet<>();//排除横坐标一样的数据
        for (int i=2;i<count;i++){
            String xV=cdlist.get(i).x;

            if (!xSet.contains(xV)) {
                xSet.add(xV);
                Float x = Float.parseFloat(xV);
                //  float y = (float) MeltCurveReader.getInstance().m_zData[currChan][ksindex][i];
                float y = (float) MeltCurveReader.getInstance().m_zdData[currChan][ksindex][i];


                Entry entry = new Entry(x, y);
                expeData.add(entry);
            }
        }


        LineDataSet dataSet = new LineDataSet(expeData, "通道" + (currChan + 1));
        dataSet.setColor(color);
        dataSet.setDrawCircles(false);
        //dataSet.setCubicIntensity();
        //dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
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

    public List<MeltChartData> avgDuplicate(List<MeltChartData> cdlist ){
        Map<String, List<String>> map=new LinkedHashMap<>();
        for (int k=0;k<cdlist.size();k++){
            String t=cdlist.get(k).x;
            if (!map.containsKey(t)){
                map.put(t,new ArrayList<String>());
            }
            map.get(t).add(cdlist.get(k).y);
        }
        List<MeltChartData> noDuplicateList =new ArrayList<>();
        for (Map.Entry<String,List<String>> entry:map.entrySet()){
            double sum = 0;
            for (int s = 0; s < entry.getValue().size(); s++) {
                sum += Double.parseDouble(entry.getValue().get(s));
            }
            double avg=sum/entry.getValue().size();
            MeltChartData meltChartData=new MeltChartData();
            meltChartData.x=entry.getKey();
            meltChartData.y=avg+"";
            noDuplicateList.add(meltChartData);
        }
        return noDuplicateList;
    }
}
