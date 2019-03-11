package com.jz.experiment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.util.TrimReader;
import com.wind.base.BaseActivity;

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
        TrimReader.getInstance().ReadTrimFile(getActivity());
        CommData.ReadDatapositionFile(getActivity());
        MeltingChart meltingChart=new MeltingChart(chart);
        DtChart dtChart = new DtChart(chart_line_2, 13);
       /* DtChart dtChart2 = new DtChart(chart_line_2, 13);*/

        try {
          //  String name="fluorescence_data.txt";
          /*  InputStream ips=getAssets().open("2019_02_25_02_42_57_dt.txt");
            dtChart.show(ChanList, KSList, ips);*/
            InputStream ips2=getAssets().open("2019_03_08_18_22_18_melting.txt");
            meltingChart.show(ChanList, KSList, ips2);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
