package com.jz.experiment.chart;

import android.os.Handler;
import android.os.Message;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jz.experiment.widget.ChartMarkerView;
import com.wind.data.expe.bean.ColorfulEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class WindChart {
    protected List<ILineDataSet> mDataSets;
    protected List<Integer> mLineColors;
    protected LineData mLineData;
    private ChartMarkerView mChartMarkerView;
    protected LineChart mChart;
    protected List<LegendEntry> mLegendEntries;

    protected FactUpdater mFactUpdater;
    public WindChart(LineChart chart){
        this(chart,null);
    }
    public WindChart(LineChart chart,FactUpdater factUpdater){
        this.mChart=chart;
        this.mFactUpdater=factUpdater;
        mDataSets = new ArrayList<>();
        mLineColors = new ArrayList<>();
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 设置X轴的位置
        xAxis.setDrawGridLines(false); // 效果如下图
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);
    /*    xAxis.setLabelCount(getCurCyclingStage().getCyclingCount() + 2, true);
        // xAxis.setAxisLineWidth(1);
        xAxis.setAxisMaximum(getCurCyclingStage().getCyclingCount() + 2);*/
        xAxis.setAxisMinimum(0);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisLeft.setDrawGridLines(false);


        Description description = new Description();
        description.setEnabled(false);
        chart.setDescription(description);
        Legend legend = chart.getLegend();
        // legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        mLineData = new LineData(mDataSets);

        chart.setMarker(mChartMarkerView = new ChartMarkerView(chart.getContext(), new ChartMarkerView.OnPointSelectedListener() {
            @Override
            public void onPointSelected(Entry e) {

                int index = -1;
                for (int i = 0; i < mDataSets.size(); i++) {
                    LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                    index = lineDataSet.getEntryIndex(e);
                    if (index != -1) {
                        break;
                    }
                }
                if (index == -1) {
                    return;
                }
                List<ColorfulEntry> entries = new ArrayList<>();
                for (int i = 0; i < mDataSets.size(); i++) {
                    try {
                        LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                        Entry entry = lineDataSet.getEntryForIndex(index);
                        ColorfulEntry colorfulEntry = new ColorfulEntry();
                        colorfulEntry.setEntry(entry);
                        colorfulEntry.setColor(mLineColors.get(i));
                        entries.add(colorfulEntry);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                mChartMarkerView.getAdapter().replaceAll(entries);

            }
        }));
        // chart.setTouchEnabled(false);

        //设置是否可以通过双击屏幕放大图表。默认是true
        chart.setDoubleTapToZoomEnabled(false);
        //设置是否可以缩放 x和y，默认true
        chart.setScaleEnabled(false);
        chart.setDrawBorders(false);
        chart.setData(mLineData);

        chart.invalidate(); // refresh
    }
    public static final int WHAT_REFRESH_CHART = 1234;

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_REFRESH_CHART:
                    //  synchronized (ExpeRunningActivity.this) {
                    mChart.setDrawMarkers(false);
                    mChart.setAutoScaleMinMaxEnabled(true);
                    mLineData.notifyDataChanged();
                    mChart.notifyDataSetChanged(); // let the chart know it's data changed
                    mChart.invalidate(); // refresh
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mChart.setDrawMarkers(true);
                        }
                    },200);
                    mChart.setDrawMarkers(true);
                    //}
                    break;
            }

        }
    };
    protected boolean mRunning;
    public void setRunning(boolean running){
        this.mRunning=running;
    }
    public abstract void show(List<String> ChanList, List<String> KSList,File dataFile);
}