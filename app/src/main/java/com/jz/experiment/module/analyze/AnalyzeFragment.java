package com.jz.experiment.module.analyze;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.chart.WindChart;
import com.jz.experiment.module.data.FilterActivity;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.utils.FileUtil;
import com.wind.toastlib.ToastUtil;
import com.wind.view.TitleBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeFragment extends BaseFragment {

    public static final int REQUEST_CODE_FILE = 1234;

    LineChart chart_line;
    Spinner spinner;
    List<String> ChanList = new ArrayList<>();
    List<String> KSList = new ArrayList<>();
    TitleBar mTitleBar;
    WindChart mChart;
    File mOpenedFile;
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_analyze;
    }

    private void initTitleBar() {
        mTitleBar.setTextColor(Color.WHITE);
        mTitleBar.setRightTextColor(Color.WHITE);
        mTitleBar.setLeftVisibility(View.GONE);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.color686868));
        mTitleBar.setTitle("分析");
        mTitleBar.setRightText("筛选");
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterActivity.start(getActivity());
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        chart_line = view.findViewById(R.id.chart_line);
        chart_line.setNoDataText("");
        mTitleBar = view.findViewById(R.id.title_bar);
        initTitleBar();
        view.findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDataFile(v);
            }
        });
        spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("onItemSelected:" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ChanList.add("Chip#1");
        ChanList.add("Chip#2");
        ChanList.add("Chip#3");
        ChanList.add("Chip#4");
        KSList.add("A1");
        KSList.add("A2");
        KSList.add("A3");
        KSList.add("A4");

        KSList.add("B1");
        KSList.add("B2");
        KSList.add("B3");
        KSList.add("B4");
    }

    /**
     * 选择数据文件
     *
     * @param view
     */
    public void selectDataFile(View view) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/txt");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件上传"), REQUEST_CODE_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtil.showToast(getActivity(), "请安装一个文件管理器");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILE) {//345选择文件的请求码
            if (data != null) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getData() == null) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    }
                    Uri uri = data.getData();
                    String path = FileUtil.getPath(getActivity(), uri);
                    mOpenedFile = new File(path);
                    String item = (String) spinner.getSelectedItem();

                    if ("熔解曲线".equals(item)) {
                        mChart = new MeltingChart(chart_line);
                    } else {
                        //获取类型，是扩增曲线还是熔解曲线
                        mChart = new DtChart(chart_line, 40);
                    }
                    mChart.show(ChanList, KSList, mOpenedFile);

                }
            }
        }
    }

    boolean mVisibleToUser;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mVisibleToUser = isVisibleToUser;
    }
    @Subscribe
    public void onFilterEvent(FilterEvent event) {
        if (!mVisibleToUser) {
            return;
        }
        ChanList =
                event.getChanList();
        for (String chan : ChanList) {
            if (chan.equals("Chip#1")) {
                CommData.cboChan1 = 1;
            } else {
                CommData.cboChan1 = 0;
            }
            if (chan.equals("Chip#2")) {
                CommData.cboChan2 = 1;
            } else {
                CommData.cboChan2 = 0;
            }
            if (chan.equals("Chip#3")) {
                CommData.cboChan3 = 1;
            } else {
                CommData.cboChan3 = 0;
            }
            if (chan.equals("Chip#4")) {
                CommData.cboChan4 = 1;
            } else {
                CommData.cboChan4 = 0;
            }
        }


        KSList =
                event.getKSList();

        showChart();
    }

    private void showChart() {
        if (mOpenedFile!=null && mChart!=null)
            mChart.show(ChanList, KSList, mOpenedFile);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
