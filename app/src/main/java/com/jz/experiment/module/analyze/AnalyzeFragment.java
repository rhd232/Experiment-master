package com.jz.experiment.module.analyze;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.R;
import com.jz.experiment.chart.CCurveShow;
import com.jz.experiment.chart.CCurveShowMet;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.chart.WindChart;
import com.jz.experiment.device.UnsupportedDeviceException;
import com.jz.experiment.device.Well;
import com.jz.experiment.module.data.FilterActivity;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.widget.CtParamInputLayout;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.utils.AppUtil;
import com.wind.base.utils.FileUtil;
import com.wind.toastlib.ToastUtil;
import com.wind.view.TitleBar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AnalyzeFragment extends CtFragment implements CtParamInputLayout.OnCtParamChangeListener {

    public static final int REQUEST_CODE_FILE = 1234;


    LineChart chart_line;
    Spinner spinner;
    List<String> ChanList = new ArrayList<>();
    List<String> KSList = new ArrayList<>();
    TitleBar mTitleBar;
    WindChart mChart;
    File mOpenedFile;

    CtParamInputLayout layout_ctparam_input;
    ExecutorService mExecutorService;


    ScrollView sv_pdf;

    Handler mHandler = new Handler();

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
        mExecutorService = Executors.newSingleThreadExecutor();
        EventBus.getDefault().register(this);
        layout_ctparam_input = view.findViewById(R.id.layout_ctparam_input);
        sv_pdf=view.findViewById(R.id.sv_pdf);
        layout_ctparam_input.setOnCtParamChangeListener(this);
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
        try {
            KSList = Well.getWell().getKsList();
        } catch (UnsupportedDeviceException e) {
            //第一次安装，没有文件读取权限导致
            e.printStackTrace();
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
        }

    }

    /**
     * 选择数据文件
     *
     * @param view
     */
    public void selectDataFile(View view) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
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

                    double[][] ctValues;
                    if ("熔解曲线".equals(item)) {
                        mChart = new MeltingChart(chart_line);
                        ctValues = CCurveShowMet.getInstance().m_CTValue;
                      /*  float f=Float.parseFloat(String.format("%f",40f));
                        mChart.setAxisMinimum(f);*/
                    } else {
                        //获取类型，是扩增曲线还是熔解曲线
                        mChart = new DtChart(chart_line, 40);
                        ctValues = CCurveShow.getInstance().m_CTValue;
                    }
                    mChart.show(ChanList, KSList, mOpenedFile, null);
                    KSList.clear();
                    KSList = Well.getWell().getKsList();
                    for (String chan : ChanList) {
                        for (String ks : KSList) {
                            getCtValue(chan, ks, ctValues);
                        }
                    }
                    notifyCtChanged();

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
        if (mOpenedFile != null && mChart != null)
            mChart.show(ChanList, KSList, mOpenedFile, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCtParamChanged(final CtParamInputLayout.CtParam ctParam) {

        if (mOpenedFile != null && mChart != null && mChart instanceof DtChart) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    mChart.show(ChanList, KSList, mOpenedFile, ctParam);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            double[][] ctValues = CCurveShow.getInstance().m_CTValue;
                            KSList.clear();
                            KSList = Well.getWell().getKsList();
                            for (String chan : ChanList) {
                                for (String ks : KSList) {
                                    getCtValue(chan, ks, ctValues);
                                }
                            }
                            notifyCtChanged();
                        }
                    });

                }
            });

        }
    }

    @OnClick(R.id.iv_pdf)
    public void onViewClick(View view) {
        switch (view.getId()) {
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

                                                LoadingDialogHelper.showOpLoading(getActivity());

                                                mHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        StringBuilder sPdfNameBuilder=new StringBuilder();
                                                        sPdfNameBuilder.append(mOpenedFile.getName().replace(".txt",""));
;                                                        if (mChart instanceof DtChart){
                                                            sPdfNameBuilder.append("变温扩增");
                                                        }else {
                                                            sPdfNameBuilder.append("熔解曲线");
                                                        }
                                                        sPdfNameBuilder.append(".pdf");
                                                        String pdfName = sPdfNameBuilder.toString();
                                                        //DataFileUtil.getPdfFileName(mExeperiment, false);
                                                        //生成pdf
                                                        generatePdf(pdfName)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Action1<Boolean>() {
                                                                    @Override
                                                                    public void call(Boolean aboolean) {

                                                                        LoadingDialogHelper.hideOpLoading();
                                                                        ToastUtil.showToast(getActivity(), "已导出");

                                                                    }
                                                                }, new Action1<Throwable>() {
                                                                    @Override
                                                                    public void call(Throwable throwable) {
                                                                        LoadingDialogHelper.hideOpLoading();
                                                                        throwable.printStackTrace();
                                                                        ToastUtil.showToast(getActivity(), "导出失败");
                                                                    }
                                                                });
                                                    }
                                                }, 3000);


                                            }


                                        });
                            }
                        }).start();
                break;
        }
    }


    private Observable<Boolean> generatePdf(final String pdfName) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                PdfDocument document = new PdfDocument();
                int width = AppUtil.getScreenWidth(getActivity());
                int height = 0;// AppUtil.getScreenHeight(getActivity());
                //计算scrollview的高度
                for (int i = 0; i < sv_pdf.getChildCount(); i++) {
                    height += sv_pdf.getChildAt(i).getHeight();
                }

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                        .Builder(width, height, 1)
                        .create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                sv_pdf.draw(canvas);
              /*  chart.draw(canvas);
                canvas.translate(0, chart.getHeight());

                gv_a.draw(canvas);
                canvas.translate(0, gv_a.getHeight());
                gv_b.draw(page.getCanvas());*/

                document.finishPage(page);
                // String pdfName = fileName + ".pdf";
                File file = new File(DataFileUtil.getPdfFilePath(pdfName));

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
}
