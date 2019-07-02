package com.jz.experiment.module.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.wind.base.mvp.view.BaseFragment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportPreviewFragment extends BaseFragment {



    @BindView(R.id.dt_chart)
    LineChart dt_chart;


    DtChart mDtChart;
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_report_preview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);



        mDtChart = new DtChart(dt_chart, 40);

        List<String> ChanList = new ArrayList<>();
        List<String> KSList = new ArrayList<>();
        CommData.cboChan1 = 1;
        ChanList.add("Chip#1");

        CommData.cboChan2 = 1;
        ChanList.add("Chip#2");

        CommData.cboChan3 = 1;
        ChanList.add("Chip#3");

        CommData.cboChan4 = 1;
        ChanList.add("Chip#4");

        KSList.add("A1");
        KSList.add("A2");
        KSList.add("A3");
        KSList.add("A4");
        KSList.add("A5");
        KSList.add("A6");
        KSList.add("A7");
        KSList.add("A8");

        KSList.add("B1");
        KSList.add("B2");
        KSList.add("B3");
        KSList.add("B4");
        KSList.add("B5");
        KSList.add("B6");
        KSList.add("B7");
        KSList.add("B8");
        try {
            InputStream ips=getActivity().getAssets().open("ADCData_2019-06-22_044036.txt");
            mDtChart.show(ChanList, KSList, ips,null,true);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
