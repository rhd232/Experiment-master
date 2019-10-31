package com.jz.experiment.module.data;

import android.Manifest;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.anitoa.exception.UnsupportedDeviceException;
import com.anitoa.well.SixteenWell;
import com.anitoa.well.Well;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.chart.CCurveShowPolyFit;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.analyze.CtFragment;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.jz.experiment.module.expe.event.SavedExpeDataEvent;
import com.jz.experiment.module.report.PcrPrintPreviewActivity;
import com.jz.experiment.module.report.bean.InputParams;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.widget.CtParamInputLayout;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.AppUtil;
import com.wind.base.utils.DateUtil;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.request.InsertExpeRequest;
import com.wind.data.expe.response.InsertExpeResponse;
import com.wind.toastlib.ToastUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ExpeDataFragment extends CtFragment implements CtParamInputLayout.OnCtParamChangeListener {

    public static final String ARGS_KEY_EXPE = "args_key_expe";
    @BindView(R.id.tv_dt)
    TextView tv_dt;
    @BindView(R.id.tv_melt)
    TextView tv_melt;

    @BindView(R.id.tv_expe_name)
    TextView tv_expe_name;
    @BindView(R.id.tv_worker_name)
    TextView tv_worker_name;
    @BindView(R.id.tv_finish_time)
    TextView tv_finish_time;
    @BindView(R.id.tv_elapsed_time)
    TextView tv_elapsed_time;

    @BindView(R.id.chart_dt)
    LineChart chart_dt;

    @BindView(R.id.chart_melt)
    LineChart chart_melt;

    /* @BindView(R.id.sv)*/
    ScrollView sv;
    LinearLayout ll_root;
    @BindView(R.id.iv_save)
    View iv_save;

    @BindView(R.id.layout_ctparam_input)
    CtParamInputLayout layout_ctparam_input;

    DtChart mDtChart;
    MeltingChart mMeltingChart;
    ExecutorService mExecutorService;
    @BindView(R.id.cb_norm)
    CheckBox cb_norm;
    private boolean mSaved;
    private Handler mHandler = new Handler();
    private boolean mLoaded;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_expe_data;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreated = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        System.out.println("ExpeDataFragment onViewCreated ->" + mExperiment.getName());
        sv = view.findViewById(R.id.sv);
        ll_root = view.findViewById(R.id.ll_root);
        layout_ctparam_input.setOnCtParamChangeListener(this);

        tv_dt.setActivated(true);
        tv_melt.setActivated(false);
        chart_melt.setVisibility(View.GONE);

        mExecutorService = Executors.newSingleThreadExecutor();

        inflateBase();


        // if (mVisibleToUser && !mLoaded){
        initKsAndChanList();
        inflateChart();
        mLoaded = true;
        // }

        iv_save.setVisibility(View.GONE);


        cb_norm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (tv_dt.isActivated()) {
                    YAxis yAxis = chart_dt.getAxisLeft();
                    yAxis.setAxisMaximum(5000);
                }
                System.out.println("onCheckedChanged");
                showChart();
            }
        });

    }


    @Override
    protected boolean isPcrMode() {
        return tv_dt.isActivated();
    }


    private void inflateBase() {
        tv_expe_name.setText(mExperiment.getName());
        tv_worker_name.setText("admin");
        String finishTime = DateUtil.getDateTime(mExperiment.getFinishMilliTime());
        tv_finish_time.setText(finishTime);
        long during = mExperiment.getDuring();
        String hh = new DecimalFormat("00").format(during / 3600);
        String mm = new DecimalFormat("00").format(during % 3600 / 60);
        String ss = new DecimalFormat("00").format(during % 60);
        String duringTime = new String(hh + ":" + mm + ":" + ss);
        tv_elapsed_time.setText(duringTime);
    }


    private List<String> ChanList = new ArrayList<>();
    private List<String> KSList = new ArrayList<String>();

    public void initKsAndChanList() {
        // CommData.diclist.clear();
        List<Channel> channels = mExperiment.getSettingsFirstInfo().getChannels();
        CommData.cboChan1 = 0;
        CommData.cboChan2 = 0;
        CommData.cboChan3 = 0;
        CommData.cboChan4 = 0;

        ChanList.clear();
        if (!TextUtils.isEmpty(channels.get(0).getValue())) {
            CommData.cboChan1 = 1;
            ChanList.add("Chip#1");
        }
        if (!TextUtils.isEmpty(channels.get(1).getValue())) {
            CommData.cboChan2 = 1;
            ChanList.add("Chip#2");
        }
        if (!TextUtils.isEmpty(channels.get(2).getValue())) {
            CommData.cboChan3 = 1;
            ChanList.add("Chip#3");
        }
        if (!TextUtils.isEmpty(channels.get(3).getValue())) {
            CommData.cboChan4 = 1;
            ChanList.add("Chip#4");
        }

        KSList.clear();

        try {
            KSList = Well.getWell().getKsList();
        } catch (UnsupportedDeviceException e) {
            //第一次安装，没有文件读取权限导致
            e.printStackTrace();
            KSList = new SixteenWell().getKsList();

        }

    }


    private boolean mHasMeltingMode;


    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            showChart();
        }
    };


    private void showChart() {
        double[][] ctValues;
        boolean[][] falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
        if (tv_dt.isActivated()) {
            mExperiment.getSettingSecondInfo().getModes().get(0).setCtMin(layout_ctparam_input.getCtParam().ctMin);
            mExperiment.getSettingSecondInfo().getModes().get(0).setCtThreshold(layout_ctparam_input.getCtParam().ctThreshhold);
            mDtChart.show(ChanList, KSList,
                    DataFileUtil.getDtImageDataFile(mExperiment), layout_ctparam_input.getCtParam(), cb_norm.isChecked());
            ctValues = mDtChart.getDtData().m_CTValue;
            falsePositive = mDtChart.getDtData().m_falsePositive;
            //falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
        } else {

            mExperiment.getSettingSecondInfo().getModes().get(1).setCtMin(layout_ctparam_input.getCtParam().ctMin);
            mExperiment.getSettingSecondInfo().getModes().get(1).setCtThreshold(layout_ctparam_input.getCtParam().ctThreshhold);


            mMeltingChart.show(ChanList, KSList, DataFileUtil.getMeltImageDateFile(mExperiment), layout_ctparam_input.getCtParam(), cb_norm.isChecked());
            ctValues = mMeltingChart.getMeltingData().m_CTValue;
            //falsePositive = CCurveShowMet.getInstance().m_falsePositive;
        }
        Activity activity = getActivity();
        if (activity == null || isDetached()) {
            return;
        }
        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks, ctValues, falsePositive);
            }
        }
        notifyCtChanged();



       /* LoadingDialogHelper.showOpLoading(getActivity());

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                double[][] ctValues;
                boolean[][] falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
                if (tv_dt.isActivated()) {
                    mExperiment.getSettingSecondInfo().getModes().get(0).setCtMin(layout_ctparam_input.getCtParam().ctMin);
                    mExperiment.getSettingSecondInfo().getModes().get(0).setCtThreshold(layout_ctparam_input.getCtParam().ctThreshhold);
                    mDtChart.show(ChanList, KSList,
                            DataFileUtil.getDtImageDataFile(mExperiment), layout_ctparam_input.getCtParam(), cb_norm.isChecked());
                    ctValues = mDtChart.getDtData().m_CTValue;
                    falsePositive = mDtChart.getDtData().m_falsePositive;
                    //falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
                } else {

                    mExperiment.getSettingSecondInfo().getModes().get(1).setCtMin(layout_ctparam_input.getCtParam().ctMin);
                    mExperiment.getSettingSecondInfo().getModes().get(1).setCtThreshold(layout_ctparam_input.getCtParam().ctThreshhold);


                    mMeltingChart.show(ChanList, KSList, DataFileUtil.getMeltImageDateFile(mExperiment), layout_ctparam_input.getCtParam(), cb_norm.isChecked());
                    ctValues = mMeltingChart.getMeltingData().m_CTValue;
                    //falsePositive = CCurveShowMet.getInstance().m_falsePositive;
                }

                showCt(ctValues, falsePositive);
            }
        });
*/

}

    private void showCt(final double[][] ctValues, final boolean[][] falsePositive) {
        Activity activity = getActivity();
        if (activity == null || isDetached()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

    private boolean mReadingFile;

    private void inflateChart() {
        if (mExperiment == null) {
            return;
        }
       /* if (mReadingFile){
            return;
        }*/
        mReadingFile = true;

        //统计总共有多少循环（不拍照的不包括）
        int totalCyclingCount = 0;
        List<Stage> cyclingSteps = mExperiment.getSettingSecondInfo().getCyclingSteps();
        for (int i = 0; i < cyclingSteps.size(); i++) {
            CyclingStage cyclingStage = (CyclingStage) cyclingSteps.get(i);
            boolean pic = false;
            for (PartStage partStage : cyclingStage.getPartStageList()) {
                if (partStage.isTakePic()) {
                    pic = true;
                    break;
                }
            }
            if (pic) {
                totalCyclingCount += cyclingStage.getCyclingCount();
            }
        }
        mDtChart = new DtChart(chart_dt, totalCyclingCount);


        Activity activity = getActivity();
        if (activity == null) {
            mLoaded = false;
            mReadingFile = false;

            return;
        }
        //文件读取之后孔数已经有值
        mDtChart.show(ChanList, KSList, DataFileUtil.getDtImageDataFile(mExperiment), layout_ctparam_input.getCtParam(), cb_norm.isChecked());

        //孔数已经放在数据文件中，不在存放在/anitoa/trim目录下
        KSList.clear();
        KSList = Well.getWell().getKsList();

        //获取CT value
        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks, mDtChart.getDtData().m_CTValue, mDtChart.getDtData().m_falsePositive);
            }
        }
        activity = getActivity();
        if (activity == null) {
            mLoaded = false;
            mReadingFile = false;
            return;
        }
        mChannelDataAdapters[0].notifyDataSetChanged();
        mChannelDataAdapters[1].notifyDataSetChanged();

        mHasMeltingMode = mExperiment.getSettingSecondInfo().getModes().size() > 1;
        if (mHasMeltingMode) {
            mMeltingChart = new MeltingChart(chart_melt);
            float start;
            try {
                List<Stage> stages = mExperiment.getSettingSecondInfo().getSteps();
                start = stages.get(stages.size() - 2).getTemp();
            } catch (Exception e) {
                e.printStackTrace();
                start = 40;
            }
            mMeltingChart.setStartTemp(start);
            mMeltingChart.setAxisMinimum(start);
            tv_melt.setVisibility(View.VISIBLE);
        } else {
            tv_melt.setVisibility(View.GONE);
        }
        mReadingFile = false;


    }


    public static ExpeDataFragment newInstance(HistoryExperiment experiment) {
        ExpeDataFragment f = new ExpeDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_KEY_EXPE, experiment);
        f.setArguments(args);
        return f;

    }

    public HistoryExperiment getExeperiment() {
        return mExperiment;
    }

    public boolean isSavedExpe() {
        return mExperiment != null && mExperiment.getId() != HistoryExperiment.ID_NONE;
    }

    private long time;

    private void deprecatedPrint() {
        String msg = getString(R.string.dialog_msg_pdf);
        AppDialogHelper.showNormalDialog(getActivity(),
                msg, new AppDialogHelper.DialogOperCallback() {
                    @Override
                    public void onDialogConfirmClick() {

                        LoadingDialogHelper.showOpLoading(getActivity());
                        if (!tv_dt.isActivated()) {
                            onViewClick(tv_dt);
                        }
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String pdfName = DataFileUtil.getPdfFileName(getActivity(), mExperiment, false);
                                //生成pdf
                                generatePdf(pdfName)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Boolean>() {
                                            @Override
                                            public void call(Boolean aboolean) {
                                                if (mHasMeltingMode) {
                                                    onViewClick(tv_melt);
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            String pdfName = DataFileUtil.getPdfFileName(getActivity(), mExperiment, true);

                                                            generatePdf(pdfName)
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(new Action1<Boolean>() {
                                                                        @Override
                                                                        public void call(Boolean aBoolean) {
                                                                            LoadingDialogHelper.hideOpLoading();

                                                                            ToastUtil.showToast(getActivity(), getString(R.string.pdf_exported));
                                                                        }
                                                                    }, new Action1<Throwable>() {
                                                                        @Override
                                                                        public void call(Throwable throwable) {
                                                                            throwable.printStackTrace();
                                                                            LoadingDialogHelper.hideOpLoading();
                                                                            ToastUtil.showToast(getActivity(), getString(R.string.pdf_export_error));
                                                                        }
                                                                    });
                                                        }
                                                    }, 3000);

                                                } else {
                                                    LoadingDialogHelper.hideOpLoading();
                                                    ToastUtil.showToast(getActivity(), getString(R.string.pdf_exported));
                                                }

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


                });
    }

    @Override
    public void onPause() {
        super.onPause();
        LoadingDialogHelper.hideOpLoading();
    }

    @Deprecated
    @OnClick({R.id.iv_pdf, R.id.iv_save, R.id.tv_dt, R.id.tv_melt, R.id.iv_std_curve})
    public void onViewClick(View view) {
        long now = System.currentTimeMillis();
        switch (view.getId()) {
            case R.id.iv_std_curve:
                InputParams params = new InputParams();
                params.setCtParam(layout_ctparam_input.getCtParam());
                StandardCurveActivity.start(getActivity(), mExperiment, params);
                break;
            case R.id.tv_dt:

                time = now;
                tv_dt.setActivated(true);
                tv_melt.setActivated(false);

                chart_dt.setVisibility(View.VISIBLE);

                chart_melt.setVisibility(View.GONE);

                //mExecutorService.execute(mRun);
                showChart();

                break;
            case R.id.tv_melt:
                // if (now-time>1500) {
                time = now;
                tv_dt.setActivated(false);
                tv_melt.setActivated(true);

                chart_dt.setVisibility(View.GONE);
                chart_melt.setVisibility(View.VISIBLE);
                showChart();
                //mExecutorService.execute(mRun);
                // }
                break;
            case R.id.iv_save:

                doSaveExpe();

                break;
            case R.id.iv_pdf:
             /*   GenerateExpeJsonRequest request = new GenerateExpeJsonRequest();
                request.setExperiment(mExperiment);
                ExpeJsonGenerator
                        .getInstance()
                        .generateExpeJson(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<BaseResponse>() {
                            @Override
                            public void call(BaseResponse response) {
                                if (response.getErrCode()== BaseResponse.CODE_SUCCESS){
                                    ToastUtil.showToast(getActivity(),"导出json成功");
                                }else {
                                    ToastUtil.showToast(getActivity(),"导出json失败");
                                }
                            }
                        });*/

                AndPermission.with(this)
                        .runtime()
                        .permission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE})
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> data) {
                                InputParams params = new InputParams();
                                params.setChanList(ChanList);
                                params.setKsList(KSList);

                                if (tv_dt.isActivated()) {
                                    params.setExpeType(InputParams.EXPE_PCR);
                                    params.setSourceDataPath(DataFileUtil.getDtImageDataFile(mExperiment).getAbsolutePath());
                                } else {
                                    params.setExpeType(InputParams.EXPE_MELTING);
                                    params.setSourceDataPath(DataFileUtil.getMeltImageDateFile(mExperiment).getAbsolutePath());
                                }
                                params.setCtParam(layout_ctparam_input.getCtParam());

                                PcrPrintPreviewActivity.start(getActivity(), mExperiment, params);

                            }
                        }).start();


                break;
        }
    }

    private void doSaveExpe() {
        if (!isSavedExpe() && !mSaved) {
            mSaved = true;
            saveExpe()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<InsertExpeResponse>() {
                        @Override
                        public void call(InsertExpeResponse response) {
                            if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                                EventBus.getDefault().post(new SavedExpeDataEvent());
                                //ToastUtil.showToast(getActivity(), "已保存到本地");
                                Tab tab = new Tab();
                                tab.setIndex(MainActivity.TAB_INDEX_EXPE);
                                MainActivity.start(getActivity(), tab);
                            } else {
                                ToastUtil.showToast(getActivity(), getString(R.string.setup_save_error));
                            }

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            ToastUtil.showToast(getActivity(), getString(R.string.setup_save_error));
                        }
                    });
        }
    }

    private Observable<InsertExpeResponse> saveExpe() {
        ExperimentStatus status = new ExperimentStatus();
        status.setStatus(ExperimentStatus.STATUS_COMPLETED);

        status.setDesc(getString(R.string.test_status_finished));
        mExperiment.setStatus(status);
        //新插入一条数据
        mExperiment.setId(HistoryExperiment.ID_NONE);

        InsertExpeRequest request = new InsertExpeRequest();
        request.setExperiment(mExperiment);
        //TODO 保存实验数据
        return ExpeDataStore
                .getInstance(ProviderModule.getInstance().getBriteDb(getActivity().getApplicationContext()))
                .insertExpe(request);

    }

    private Observable<Boolean> generatePdf(final String pdfName) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                PdfDocument document = new PdfDocument();
                int width = AppUtil.getScreenWidth(getActivity());
                int height = 0;// AppUtil.getScreenHeight(getActivity());
                if (sv != null) {
                    //计算scrollview的高度
                    for (int i = 0; i < sv.getChildCount(); i++) {
                        height += sv.getChildAt(i).getHeight();
                    }
                } else {
                    height = ll_root.getHeight();
                }

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                        .Builder(width, height, 1)
                        .create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                if (sv != null) {
                    sv.draw(canvas);
                } else {
                    ll_root.draw(canvas);
                }
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

    boolean mVisibleToUser;
    boolean mViewCreated;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mVisibleToUser = isVisibleToUser;
        System.out.println("getUserVisibleHint->" + getUserVisibleHint() + " mVisibleToUser->" + mVisibleToUser);
       /*if (mViewCreated && mVisibleToUser && !mLoaded && mExperiment!=null){
            System.out.println("inflateChart");
            mLoaded=true;
            initKsAndChanList();
            inflateChart();
        }*/
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


        //mExecutorService.execute(mRun);
        showChart();

        inflateCt();
    }


    private void inflateCt() {
        mChannelDataAdapters[0].clear();
        mChannelDataAdapters[1].clear();
      /*  GridView[] gvs = new GridView[2];
        gvs[0] = gv_a;
        gvs[1] = gv_b;
        String[] titles = {"A", "B"};*/
        buildChannelData();
        double[][] ctValues;
        boolean[][] falsePositive;


        if (!tv_dt.isActivated()) {
            ctValues = mMeltingChart.getMeltingData().m_CTValue;
            falsePositive = new boolean[CCurveShowPolyFit.MAX_CHAN][CCurveShowPolyFit.MAX_WELL];
        } else {
            ctValues = mDtChart.getDtData().m_CTValue;
            falsePositive = mDtChart.getDtData().m_falsePositive;
         /*   ctValues = CCurveShowPolyFit.getInstance().m_CTValue;
            falsePositive = CCurveShowPolyFit.getInstance().m_falsePositive;*/
        }
        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks, ctValues, falsePositive);
            }
        }
        notifyCtChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLoaded = false;
        mReadingFile = false;
        mViewCreated = false;
        System.out.println("ExpeDataFragment onDestroyView:" + mExperiment.getName());
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCtParamChanged(CtParamInputLayout.CtParam ctParam) {

        showChart();
    }
}
