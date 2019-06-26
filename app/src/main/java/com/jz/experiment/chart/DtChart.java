package com.jz.experiment.chart;

import android.graphics.Color;

import com.anitoa.util.AnitoaLogUtil;
import com.anitoa.well.Well;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.jz.experiment.R;
import com.jz.experiment.util.DataFileUtil;
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
    private String channelStr;
    public DtChart(LineChart chart,int cylingCount,FactUpdater factUpdater) {
        super(chart,factUpdater);
        channelStr=chart.getContext().getString(R.string.setup_channel);
        XAxis xAxis = chart.getXAxis();
      //  xAxis.setLabelCount(cylingCount + 4, true);
      //  xAxis.setAxisMaximum(60);
        xAxis.setAxisMaximum(cylingCount);

        YAxis yAxis=chart.getAxisLeft();
      //  yAxis.setAxisMinimum(-500);
        yAxis.setDrawGridLines(true);
    }
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam){
        show(ChanList,KSList,dataFile,ctParam,true);
    }
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam,boolean norm){
        InputStream ips = null;
        try {
            ips = new FileInputStream(dataFile);
            //ips = mChart.getContext().getAssets().open("2019_02_13_04_11_17_dt.txt");
            show(ChanList, KSList, ips,ctParam, norm);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void recordFactorData(String msg, double[][] factorData){
        StringBuilder sBuilder=new StringBuilder();
        sBuilder.append(msg);
        sBuilder.append("\n");
        sBuilder.append("[");
        for (int j=0;j<=3;j++) {
            if (j==0) {
                String s = channelStr + (j + 1);
                sBuilder.append(s);
                sBuilder.append("\n");
                for (int i = 0; i < 100; i++) {
                    double d = factorData[j][i];
                    if (i % 10 == 0) {
                        sBuilder.append("\n");
                    }
                    sBuilder.append(d).append(",");
                }
                sBuilder.append("\n");
            }
        }
        sBuilder.append("]");
        //System.out.println(sBuilder.toString());
        AnitoaLogUtil.writeToFile(DataFileUtil.getOrCreateFile("factor_log.txt"),sBuilder.toString());
    }
    public void show(List<String> ChanList, List<String> KSList,InputStream ips,CtParamInputLayout.CtParam ctParam,boolean norm) {

        //读取图像文件数据
        DataFileReader.getInstance().ReadFileData(ips,mRunning);
        if (mRunning) {
            mFactUpdater.updateFact();
           // CommData.m_factorData=DataFileReader.getInstance().factorValue;
           // recordFactorData("运行",CommData.m_factorData);
        }else {
            CommData.m_factorData=DataFileReader.getInstance().factorValue;
           // recordFactorData("离线",CommData.m_factorData);
        }
        CurveReader.getInstance().readCurve(CommData.m_factorData,ctParam,norm);

        // Map<Integer,List<List<String>>> chanMap= DataParser.parseDtData(dataFile);
        //请求服务器，解析图像版返回的原始数据

        mLegendEntries = new ArrayList<>();
        mLineColors.clear();
        mWellNames.clear();
        mDataSets.clear();

        for (String chan : ChanList) {
            for (String ks : KSList) {
                DrawLine(chan, 4, ks);
                // DrawLineFromServer(chan,ks,chanMap);
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
        int ksindex;


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


            float y = (float) CurveReader.getInstance().m_zData[currChan][ksindex][i];
            Entry entry = new Entry(i, y);
            expeData.add(entry);

        }

        LineDataSet dataSet = new LineDataSet(expeData, channelStr + (currChan + 1));
        dataSet.setColor(color);
        dataSet.setDrawCircles(false);
       // dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
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
}
