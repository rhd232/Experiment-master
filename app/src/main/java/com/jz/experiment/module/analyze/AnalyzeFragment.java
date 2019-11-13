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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.anitoa.well.Well;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.jz.experiment.R;
import com.jz.experiment.chart.CCurveShowPolyFit;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.chart.WindChart;
import com.jz.experiment.module.data.FilterActivity;
import com.jz.experiment.module.data.StandardCurveActivity;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.jz.experiment.module.report.PcrPrintPreviewActivity;
import com.jz.experiment.module.report.bean.InputParams;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.ExpeJsonGenerator;
import com.jz.experiment.widget.CtParamInputLayout;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.utils.AppUtil;
import com.wind.base.utils.FileUtil;
import com.wind.base.utils.JsonParser;
import com.wind.data.expe.bean.ExpeJsonBean;
import com.wind.data.expe.bean.ExpeSettingsFirstInfo;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Sample;
import com.wind.toastlib.ToastUtil;
import com.wind.view.TitleBar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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

    TitleBar mTitleBar;
    WindChart mChart;
    File mOpenedFile;

    CtParamInputLayout layout_ctparam_input;
    ExecutorService mExecutorService;


    CheckBox cb_norm;

    ScrollView sv_pdf;

    Handler mHandler = new Handler();

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_analyze;
    }

    private void initTitleBar() {
        mTitleBar.setTextColor(Color.BLACK);
        mTitleBar.setRightTextColor(Color.BLACK);
        mTitleBar.setLeftVisibility(View.GONE);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.white));

        mTitleBar.setTitle(getString(R.string.title_analyze));
//        mTitleBar.setRightText(getString(R.string.running_filter));
        mTitleBar.setRightIcon(R.drawable.btn_selector_filter);
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
        sv_pdf = view.findViewById(R.id.sv_pdf);
        cb_norm = view.findViewById(R.id.cb_norm);
        cb_norm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mChart instanceof DtChart) {

                    YAxis yAxis=chart_line.getAxisLeft();
                    yAxis.setAxisMaximum(5000);
                }
                showChart();

            }
        });
        layout_ctparam_input.setOnCtParamChangeListener(this);
        chart_line = view.findViewById(R.id.chart_line);
        chart_line.setNoDataText("");
        mTitleBar = view.findViewById(R.id.title_bar);
        initTitleBar();
        view.findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // AppDialogHelper.showSelectJsonTip();
                selectDataFile(v);
            }
        });
        spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //System.out.println("onItemSelected:" + position);

               switchMode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initChanAndKs();
        System.out.println("AnalyzeFragment onViewCreated");
    }


    /**
     * 选择数据文件
     *
     * @param view
     */
    public void selectDataFile(View view) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
       // intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.analyze_select_file)), REQUEST_CODE_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtil.showToast(getActivity(), "请安装一个文件管理器");
        }

    }

    private ExpeJsonBean mExpeJsonBean=null;
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
                    initChanAndKs();
                    Uri uri = data.getData();
                    String path = FileUtil.getPath(getActivity(), uri);
                    mOpenedFile = new File(path);
                    cb_norm.setVisibility(View.VISIBLE);
                  //  ExpeJsonBean expeJsonBean=null;
                    try {
                        //TODO 解析json文件
                        String fileContent=FileUtils.readFileToString(mOpenedFile, Charset.forName("utf-8"));
                        mExpeJsonBean= JsonParser.parserObject(fileContent, ExpeJsonBean.class);

                        //TODO 转换成Experiment
                        mExperiment=ExpeJsonGenerator
                                .getInstance()
                                .expeJsonBeanToExperiment(mExpeJsonBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToast(getActivity(),R.string.tip_illegal_file);
                    }
                    if (mExpeJsonBean==null){
                        return;
                    }
                    switchMode();
                }
            }
        }
    }
    private String mCurDataPath;
    private void switchMode() {

        if (mExpeJsonBean==null){
            return;
        }


        double[][] ctValues;
        boolean[][] falsePositive;

        chart_line.setDrawMarkers(false);

        String dataFileName="";
        if (!isPcrMode()) {
            if (mExpeJsonBean.getMelting()!=null) {
                layout_ctparam_input.set(mExpeJsonBean.getMelting().ctMin,mExpeJsonBean.getMelting().ctThreshold);
                mChart = new MeltingChart(chart_line);
                dataFileName = mExpeJsonBean.getMelting().dataFileName;
                float startTemp=mExpeJsonBean.getStages().meltingStages.get(0).temp;
                ((MeltingChart) mChart).setStartTemp(startTemp);
                ((MeltingChart) mChart).setAxisMinimum(startTemp);
            }else {
                return;
            }
        } else {
            int totalCyclingCount=0;
            List<ExpeJsonBean.CyclingStage> cyclingSteps=mExpeJsonBean.getStages().cyclingStages;
            for (int i=0;i<cyclingSteps.size();i++){
                ExpeJsonBean.CyclingStage cyclingStage=cyclingSteps.get(i);
                boolean pic=false;
                for (ExpeJsonBean.PartStage partStage:cyclingStage.partStages){
                    if (partStage.takePic){
                        pic=true;
                        break;
                    }
                }
                if (pic){
                    totalCyclingCount+=cyclingStage.cyclingCount;
                }
            }
            layout_ctparam_input.set(mExpeJsonBean.getPcr().ctMin,mExpeJsonBean.getPcr().ctThreshold);

            dataFileName = mExpeJsonBean.getPcr().dataFileName;
            //获取类型，是扩增曲线还是熔解曲线
            mChart = new DtChart(chart_line, totalCyclingCount);
        }



        mCurDataPath=mOpenedFile.getParentFile().getAbsolutePath()+"/"+dataFileName;
        mChart.show(ChanList, KSList, new File(mCurDataPath), layout_ctparam_input.getCtParam(),cb_norm.isChecked());


        buildChannelData();

        if (!isPcrMode()) {
            MeltingChart meltingChart= (MeltingChart) mChart;
            ctValues = meltingChart.getMeltingData().m_CTValue;
            falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
        } else {
            DtChart dtChart= (DtChart) mChart;
            ctValues = dtChart.getDtData().m_CTValue;
            falsePositive = dtChart.getDtData().m_falsePositive;
        }
        KSList.clear();
        KSList = Well.getWell().getKsList();
        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks, ctValues, falsePositive);
            }
        }
        notifyCtChanged();

    }


    @Override
    protected boolean isPcrMode() {

        String item = (String) spinner.getSelectedItem();
        String melting = getString(R.string.setup_mode_melting);
        return !melting.equals(item);
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

        //TODO ct值也要筛选
        mChannelDataAdapters[0].clear();
        mChannelDataAdapters[1].clear();
      /*  GridView[] gvs = new GridView[2];
        gvs[0] = gv_a;
        gvs[1] = gv_b;
        String[] titles = {"A", "B"};*/
        buildChannelData();
        double [][] ctValues=null;
        boolean [][] falsePositive=new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
        String item = (String) spinner.getSelectedItem();
        String melting = getString(R.string.setup_mode_melting);
        if (melting.equals(item)) {
            MeltingChart meltingChart= (MeltingChart) mChart;
            ctValues = meltingChart.getMeltingData().m_CTValue;
            falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
        } else {
            DtChart dtChart= (DtChart) mChart;
            DtChart.DtData dtData=dtChart.getDtData();
            if (dtChart!=null) {
                ctValues =dtData.m_CTValue;
                falsePositive = dtData.m_falsePositive;
            }
        }
        if (ctValues==null){
            return;
        }
        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks, ctValues, falsePositive);
            }
        }
        notifyCtChanged();
    }

    private void showChart() {
        if (mCurDataPath != null && mChart != null)
            mChart.show(ChanList, KSList, new File(mCurDataPath), layout_ctparam_input.getCtParam(),cb_norm.isChecked());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCtParamChanged(final CtParamInputLayout.CtParam ctParam) {

        if (mOpenedFile != null && mChart != null && mChart instanceof DtChart) {
            LoadingDialogHelper.showOpLoading(getActivity());
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    mChart.show(ChanList, KSList, mOpenedFile, ctParam,cb_norm.isChecked());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DtChart dtChart= (DtChart) mChart;
                            double[][] ctValues =dtChart.getDtData().m_CTValue;
                            boolean[][] falsePositive = dtChart.getDtData().m_falsePositive;
                            KSList.clear();
                            KSList = Well.getWell().getKsList();
                            for (String chan : ChanList) {
                                for (String ks : KSList) {
                                    getCtValue(chan, ks, ctValues, falsePositive);
                                }
                            }
                            notifyCtChanged();
                            LoadingDialogHelper.hideOpLoading();
                        }
                    });

                }
            });

        }
    }

    private HistoryExperiment buildExpe() {
       /* KSList.clear();
        KSList = Well.getWell().getKsList();
        for (String chan : ChanList) {
            for (String ks : KSList) {
                List<com.jz.experiment.chart.ChartData> cdlist = CommData.GetChartData(chan, 4, ks);
                if (cdlist.size() == 0){

                }
            }
        }
       */

        HistoryExperiment experiment = new HistoryExperiment();
        ExpeSettingsFirstInfo firstInfo = new ExpeSettingsFirstInfo();
        List<Sample> samplesA = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Sample sample = new Sample();
            sample.setType(Sample.TYPE_A);
            samplesA.add(sample);
        }
        firstInfo.setSamplesA(samplesA);
        List<Sample> samplesB = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Sample sample = new Sample();
            sample.setType(Sample.TYPE_B);
            samplesB.add(sample);
        }
        firstInfo.setSamplesB(samplesB);
        experiment.setSettingsFirstInfo(firstInfo);

        return experiment;
    }

    private void deprecatedPrint(){
        LoadingDialogHelper.showOpLoading(getActivity());

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                StringBuilder sPdfNameBuilder = new StringBuilder();
                sPdfNameBuilder.append(mOpenedFile.getName().replace(".txt", ""));
                ;
                if (mChart instanceof DtChart) {
                    String dt = getString(R.string.setup_mode_dt);
                    sPdfNameBuilder.append(dt);
                } else {
                    String melting = getString(R.string.setup_mode_melting);
                    sPdfNameBuilder.append(melting);
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
                                ToastUtil.showToast(getActivity(), getString(R.string.pdf_exported));

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                LoadingDialogHelper.hideOpLoading();
                                throwable.printStackTrace();
                                ToastUtil.showToast(getActivity(), getString(R.string.pdf_export_error));
                            }
                        });
            }
        }, 3000);
    }
    @OnClick({R.id.iv_pdf, R.id.iv_std_curve})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_std_curve:

                if (mOpenedFile != null) {

                    InputParams params=new InputParams();
                    params.setCtParam(layout_ctparam_input.getCtParam());
                    StandardCurveActivity.start(getActivity(), mExperiment,params);
                }

                break;
            case R.id.iv_pdf:
                if (mOpenedFile == null) {
                    return;
                }
                AndPermission.with(this)
                        .runtime()
                        .permission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE})
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                InputParams params = new InputParams();
                                params.setKsList(KSList);
                                params.setChanList(ChanList);

                                params.setSourceDataPath(mCurDataPath);
                                if (isPcrMode()) {
                                    params.setExpeType(InputParams.EXPE_PCR);

                                }else {
                                    params.setExpeType(InputParams.EXPE_MELTING);
                                }
                                params.setCtParam(layout_ctparam_input.getCtParam());

                                PcrPrintPreviewActivity.start(getActivity(),mExperiment,params);
                              /*  String msg = getString(R.string.dialog_msg_pdf);
                                AppDialogHelper.showNormalDialog(getActivity(),
                                        msg, new AppDialogHelper.DialogOperCallback() {
                                            @Override
                                            public void onDialogConfirmClick() {




                                            }


                                        });*/
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
