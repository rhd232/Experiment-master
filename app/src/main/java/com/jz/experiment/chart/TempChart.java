package com.jz.experiment.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.jz.experiment.R;
import com.jz.experiment.widget.CtParamInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TempChart extends WindChart {

    public TempChart(LineChart chart) {
        super(chart);

        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(0);
    }

    @Override
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam, boolean norm) {

    }

    @Override
    public void show(List<String> ChanList, List<String> KSList, File dataFile, CtParamInputLayout.CtParam ctParam) {


    }


    public static final int LID_KEY = 0;
    public static final int PELTIER_KEY = 1;

    public void addTemp(float lid, float peltier) {
      /*  List<Float> lidTemps=tempArray.get(LID_KEY);
        if (lidTemps==null){
            tempArray.put(LID_KEY,lidTemps=new ArrayList<>());
        }
        lidTemps.add(lid);


        List<Float> peltierTemps=tempArray.get(PELTIER_KEY);
        if (peltierTemps==null){
            tempArray.put(PELTIER_KEY,peltierTemps=new ArrayList<>());
        }
        peltierTemps.add(peltier);*/
        LineDataSet lidDataSet;
        LineDataSet peltierDataSet;
        if (mDataSets.isEmpty()) {


            List<Entry> lidData = new ArrayList<>();
            lidDataSet = new LineDataSet(lidData, "热盖温度");
            lidDataSet.setColor(Color.BLUE);
            lidDataSet.setDrawCircles(false);
            mDataSets.add(LID_KEY, lidDataSet);

            List<Entry> peltierData = new ArrayList<>();
            peltierDataSet = new LineDataSet(peltierData, "温度");
            peltierDataSet.setColor(Color.RED);
            peltierDataSet.setDrawCircles(false);
            mDataSets.add(PELTIER_KEY, peltierDataSet);

        } else {
            lidDataSet = (LineDataSet) mDataSets.get(LID_KEY);
            peltierDataSet = (LineDataSet) mDataSets.get(PELTIER_KEY);
        }

        Entry entry = new Entry();
        entry.setX(lidDataSet.getValues().size());
        entry.setY(lid);
        lidDataSet.addEntry(entry);
        lidDataSet.setDrawValues(false);

        Entry pentry = new Entry();
        pentry.setX(peltierDataSet.getValues().size());
        pentry.setY(peltier);
        peltierDataSet.addEntry(pentry);
        peltierDataSet.setDrawValues(false);
        if (mLegendEntries == null || mLegendEntries.isEmpty()) {
            mLegendEntries = new ArrayList<>();
            String lidT=mChart.getContext().getString(R.string.running_lid_temp);
            LegendEntry lidlegendEntry = new LegendEntry(lidT, Legend.LegendForm.LINE,
                    20, 4, null, Color.BLUE);
            mLegendEntries.add(lidlegendEntry);

            String heaterT=mChart.getContext().getString(R.string.running_heater_temp);
            LegendEntry plegendEntry = new LegendEntry(heaterT, Legend.LegendForm.LINE,
                    20, 4, null, Color.RED);
            mLegendEntries.add(plegendEntry);

          /*  Legend legend = mChart.getLegend();
            legend.setCustom(mLegendEntries);*/
        }


        mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
    }
}
