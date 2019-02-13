package com.jz.experiment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.wind.base.BaseActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends BaseActivity {

    LineChart chart,chart_line_2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chart);
        chart = findViewById(R.id.chart_line);
        chart_line_2 = findViewById(R.id.chart_line_2);
        List<String> ChanList = new ArrayList<>();
        List<String> KSList = new ArrayList<>();
        ChanList.clear();

        CommData.cboChan1 = 1;
        ChanList.add("Chip#1");

        CommData.cboChan2 = 1;
        ChanList.add("Chip#2");

        CommData.cboChan3 = 1;
        ChanList.add("Chip#3");

        CommData.cboChan4 = 1;
        ChanList.add("Chip#4");


        KSList.clear();
        KSList.add("A1");
        KSList.add("A2");
        KSList.add("A3");
        KSList.add("A4");

        KSList.add("B1");
        KSList.add("B2");
        KSList.add("B3");
        KSList.add("B4");
        CommData.ReadDatapositionFile(getActivity());
        DtChart dtChart = new DtChart(chart, 13);
        DtChart dtChart2 = new DtChart(chart_line_2, 13);

        try {
            InputStream ips=getAssets().open("Fluorescence_Data_2019-02-13_112107.txt");
            dtChart.show(ChanList, KSList, ips);

            InputStream ips2=getAssets().open("2019_02_13_11_32_41_dt.txt");
            dtChart2.show(ChanList, KSList, ips2);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
