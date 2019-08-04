package com.jz.experiment.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;
import java.util.List;

public class StandardCurveChart {


    private CombinedChart mCombinedChart;
    public StandardCurveChart(CombinedChart combinedChart){
        this.mCombinedChart=combinedChart;
        Description description = new Description();
        description.setEnabled(false);
        mCombinedChart.setDescription(description);

        mCombinedChart.setNoDataText("");
        XAxis xAxis = mCombinedChart.getXAxis();
        xAxis.setEnabled(true);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 设置X轴的位置
        xAxis.setDrawGridLines(false); // 效果如下图
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisMinimum(20);

        YAxis yAxisRight = mCombinedChart.getAxisRight();
        yAxisRight.setAxisMinimum(0);
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = mCombinedChart.getAxisLeft();
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawGridLines(false);


        Legend legend = mCombinedChart.getLegend();
        legend.setEnabled(false);

        mCombinedChart.setDrawMarkers(false);
        mCombinedChart.setAutoScaleMinMaxEnabled(true);
        mCombinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });


        mCombinedChart.setTouchEnabled(false);
        mCombinedChart.setDoubleTapToZoomEnabled(false);
        //设置是否可以缩放 x和y，默认true
        mCombinedChart.setScaleEnabled(false);
    }

    public void setXAxisMinimum(float min){
        XAxis xAxis = mCombinedChart.getXAxis();
        xAxis.setAxisMinimum(min);
    }

    public void addPoints(double[] xx, double[] yy, double[] stdXX, double[] stdYY,double[] unknowXX, double[] unknowYY) {

        CombinedData data = new CombinedData();
        /*******直线数据*****************/
        int count = xx.length;
        List<Entry> expeData = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Entry entry = new Entry((float) xx[i],(float) yy[i]);
            expeData.add(entry);
        }

        LineDataSet dataSet = new LineDataSet(expeData,"标准曲线");
        dataSet.setColor(Color.parseColor("#1EA0DC"));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.enableDashedLine(10,4,0);
        LineData lineData = new LineData();
        lineData.addDataSet(dataSet);
        data.setData(lineData);

        /*******标准点*****************/
        ScatterData scatterData = new ScatterData();
        double minX=0;
        if (stdXX.length>0) {
             minX = stdXX[0];
        }
        List<Entry> pointList = new ArrayList<>();
        for (int i = 0; i < stdXX.length; i++) {
            Entry entry = new Entry((float) stdXX[i],(float) stdYY[i]);
            pointList.add(entry);

            if (stdXX[i]<minX){
                minX=stdXX[i];
            }
        }
        ScatterDataSet scatterDataSet=new ScatterDataSet(pointList,"标准点");
        scatterDataSet.setColor(Color.parseColor("#ffff8800"));
        scatterDataSet.setScatterShapeSize(7.5f);
        scatterDataSet.setDrawValues(false);
        scatterDataSet.setValueTextSize(10f);

        scatterData.addDataSet(scatterDataSet);


        /*******未知点*****************/
        //每种类型数据只能增加一次
        //===================未知点=======
        List<Entry> unknownPointList = new ArrayList<>();
        for (int i = 0; i < unknowXX.length; i++) {
            Entry entry = new Entry((float) unknowXX[i],(float) unknowYY[i]);
            unknownPointList.add(entry);

            if (unknowXX[i]<minX){
                minX=unknowXX[i];
            }
        }

        ScatterDataSet unknownScatterDataSet=new ScatterDataSet(unknownPointList,"未知点");
        unknownScatterDataSet.setColor(Color.parseColor("#1f4e99"));
        unknownScatterDataSet.setScatterShapeSize(7.5f);
        unknownScatterDataSet.setDrawValues(false);
        unknownScatterDataSet.setValueTextSize(10f);

        scatterData.addDataSet(unknownScatterDataSet);

        data.setData(scatterData);

        //设置x轴最小值，
        setXAxisMinimum((int)(minX-1));

        mCombinedChart.setData(data);
        data.notifyDataChanged();
        mCombinedChart.notifyDataSetChanged(); // let the chart know it's data changed
        mCombinedChart.invalidate();
    }

    public void clear() {
        double[] xx=new double[0];
        double[] yy=new double[0];

        double[] stdXX=new double[0];
        double[] stdYY=new double[0];

        double[] unknowXX=new double[0];
        double[] unknowYY=new double[0];

        addPoints(xx,yy,stdXX,stdYY,unknowXX,unknowYY);

    }
}
