package com.jz.experiment.module.data;

import android.Manifest;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jz.experiment.R;
import com.jz.experiment.module.data.adapter.ChannelDataAdapter;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.utils.AppUtil;
import com.wind.data.expe.bean.ChannelData;
import com.wind.data.expe.bean.ChartData;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.toastlib.ToastUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ExpeDataFragment extends BaseFragment {

    public static final String ARGS_KEY_EXPE = "args_key_expe";
    @BindView(R.id.tv_dt)
    TextView tv_dt;
    @BindView(R.id.tv_melt)
    TextView tv_melt;

    @BindView(R.id.gv_a)
    GridView gv_a;

    @BindView(R.id.gv_b)
    GridView gv_b;

    @BindView(R.id.chart)
    LineChart chart;

    LineData mLineData;
    ArrayList<ILineDataSet> mDataSets;

    private HistoryExperiment mExeperiment;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_expe_data;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mExeperiment = getArguments().getParcelable(ARGS_KEY_EXPE);
        inflateChart();
        tv_dt.setActivated(true);
        tv_melt.setActivated(false);
        GridView[] gvs = new GridView[2];
        gvs[0] = gv_a;
        gvs[1] = gv_b;
        String[] titles = {"A", "B"};
        buildChannelData(gvs, titles);
    }

    private void inflateChart() {
        ChartData chartData = mExeperiment.getDtChartData();
        List<com.wind.data.expe.bean.LineData> lineDataList = chartData.getLineDataList();
        mDataSets = new ArrayList<>();
        for (int i = 0; i < lineDataList.size(); i++) {//4组数据
            com.wind.data.expe.bean.LineData lineData = lineDataList.get(i);
            List<Entry> entries = lineData.getEntries();

            LineDataSet dataSet = new LineDataSet(entries, "通道" + (i + 1));
            dataSet.setDrawCircles(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            mDataSets.add(dataSet);
        }

        setupChartStyle();

        mLineData = new LineData(mDataSets);
        // chart.setMarker(new ChartMarkerView(getActivity()));
        chart.setDrawBorders(false);
        chart.setData(mLineData);
        chart.animateX(1500);
        chart.invalidate(); // refresh
    }

    private void setupChartStyle() {

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 设置X轴的位置
        xAxis.setDrawGridLines(false); // 效果如下图
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);

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


    }

    private void buildChannelData(GridView[] gvs, String[] titles) {
        for (int k = 0; k < gvs.length; k++) {
            ChannelDataAdapter channelDataAdapter = new ChannelDataAdapter(getActivity(), R.layout.item_channel_data);
            gvs[k].setAdapter(channelDataAdapter);
            List<ChannelData> channelDataAList = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                for (int i = 0; i < 9; i++) {

                    String channelName = "";
                    switch (j) {
                        case 0:
                            channelName = titles[k];
                            break;
                        case 1:
                            channelName = "通道1";
                            break;
                        case 2:
                            channelName = "通道2";
                            break;
                        case 3:
                            channelName = "通道3";
                            break;
                        case 4:
                            channelName = "通道4";
                            break;
                    }
                    ChannelData channelData = new ChannelData(channelName, i, "");
                    channelDataAList.add(channelData);
                }
            }
            channelDataAdapter.replaceAll(channelDataAList);
        }
    }


    public static ExpeDataFragment newInstance(HistoryExperiment experiment) {
        ExpeDataFragment f = new ExpeDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_KEY_EXPE, experiment);
        f.setArguments(args);
        return f;

    }

    public boolean isSavedExpe() {
        return mExeperiment.getId() != HistoryExperiment.ID_NONE;
    }


    @OnClick({R.id.iv_pdf, R.id.iv_save})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_save:
                ExperimentStatus status = new ExperimentStatus();
                status.setStatus(ExperimentStatus.STATUS_COMPLETED);
                status.setDesc("已完成");
                mExeperiment.setStatus(status);
                //新插入一条数据
                mExeperiment.setId(HistoryExperiment.ID_NONE);
              /*  ExpeDataStore
                        .getInstance(ProviderModule.getInstance().getBriteDb(getActivity().getApplicationContext()))

                        .insertExpe();*/


                break;
            case R.id.iv_pdf:
                AndPermission.with(this)
                        .runtime()
                        .permission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE})
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                AppDialogHelper.showNormalDialog(getActivity(),
                                        "确定要导出pdf吗？", new AppDialogHelper.DialogOperCallback() {
                                            @Override
                                            public void onDialogConfirmClick() {
                                                //生成pdf
                                                generatePdf()
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Action1<Boolean>() {
                                                            @Override
                                                            public void call(Boolean aboolean) {
                                                                ToastUtil.showToast(getActivity(), "已导出");
                                                            }
                                                        }, new Action1<Throwable>() {
                                                            @Override
                                                            public void call(Throwable throwable) {
                                                                ToastUtil.showToast(getActivity(), "导出失败");
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }).start();


                break;
        }
    }

    private Observable<Boolean> generatePdf() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                PdfDocument document = new PdfDocument();
                int width = AppUtil.getScreenWidth(getActivity());
                int height = AppUtil.getScreenHeight(getActivity());
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                        .Builder(width, height, 1)
                        .create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                chart.draw(canvas);
                canvas.translate(0, chart.getHeight());

                gv_a.draw(canvas);
                canvas.translate(0, gv_a.getHeight());
                gv_b.draw(page.getCanvas());

                document.finishPage(page);
                String pdfName = System.currentTimeMillis() + ".pdf";
                File file = new File(getPdfFilePath(pdfName));

                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    document.writeTo(outputStream);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                document.close();

            }
        });


    }

    private String getPdfFilePath(String pdfName) {
        try {
            String dir = Environment.getExternalStorageDirectory() + "/anito/";
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File pdfFile = new File(dir, pdfName);
            if (pdfFile.exists()) {
                pdfFile.createNewFile();
            }
            return pdfFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }
}
