package com.jz.experiment.module.data;

import android.Manifest;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.CurveReader;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.data.adapter.ChannelDataAdapter;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.jz.experiment.module.expe.event.SavedExpeDataEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.DataFileUtil;
import com.wind.base.bean.CyclingStage;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.AppUtil;
import com.wind.base.utils.DateUtil;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ChannelData;
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

    @BindView(R.id.tv_expe_name)
    TextView tv_expe_name;
    @BindView(R.id.tv_worker_name)
    TextView tv_worker_name;
    @BindView(R.id.tv_finish_time)
    TextView tv_finish_time;
    @BindView(R.id.tv_elapsed_time)
    TextView tv_elapsed_time;

    @BindView(R.id.gv_a)
    GridView gv_a;

    @BindView(R.id.gv_b)
    GridView gv_b;

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

    DtChart mDtChart;
    MeltingChart mMeltingChart;
    ExecutorService mExecutorService;
    private HistoryExperiment mExeperiment;
    private boolean mSaved;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_expe_data;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        mExeperiment = getArguments().getParcelable(ARGS_KEY_EXPE);
        mChannelDataAdapters = new ChannelDataAdapter[2];
        GridView[] gvs = new GridView[2];
        gvs[0] = gv_a;
        gvs[1] = gv_b;
        String[] titles = {"A", "B"};
        buildChannelData(gvs, titles);

        mExecutorService = Executors.newSingleThreadExecutor();
        init();
        inflateBase();
        inflateChart();
        tv_dt.setActivated(true);
        tv_melt.setActivated(false);

        if (isSavedExpe()){
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
        CommData.positionlist.clear();
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
        KSList.add("A1");
        KSList.add("A2");
        KSList.add("A3");
        KSList.add("A4");

        KSList.add("B1");
        KSList.add("B2");
        KSList.add("B3");
        KSList.add("B4");
    }

    private boolean mHasMeltingMode;


    private boolean mDtShow=true;
    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            if (mDtShow)
                mDtChart.show(ChanList, KSList, DataFileUtil.getDtImageDataFile(mExeperiment));
            else {

            }
        }
    };

    private void inflateChart() {
        if (mExeperiment == null) {
            return;
        }
        CyclingStage cyclingStage = (CyclingStage) mExeperiment.getSettingSecondInfo().getCyclingSteps().get(0);
        mDtChart = new DtChart(chart_dt, cyclingStage.getCyclingCount());
        mDtChart.show(ChanList, KSList, DataFileUtil.getDtImageDataFile(mExeperiment));

        //获取CT value

        for (String chan : ChanList) {
            for (String ks : KSList) {
                getCtValue(chan, ks);
            }
        }
        mChannelDataAdapters[0].notifyDataSetChanged();
        mChannelDataAdapters[1].notifyDataSetChanged();

        mHasMeltingMode = mExeperiment.getSettingSecondInfo().getModes().size() > 1;
        if (mHasMeltingMode) {
            tv_melt.setVisibility(View.VISIBLE);
            mMeltingChart = new MeltingChart(chart_melt);
            mMeltingChart.show(ChanList, KSList, DataFileUtil.getMeltImageDateFile(mExeperiment));
        } else {
            tv_melt.setVisibility(View.GONE);
        }

      /*  ChartData chartData = mExeperiment.getDtChartData();
        List<com.wind.data.expe.bean.LineData> lineDataList = chartData.getLineDataList();
        mDataSets = new ArrayList<>();
        for (int i = 0; i < lineDataList.size(); i++) {//4组数据
            com.wind.data.expe.bean.LineData lineData = lineDataList.get(i);
            List<Entry> entries = lineData.getEntries();

            LineDataSet dataSet = new LineDataSet(entries, "通道" + (i + 1));
            dataSet.setDrawCircles(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);
            dataSet.setColor(lineData.getColor());
            mDataSets.add(dataSet);
        }

        setupChartStyle();

        mLineData = new LineData(mDataSets);
        // chart.setMarker(new ChartMarkerView(getActivity()));
        chart.setTouchEnabled(false);
        chart.setDrawBorders(false);
        chart.setData(mLineData);
        chart.animateX(1500);
        chart.invalidate(); // refresh*/
    }

    private void getCtValue(String chan, String currks) {
        if (!CommData.diclist.keySet().contains(chan) || CommData.diclist.get(chan).size() == 0)
            return;

        int currChan = 0;
        int ksindex = -1;

        int line=1;
        switch (chan) {
            case "Chip#1":
                currChan = 0;

                line=1;
                break;
            case "Chip#2":
                currChan = 1;
                line=2;
                break;
            case "Chip#3":
                currChan = 2;
                line=3;

                break;
            case "Chip#4":
                currChan = 3;
                line=4;
                break;
        }


        int gvIndex = 0;
        int ksIndexInAdapter=0;
        int lineCount=9;//反应孔数+1

        switch (currks) {
            case "A1":
                gvIndex = 0;
                ksindex = 0;

                ksIndexInAdapter = lineCount*line+1;
                break;
            case "A2":
                gvIndex = 0;
                ksindex = 1;

                ksIndexInAdapter = lineCount*line+2;
                break;
            case "A3":
                gvIndex = 0;
                ksindex = 2;

                ksIndexInAdapter =  lineCount*line+3;
                break;
            case "A4":
                gvIndex = 0;
                ksindex = 3;

                ksIndexInAdapter = lineCount*line+4;
                break;
            case "B1":
                gvIndex = 1;
                ksindex = 4;

                ksIndexInAdapter = lineCount*line+1;
                break;
            case "B2":
                gvIndex = 1;
                ksindex = 5;

                ksIndexInAdapter = lineCount*line+2;
                break;
            case "B3":
                gvIndex = 1;
                ksindex = 6;

                ksIndexInAdapter = lineCount*line+3;
                break;
            case "B4":
                gvIndex = 1;
                ksindex = 7;

                ksIndexInAdapter = lineCount*line+4;
                break;
        }
        double[][] ctValues = CurveReader.getInstance().m_CTValue;
        double val = ctValues[currChan][ksindex];
        DecimalFormat format = new DecimalFormat("#0.00");
        String ctValue = format.format(val);
      //  System.out.println("ctValue:" + ctValue);
        mChannelDataAdapters[gvIndex].getItem(ksIndexInAdapter).setSampleVal(ctValue);


    }

    /*private void setupChartStyle() {

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


    }*/
    private ChannelDataAdapter[] mChannelDataAdapters;

    private void buildChannelData(GridView[] gvs, String[] titles) {
        for (int k = 0; k < gvs.length; k++) {
            mChannelDataAdapters[k] = new ChannelDataAdapter(getActivity(), R.layout.item_channel_data);
            gvs[k].setAdapter(mChannelDataAdapters[k]);
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
            mChannelDataAdapters[k].replaceAll(channelDataAList);
        }
    }


    public static ExpeDataFragment newInstance(HistoryExperiment experiment) {
        ExpeDataFragment f = new ExpeDataFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_KEY_EXPE, experiment);
        f.setArguments(args);
        return f;

    }
    public HistoryExperiment getExeperiment(){
        return mExeperiment;
    }
    public boolean isSavedExpe() {
        return mExeperiment.getId() != HistoryExperiment.ID_NONE;
    }


    @OnClick({R.id.iv_pdf, R.id.iv_save})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_save:

                if (!isSavedExpe()&&!mSaved ){
                    mSaved=true;
                    saveExpe()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<InsertExpeResponse>() {
                                @Override
                                public void call(InsertExpeResponse response) {
                                    if (response.getErrCode()== BaseResponse.CODE_SUCCESS){
                                        EventBus.getDefault().post(new SavedExpeDataEvent());
                                        ToastUtil.showToast(getActivity(), "已保存到本地");
                                        Tab tab = new Tab();
                                        tab.setIndex(MainActivity.TAB_INDEX_EXPE);
                                        MainActivity.start(getActivity(), tab);
                                    }else {
                                        ToastUtil.showToast(getActivity(), "保存失败");
                                    }

                                }
                            });
                }



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

    private Observable<Boolean> generatePdf() {
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
                String pdfName = System.currentTimeMillis() + ".pdf";
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
        mVisibleToUser=isVisibleToUser;
    }

    @Subscribe
    public void onFilterEvent(FilterEvent event){
        if (!mVisibleToUser){
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

        mExecutorService.execute(mRun);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
