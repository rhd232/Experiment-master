package com.jz.experiment.module.data;

import android.Manifest;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.chart.CCurveShow;
import com.jz.experiment.chart.CCurveShowMet;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.device.UnsupportedDeviceException;
import com.jz.experiment.device.Well;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.analyze.CtFragment;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.jz.experiment.module.expe.event.SavedExpeDataEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.widget.CtParamInputLayout;
import com.wind.base.bean.CyclingStage;
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

   /* @BindView(R.id.gv_a)
    GridView gv_a;

    @BindView(R.id.gv_b)
    GridView gv_b;*/

    @BindView(R.id.chart_dt)
    LineChart chart_dt;

    @BindView(R.id.chart_melt)
    LineChart chart_melt;

    @BindView(R.id.sv)
    ScrollView sv;
    @BindView(R.id.iv_save)
    View iv_save;
   /* LineData mLineData;
    ArrayList<ILineDataSet> mDataSets;*/

   @BindView(R.id.layout_ctparam_input)
   CtParamInputLayout layout_ctparam_input;

    DtChart mDtChart;
    MeltingChart mMeltingChart;
    ExecutorService mExecutorService;

    private boolean mSaved;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_expe_data;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventBus.getDefault().register(this);
       // mExeperiment = getArguments().getParcelable(ARGS_KEY_EXPE);

        layout_ctparam_input.setOnCtParamChangeListener(this);
      /*  GridView[] gvs = new GridView[2];
        gvs[0] = gv_a;
        gvs[1] = gv_b;
        String[] titles = {"A", "B"};
        buildChannelData(gvs, titles);*/
        tv_dt.setActivated(true);
        tv_melt.setActivated(false);
        chart_melt.setVisibility(View.GONE);

        mExecutorService = Executors.newSingleThreadExecutor();
        init();
        inflateBase();
        inflateChart();

        if (isSavedExpe()) {
            iv_save.setVisibility(View.GONE);
        }
        // mExecutorService.execute(mRun);

    }

    private void inflateBase() {
        tv_expe_name.setText(mExeperiment.getName());
        tv_worker_name.setText("admin");
        String finishTime = DateUtil.getDateTime(mExeperiment.getFinishMilliTime());
        tv_finish_time.setText(finishTime);
        long during = mExeperiment.getDuring();
        String hh = new DecimalFormat("00").format(during / 3600);
        String mm = new DecimalFormat("00").format(during % 3600 / 60);
        String ss = new DecimalFormat("00").format(during % 60);
        String duringTime = new String(hh + ":" + mm + ":" + ss);
        tv_elapsed_time.setText(duringTime);
    }

    private List<String> ChanList = new ArrayList<>();
    private List<String> KSList = new ArrayList<String>();

    public void init() {
        CommData.diclist.clear();
        List<Channel> channels = mExeperiment.getSettingsFirstInfo().getChannels();
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
            KSList=Well.getWell().getKsList();
        }catch (UnsupportedDeviceException e){
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



    private boolean mHasMeltingMode;


    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            showChart();
        }
    };



    private void showChart() {
        double [][] ctValues;
        if (tv_dt.isActivated()) {

            mDtChart.show(ChanList, KSList, DataFileUtil.getDtImageDataFile(mExeperiment),layout_ctparam_input.getCtParam());
            ctValues=CCurveShow.getInstance().m_CTValue;
        } else {
          /*  float t=Float.parseFloat(mExeperiment.getSettingSecondInfo().getStartTemperature());
            float f=Float.parseFloat(String.format("%f",t));
            mMeltingChart.setAxisMinimum(f);*/
            mMeltingChart.show(ChanList, KSList, DataFileUtil.getMeltImageDateFile(mExeperiment),layout_ctparam_input.getCtParam());
            ctValues=CCurveShowMet.getInstance().m_CTValue;
        }

        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks,ctValues);
            }
        }
        notifyCtChanged();
    }


    private void inflateChart() {
        if (mExeperiment == null) {
            return;
        }
        CyclingStage cyclingStage = (CyclingStage) mExeperiment.getSettingSecondInfo().getCyclingSteps().get(0);
        mDtChart = new DtChart(chart_dt, cyclingStage.getCyclingCount());
        mDtChart.show(ChanList, KSList, DataFileUtil.getDtImageDataFile(mExeperiment),layout_ctparam_input.getCtParam());

        //孔数已经放在数据文件中，不在存放在/anitoa/trim目录下
        KSList.clear();
        KSList=Well.getWell().getKsList();

        //获取CT value
        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks,CCurveShow.getInstance().m_CTValue);
            }
        }

        mChannelDataAdapters[0].notifyDataSetChanged();
        mChannelDataAdapters[1].notifyDataSetChanged();

        mHasMeltingMode = mExeperiment.getSettingSecondInfo().getModes().size() > 1;
        if (mHasMeltingMode) {
            //tv_melt.setVisibility(View.VISIBLE);
            mMeltingChart = new MeltingChart(chart_melt);
            mMeltingChart.show(ChanList, KSList, DataFileUtil.getMeltImageDateFile(mExeperiment),layout_ctparam_input.getCtParam());
        } else {
            tv_melt.setVisibility(View.GONE);
        }

    }







    public static ExpeDataFragment newInstance(HistoryExperiment experiment) {
        ExpeDataFragment f = new ExpeDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_KEY_EXPE, experiment);
        f.setArguments(args);
        return f;

    }

    public HistoryExperiment getExeperiment() {
        return mExeperiment;
    }

    public boolean isSavedExpe() {
        return mExeperiment!=null&&mExeperiment.getId() != HistoryExperiment.ID_NONE;
    }

    private long time;
    private Handler mHandler=new Handler();
    @OnClick({R.id.iv_pdf, R.id.iv_save, R.id.tv_dt, R.id.tv_melt})
    public void onViewClick(View view) {
        long now = System.currentTimeMillis();
        switch (view.getId()) {
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
                                                if (!tv_dt.isActivated()) {
                                                    onViewClick(tv_dt);
                                                }
                                                mHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        String pdfName = DataFileUtil.getPdfFileName(mExeperiment, false);
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
                                                                                    String pdfName = DataFileUtil.getPdfFileName(mExeperiment, true);

                                                                                    generatePdf(pdfName)
                                                                                            .subscribeOn(Schedulers.io())
                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                            .subscribe(new Action1<Boolean>() {
                                                                                                @Override
                                                                                                public void call(Boolean aBoolean) {
                                                                                                    LoadingDialogHelper.hideOpLoading();
                                                                                                    ToastUtil.showToast(getActivity(), "已导出");
                                                                                                }
                                                                                            }, new Action1<Throwable>() {
                                                                                                @Override
                                                                                                public void call(Throwable throwable) {
                                                                                                    throwable.printStackTrace();
                                                                                                    LoadingDialogHelper.hideOpLoading();
                                                                                                    ToastUtil.showToast(getActivity(), "请重试");
                                                                                                }
                                                                                            });
                                                                                }
                                                                            },3000);

                                                                        } else {
                                                                            LoadingDialogHelper.hideOpLoading();
                                                                            ToastUtil.showToast(getActivity(), "已导出");
                                                                        }

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
                                                },3000);


                                            }


                                        });
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
                                ToastUtil.showToast(getActivity(), "已保存到本地");
                                Tab tab = new Tab();
                                tab.setIndex(MainActivity.TAB_INDEX_EXPE);
                                MainActivity.start(getActivity(), tab);
                            } else {
                                ToastUtil.showToast(getActivity(), "保存失败");
                            }

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            ToastUtil.showToast(getActivity(), "保存失败,请重试");
                        }
                    });
        }
    }
    private Observable<InsertExpeResponse> saveExpe() {
        ExperimentStatus status = new ExperimentStatus();
        status.setStatus(ExperimentStatus.STATUS_COMPLETED);
        status.setDesc("已完成");
        mExeperiment.setStatus(status);
        //新插入一条数据
        mExeperiment.setId(HistoryExperiment.ID_NONE);

        InsertExpeRequest request = new InsertExpeRequest();
        request.setExperiment(mExeperiment);
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
                //计算scrollview的高度
                for (int i = 0; i < sv.getChildCount(); i++) {
                    height += sv.getChildAt(i).getHeight();
                }

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                        .Builder(width, height, 1)
                        .create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                sv.draw(canvas);
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



        //mExecutorService.execute(mRun);
        showChart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCtParamChanged(CtParamInputLayout.CtParam ctParam) {
        showChart();
    }
}
