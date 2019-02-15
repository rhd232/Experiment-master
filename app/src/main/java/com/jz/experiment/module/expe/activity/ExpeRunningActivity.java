package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.FactUpdater;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.module.bluetooth.BluetoothReceiver;
import com.jz.experiment.module.bluetooth.BluetoothService;
import com.jz.experiment.module.bluetooth.CommunicationService;
import com.jz.experiment.module.bluetooth.Data;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.module.bluetooth.UsbService;
import com.jz.experiment.module.bluetooth.ble.BluetoothConnectionListener;
import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;
import com.jz.experiment.module.data.FilterActivity;
import com.jz.experiment.module.expe.bean.ChannelImageStatus;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.expe.event.ExpeNormalFinishEvent;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.ByteHelper;
import com.jz.experiment.util.ByteUtil;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.DeviceProxyHelper;
import com.jz.experiment.util.StatusChecker;
import com.jz.experiment.util.TrimReader;
import com.jz.experiment.widget.DuringView;
import com.wind.base.BaseActivity;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Navigator;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Mode;
import com.wind.toastlib.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ExpeRunningActivity extends BaseActivity implements BluetoothConnectionListener {


    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, ExpeRunningActivity.class, experiment);
    }

    @BindView(R.id.chart_dt)
    LineChart chart_dt;
    @BindView(R.id.chart_melt)
    LineChart chart_melt;


    @BindView(R.id.tv_cur_mode)
    TextView tv_cur_mode;
    @BindView(R.id.tv_duration)
    DuringView tv_duration;

    @BindView(R.id.ll_cycling)
    View ll_cycling;
    @BindView(R.id.tv_cycling)
    TextView tv_cycling;

    @BindView(R.id.tv_cycling_desc)
    TextView tv_cycling_desc;


    private HistoryExperiment mHistoryExperiment;
    BluetoothReceiver mBluetoothReceiver;
    /*BluetoothService mBluetoothService;
    UsbService mUsbService;*/
    CommunicationService mCommunicationService;
    /**
     * 当前图像格式
     */
    private PcrCommand.IMAGE_MODE mImageMode;
    /**
     * 是否含有溶解曲线
     */
    private boolean mHasMeltingCurve;
    private ExecutorService mExecutorService;

    private DtChart mDtChart;
    private MeltingChart mMeltingChart;
    private FactUpdater mFactUpdater;
    @Override
    protected void setTitle() {
        mTitleBar.setTitle("运行");
        mTitleBar.setRightText("筛选");
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterActivity.start(getActivity());
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expe_running);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);

        mImageMode = PcrCommand.IMAGE_MODE.IMAGE_12;
        mHistoryExperiment = Navigator.getParcelableExtra(this);
        List<Mode> modeList = mHistoryExperiment.getSettingSecondInfo().getModes();
        if (modeList.size() > 1) {
            mHasMeltingCurve = true;
        }

        tv_cur_mode.setText("当前模式：变温扩增");
        tv_cycling_desc.setText("");
        tv_cycling.setText("");
        //启动计时器
        tv_duration.start();


        initChart();


        bindService();
        init();


        mExecutorService = Executors.newSingleThreadExecutor();

        //mDtChart.show(ChanList,KSList,null);
       /* Thread thread = new Thread(mRun);
        thread.start();*/


    }

    private void bindService() {
         mCommunicationService=DeviceProxyHelper.getInstance(getActivity()).getCommunicationService();
//        mBluetoothService = DeviceProxyHelper.getInstance(getActivity()).getBluetoothService();
//        mUsbService = DeviceProxyHelper.getInstance(getActivity()).getUsbService();

        chart_dt.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ActivityUtil.isFinish(getActivity())) {
                    step1();
                }
            }
        }, 500);


        mBluetoothReceiver = new BluetoothReceiver();
        mBluetoothReceiver.setBluetoothConnectInteface(this);
        getActivity().registerReceiver(mBluetoothReceiver,
                makeIntentFilter());
    }

    public static IntentFilter makeIntentFilter() { // 注册接收的事件
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void step1() {
       /* PcrCommand resetCommond = new PcrCommand();
        resetCommond.resetDevice();
        mUsbService.sendPcrCommandSync(resetCommond);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
       if (mCommunicationService instanceof UsbService){
           UsbService usbService= (UsbService) mCommunicationService;
           usbService.startReadThread();
       }

        // 给设备发指令 step1
        PcrCommand cmd = new PcrCommand();
        List<Channel> channels = mHistoryExperiment.getSettingsFirstInfo().getChannels();
        int[] channelOp = new int[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            String value = channels.get(i).getValue();
            if (TextUtils.isEmpty(value)) {
                channelOp[i] = 0;
            } else {
                channelOp[i] = 1;
            }
        }

        cmd.step1(channelOp);
        mCommunicationService.sendPcrCommand(cmd);
        //mUsbService.sendPcrCommand(cmd);
    }

    private void initChart() {
        mFactUpdater=FactUpdater.getInstance(mCommunicationService);
        mFactUpdater.SetInitData();
        mDtChart = new DtChart(chart_dt, getCurCyclingStage().getCyclingCount(),mFactUpdater);
        mDtChart.setRunning(true);
        mMeltingChart = new MeltingChart(chart_melt,mFactUpdater);
        mMeltingChart.setRunning(true);
    }

    private List<String> ChanList = new ArrayList<>();
    private List<String> KSList = new ArrayList<String>();

    public void init() {
        CommData.diclist.clear();
        //CommData.positionlist.clear();
        List<Channel> channels = mHistoryExperiment.getSettingsFirstInfo().getChannels();
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


    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            showChart();
        }
    };

    private void showChart(){
        if (mInCycling)
            showDtChart();
        if (mInMeltCurve) {
            showMeltChart();
        }
    }
    private void showMeltChart() {
        if (chart_melt.getVisibility()==View.GONE){
            chart_melt.setVisibility(View.VISIBLE);
        }
        if (chart_dt.getVisibility()==View.VISIBLE){
            chart_dt.setVisibility(View.GONE);
        }
        mMeltingChart.show(ChanList, KSList, DataFileUtil.getMeltImageDateFile(mHistoryExperiment));
    }

    private void showDtChart() {
        if (chart_melt.getVisibility()==View.VISIBLE){
            chart_melt.setVisibility(View.GONE);
        }
        if (chart_dt.getVisibility()==View.GONE){
            chart_dt.setVisibility(View.VISIBLE);
        }

        mDtChart.show(ChanList, KSList, DataFileUtil.getDtImageDataFile(mHistoryExperiment));

    }

    private boolean mBackPressed;

    @Override
    public void onBackPressed() {
        if (mBackPressed) {
            return;
        }
        mBackPressed = true;
        if (!mExpeFinished) {

            String msg = "确定要强行终止吗？";
            AppDialogHelper.showNormalDialog(getActivity(), msg, new AppDialogHelper.DialogOperCallback() {
                @Override
                public void onDialogCancelClick() {
                    super.onDialogCancelClick();
                    mBackPressed = false;
                }

                @Override
                public void onDialogConfirmClick() {
                    //  LoadingDialogHelper.showOpLoading(getActivity());

                    closeDevice()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            //  LoadingDialogHelper.hideOpLoading();
                            ActivityUtil.finish(getActivity());
                        }
                    });


                }
            });
        } else {
            ActivityUtil.finish(getActivity());
        }
    }

    private Observable<Boolean> closeDevice() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                while (mInReadingImg) {

                }
                if (mCommunicationService instanceof UsbService){
                    UsbService usbService= (UsbService) mCommunicationService;
                    usbService.stopReadThread();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!mExpeFinished) {
                    if (mHasMeltingCurve) {
                        if (mInMeltCurve) {
                            stopMeltingCurve();
                        }
                    }
                    // if (mInCycling) {
                    stopCycling();
                    //}
                }

                subscriber.onNext(true);
            }
        });
    }

    @OnClick({R.id.tv_stop})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_stop:
                //强行终止：停止循环和停止溶解曲线
                onBackPressed();


               // toAnalyzePage();

               /* mHistoryExperiment.setDuring(tv_duration.getDuring());
                mHistoryExperiment.setFinishMilliTime(System.currentTimeMillis());
                //数据
                //变增扩温曲线数据
                //TODO 溶解曲线曲线数据
                ChartData chartData = new ChartData();
                List<com.wind.data.expe.bean.LineData> lineDataList = new ArrayList<>();
                for (int i = 0; i < mDataSets.size(); i++) {
                    LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                    List<Entry> entries = lineDataSet.getValues();
                    com.wind.data.expe.bean.LineData lineData = new com.wind.data.expe.bean.LineData();
                    lineData.setEntries(entries);
                    lineData.setColor(mLineColors.get(i));
                    lineDataList.add(lineData);

                }
                chartData.setLineDataList(lineDataList);


                mHistoryExperiment.setDtChartData(chartData);
                Tab tab = new Tab();
                tab.setIndex(MainActivity.TAB_INDEX_DATA);
                tab.setExtra(mHistoryExperiment);
                MainActivity.start(getActivity(), tab);*/
                break;
        }
    }


    /**
     * 蓝牙设备断开连接
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothDisConnectedEvent(BluetoothDisConnectedEvent event) {

    }

    /**
     * 筛选图像
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFilterEvent(FilterEvent event) {
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
        //mExecutorService.execute(mRun);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (step5Subscription != null && !step5Subscription.isUnsubscribed()) {
            step5Subscription.unsubscribe();
            step5Subscription = null;
        }
        getActivity().unregisterReceiver(mBluetoothReceiver);
        EventBus.getDefault().unregister(this);
    }


    /****设备连接******/
    @Override
    public void onConnectSuccess() {

    }

    @Override
    public void onConnectCancel() {

    }

    @Override
    public void onDoThing() {

    }

    @Override
    public void onReceivedData(Data data) {
        //接收到数据 TODO 根据command和type执行操作
        byte[] reveicedBytes = data.getBuffer();
        int statusIndex = 1;
        int status = hexToDecimal(reveicedBytes[statusIndex]);
        boolean succ = StatusChecker.checkStatus(status);
        if (!succ) {
            AppDialogHelper.showSingleBtnDialog(getActivity(), StatusChecker.getStatusDesc(status), new AppDialogHelper.DialogOperCallback() {
                @Override
                public void onDialogConfirmClick() {

                }
            });
            return;
        }

        int cmdIndex = 2;
        int cmd = hexToDecimal(reveicedBytes[cmdIndex]);

        switch (cmd) {
            case PcrCommand.STEP_1_CMD:
                doReceiveStep1(data);
                break;
            case PcrCommand.STEP_2_CMD:
                doReceiveStep2(data);
                break;
            case PcrCommand.STEP_3_OR_4_CMD:
                doReceiveStep3or4(data);
                break;
            case PcrCommand.STEP_5_CMD:
                doReceiveStep5(data);
                break;
            case PcrCommand.STEP_6_CMD:
                doReceiveStep6(data);
                break;
            case PcrCommand.STEP_7_CMD:
                doReceiveStep7(data);
                break;
        }
    }


    /**
     * 十六进制转十进制
     *
     * @param value
     * @return
     */
    private int hexToDecimal(int value) {
        return value;//Integer.parseInt(value + "", 16);

    }

    private void doReceiveStep1(Data data) {
        int headIndex = 0;
        int ackIndex = 1;
        int cmdIndex = 2;
        int lengthIndex = 3;
        int typeIndex = 4;

        byte[] reveicedBytes = data.getBuffer();


        int type = hexToDecimal(reveicedBytes[typeIndex]);

        switch (type) {
            case PcrCommand.STEP_1_TYPE:
                //执行step2
                PcrCommand command = new PcrCommand();
                float temp = 105;
                short during = 0;
                command.step2(temp, during);
                mCommunicationService.sendPcrCommand(command);
                //mUsbService.sendPcrCommand(command);
                break;
        }

    }

    private int mCurCycling;//当前是第x个循环
    private int mCyclingStageIndex;//当前处于循环的cyclingstage index

    /**
     * 接收到step2返回数据，开始执行step3,设定循环参数
     *
     * @param data
     */
    private void doReceiveStep2(Data data) {
        int typeIndex = 4;
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        if (type == PcrCommand.STEP_2_TYPE) {

            mCyclingStageIndex = 0;
            //mCurCycling = 1;

            step3();

        }
    }

    private int mCyclingStageCount;

    private CyclingStage getCurCyclingStage() {
        List<Stage> stageList = getCyclingSteps();
        mCyclingStageCount = stageList.size();
        return (CyclingStage) stageList.get(mCyclingStageIndex);
    }

    /**
     * 设置循环参数
     */
    private void step3() {
        //执行step3
        PcrCommand command = new PcrCommand();
        CyclingStage cyclingStage = getCurCyclingStage();
        //int cyclingCount = cyclingStage.getCyclingCount();

        int picIndex = 1;
        List<PcrCommand.TempDuringCombine> combines = new ArrayList<>();
        for (int i = 0; i < cyclingStage.getPartStageList().size(); i++) {
            PartStage partStage = cyclingStage.getPartStageList().get(i);
            if (partStage.isTakePic()) {
                picIndex = i + 1;
            }
            PcrCommand.TempDuringCombine combine = new PcrCommand.TempDuringCombine(partStage.getTemp(),
                    partStage.getDuring());
            combines.add(combine);
        }

        int rsvd = 0;
       /* if (mHistoryExperiment.getSettingSecondInfo().getModes().size()>1){
            rsvd=1;
        }*/
        //command.step3(cyclingCount, mCurCycling, picIndex, cyclingStage.getPartStageList().size(), combines);
        command.step3(rsvd, picIndex, cyclingStage.getPartStageList().size(), combines);
        mCommunicationService.sendPcrCommand(command);
        //mUsbService.sendPcrCommand(command);


    }

    private void doReceiveStep3or4(Data data) {
        int typeIndex = 4;
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        switch (type) {
            case PcrCommand.STEP_3_TYPE:
                //检查循环是否已经启动
                if (!mCyclingRun) {
                    //启动循环
                    startCycling();
                } /*else {
                    delayAskTriggerStatus();
                }*/

                break;
            case PcrCommand.STEP_4_TYPE:
                //启动循环返回，查询循环状态
                //askIfContinuePolling();
                step5();

                break;
            case PcrCommand.STEP_MELTING_TYPE:
                //启动溶解曲线返回，查询查询循环状态
                step5();
                break;
        }
    }

    private void step5() {
        mInReadingImg = false;
        PcrCommand command = new PcrCommand();
        command.step5();
        mCommunicationService.sendPcrCommand(command);
        //mUsbService.sendPcrCommand(command);
    }

    /* *//**
     * 询问图像板是否准备好数据
     *//*
    private void askTriggerStatus() {
        PcrCommand command = new PcrCommand();
        command.step5();
        mBluetoothService.write(command);
    }

    private void delayAskTriggerStatus() {
        //是否已经设置下一个循环
        askIfContinuePolling();
        Observable.timer(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        askTriggerStatus();
                    }
                });
    }*/

    /**
     * 是否已经启动循环
     */
    private boolean mCyclingRun;

    /**
     * 启动循环
     */
    private void startCycling() {
        mCyclingRun = true;
        PcrCommand command = new PcrCommand();
        List<Stage> cyclingSteps = mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
        CyclingStage cyclingStage = (CyclingStage) cyclingSteps.get(mCyclingStageIndex);
        int cyclingCount = cyclingStage.getCyclingCount();
        List<Stage> stageList = mHistoryExperiment.getSettingSecondInfo().getSteps();
        StartStage startStage = (StartStage) stageList.get(0);
        EndStage endStage = (EndStage) stageList.get(stageList.size() - 1);

        PcrCommand.TempDuringCombine predenaturationCombine = new PcrCommand.TempDuringCombine(startStage.getTemp(), startStage.getDuring());
        PcrCommand.TempDuringCombine extendCombine = new PcrCommand.TempDuringCombine(endStage.getTemp(), endStage.getDuring());
//        LogUtil.e("Step4", "predenaturationCombine:" + predenaturationCombine.getTemp() + "-" + predenaturationCombine.getDuring());
  //      LogUtil.e("Step4", "extendCombine:" + extendCombine.getTemp() + "-" + extendCombine.getDuring());

        PcrCommand.CmdMode cmdMode = PcrCommand.CmdMode.NORMAL;
        /*if (mHistoryExperiment.getSettingSecondInfo().getModes().size()>1){
            cmdMode=PcrCommand.CmdMode.CONTINU;
        }*/
        command.step4(PcrCommand.Control.START, cyclingCount, cmdMode,
                predenaturationCombine, extendCombine);

        mCommunicationService.sendPcrCommand(command);
        //mUsbService.sendPcrCommand(command);

    }

    private List<ChannelImageStatus> mChannelStatusList;

    /**
     * 查询是否有中断
     */
    private void step6() {
        PcrCommand command = new PcrCommand();
        command.step6();
        mCommunicationService.sendPcrCommand(command);
        //mUsbService.sendPcrCommand(command);
    }

    /**
     * 处于溶解曲线阶段
     */
    private boolean mInMeltCurve;
    private boolean mExpeFinished;
    private boolean mStep5Responsed;
    Subscription step5Subscription;

    /**
     * 返回循环状态
     *
     * @param data
     */
    private void doReceiveStep5(Data data) {
        mStep5Responsed = true;
        int typeIndex = 4;
        int dataIndex = 5;
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        if (type == PcrCommand.STEP_5_TYPE) {
            //当前循环状态
            byte status = reveicedBytes[dataIndex];
            switch (status) {
                case 0://IDLE
                    mInCycling = false;
                    mExpeFinished = true;
                    break;
                case 1://LID HEAT
                    mInCycling = false;
                    tv_cycling_desc.setText("加热中");
                    tv_cycling.setText("");
                    break;
                case 2://CYCLING
                    if (mInCycling == false) {
                        mCurCycling = 1;
                    }
                    int cyclingCount = getCurCyclingStage().getCyclingCount();
                    if (mCyclingStageCount > 1) {
                        int step = mCyclingStageIndex + 1;
                        String stepDesc = "阶段" + step + "循环：";
                        tv_cycling_desc.setText(stepDesc);

                    } else {
                        tv_cycling_desc.setText("循环：");
                    }
                    if (mCurCycling > cyclingCount) {
                        mCurCycling = cyclingCount;
                    }
                    tv_cycling.setText(mCurCycling + "/" + cyclingCount);
                    mInCycling = true;
                    break;
                case 3://COOL DOWN
                    tv_cycling_desc.setText("冷却中");
                    tv_cycling.setText("");
                    mInCycling = false;


                    // mExpeFinished = true;
                    break;
                case 4:
                    tv_cycling_desc.setText("");
                    tv_cycling.setText("");
                    //熔解曲线预热阶段
                    tv_cur_mode.setText("当前模式：熔解曲线");
                    mInCycling = false;
                    mInMeltCurve = true;
                    break;
                case 5:
                    tv_cur_mode.setText("当前模式：熔解曲线");
                    tv_cycling_desc.setText("");
                    tv_cycling.setText("");
                    //熔解曲线阶段
                    mInCycling = false;
                    mInMeltCurve = true;
                    break;
            }
            if (status == 1 || status == 3) {

                mStep5Responsed = false;
                if (step5Subscription == null) {
                    step5Subscription = Observable.interval(1000, 1000, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Long>() {
                                @Override
                                public void call(Long aLong) {
                                    if (!mStep5Responsed) {
                                        step5();
                                    }

                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    throwable.printStackTrace();
                                    if (!mStep5Responsed) {
                                        step5();
                                    }
                                }
                            });
                }

               /* mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        step5();
                    }
                });*/

                return;
            }
            if (step5Subscription != null && !step5Subscription.isUnsubscribed()) {
                step5Subscription.unsubscribe();
                step5Subscription = null;
            }
            if (status == 0) {//实验已经结束
                if (!mBackPressed){
                    ToastUtil.showToast(getActivity(), "实验结束");
                    //TODO 自动跳转到实验数据分析页面

                    toAnalyzePage();

                    EventBus.getDefault().post(new ExpeNormalFinishEvent());
                    ActivityUtil.finish(getActivity());
                }

                return;
            }

            checkHasNewParam(reveicedBytes);
            if (mInCycling || mInMeltCurve) {
                //温度循环中，执行step6
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                step6();
            }


        }

    }

    private void toAnalyzePage() {
        mHistoryExperiment.setDuring(tv_duration.getDuring());
        mHistoryExperiment.setFinishMilliTime(System.currentTimeMillis());
        //数据
        //变增扩温曲线数据
        //TODO 溶解曲线曲线数据
       /* ChartData chartData = new ChartData();
        List<com.wind.data.expe.bean.LineData> lineDataList = new ArrayList<>();
        for (int i = 0; i < mDataSets.size(); i++) {
            LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
            List<Entry> entries = lineDataSet.getValues();
            com.wind.data.expe.bean.LineData lineData = new com.wind.data.expe.bean.LineData();
            lineData.setEntries(entries);
            lineData.setColor(mLineColors.get(i));
            lineDataList.add(lineData);

        }
        chartData.setLineDataList(lineDataList);
        mHistoryExperiment.setDtChartData(chartData);*/
        Tab tab = new Tab();
        tab.setIndex(MainActivity.TAB_INDEX_DATA);
        tab.setExtra(mHistoryExperiment);
        MainActivity.start(getActivity(), tab);
    }

    private void checkHasNewParam(byte[] reveicedBytes) {
        int dataIndex = 5;
        byte cfg = reveicedBytes[dataIndex + 1];
        int mode = ByteHelper.getHigh4(cfg);

        int hasNewParam = reveicedBytes[dataIndex + 2];
        if (hasNewParam == 0) {
            //还没有设置新参数
            /*
             * 查看当前cyclingstage循环是否已经结束，
             *  a.如果当前还有没有执行的循环，继续下一个循环
             *  b.当前cyclingstage循环已经全部执行，查看是否还有一个cyclingstage，有的话继续
             */
            int cyclingSteps = getCyclingSteps().size();
            if (cyclingSteps > mCyclingStageIndex + 1) {
                // CyclingStage nextCyclingStage = (CyclingStage) getCyclingSteps().get(mCyclingStageIndex);
                mCurCycling = 1;
                mCyclingStageIndex++;
                step3();
            } else {
                /*
                 * TODO 没有新参数需要设置了，查看是否有设置溶解曲线模式
                 * a.没有就结束实验,跳转到数据分析页面
                 * b.设置了溶解曲线，开始该模式
                 */

                List<Mode> modeList = mHistoryExperiment.getSettingSecondInfo().getModes();
                if (modeList.size() > 1) {
                    if (!mMeltingCurveStarted) {
                        startMeltingCurveSync();
                    }
                }

            }
            // }

        } else if (hasNewParam == 1) {
            //有新参数在等待当前循环结束
        }
    }

    //  private PcrCommand.PCR_IMAGE mNextPcrImage;
    private void doReceiveStep6(Data data) {
        mChannelStatusList = new ArrayList<>();
        int typeIndex = 4;
        int dataIndex = 5;
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        if (type == PcrCommand.STEP_6_TYPE) {
            int status = hexToDecimal(reveicedBytes[dataIndex]);
            int channel0 = status & 0x1;
            int channel1 = status >> 1 & 0x1;
            int channel2 = status >> 2 & 0x1;
            int channel3 = status >> 3 & 0x1;
            ChannelImageStatus channel0ImageStatus = new ChannelImageStatus(0, mImageMode.getSize());
            channel0ImageStatus.setReadable(channel0 == 1);
            mChannelStatusList.add(channel0ImageStatus);

            ChannelImageStatus channel1ImageStatus = new ChannelImageStatus(1, mImageMode.getSize());
            channel1ImageStatus.setReadable(channel1 == 1);
            mChannelStatusList.add(channel1ImageStatus);

            ChannelImageStatus channel2ImageStatus = new ChannelImageStatus(2, mImageMode.getSize());
            channel2ImageStatus.setReadable(channel2 == 1);
            mChannelStatusList.add(channel2ImageStatus);

            ChannelImageStatus channel3ImageStatus = new ChannelImageStatus(3, mImageMode.getSize());
            channel3ImageStatus.setReadable(channel3 == 1);
            mChannelStatusList.add(channel3ImageStatus);
            int readableIndex = -1;
            for (int i = 0; i < mChannelStatusList.size(); i++) {
                if (mChannelStatusList.get(i).isReadable()) {
                    readableIndex = i;
                    break;
                }
            }
            if (readableIndex == -1) {
                //图像板还未准备好,继续询问
               /* mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        step6();
                    }
                });*/
                //图像板还未准备好,应该询问循环状态
               /* try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                step5();*/
                if (step5Subscription == null) {
                    mStep5Responsed = false;
                    step5Subscription = Observable.interval(1000, 1000, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Long>() {
                                @Override
                                public void call(Long aLong) {
                                    if (!mStep5Responsed) {
                                        step5();
                                    }

                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    throwable.printStackTrace();
                                    if (!mStep5Responsed) {
                                        step5();
                                    }
                                }
                            });
                }

            } else {
                //循环数加1
                if (mInCycling) {
                    mCurCycling++;
                }
                //读取通道图像数据
                step7(mChannelStatusList.get(readableIndex).getPctImageCmd());
            }

        }
    }

    private void step7(PcrCommand.PCR_IMAGE pcr_image) {
        mInReadingImg = true;
        PcrCommand command = new PcrCommand();
        command.step7(pcr_image);
        mCommunicationService.sendPcrCommand(command);
        //mUsbService.sendPcrCommand(command);
    }

    /**
     * 是否正在读取图像数据
     */
    private boolean mInReadingImg;

    /**
     * 读取PCR数据
     * <p>
     * private void readPcrData() {
     * PcrCommand command = new PcrCommand();
     * command.step6(mNextPcrImage);
     * mBluetoothService.write(command);
     * }
     */

    private void doReceiveStep7(Data data) {
        /*
         *  TODO 1，解析图像数据，绘制到图表中，需要判断是扩增阶段，还是溶解阶段。
         *       2，如果没有读取到最后一行，就继续读取
         *
         *       下位机在进入溶解曲线功能后，回复的图像时，第一行图像数据包的最后，会增加温度信息 （float， 4byte）
         *       上位机通过polling下位机温度循环状态，可判断下位机是在温度循环中还是溶解曲线中，据此判断图像数据报中是否包含温度信息。
         */

        int lengthIndex = 3;
        int typeIndex = 4;
        int dataStartIndex = 5;
        //header ack cmd length  type  row   pix1 pix2 pix3 pix4 pix5 pix6 pix7 pix8 pix9 pix10 pix11 pix12
        //aa      00  02    1c   02    0b    0711 2b12 0011 1711 5012 bb10 6311 8b0a 6311 741b  b50e  000e  d07a 0d 1717
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        int length = hexToDecimal(reveicedBytes[lengthIndex]);
        //int total = reveicedBytes[dataStartIndex++];
        int curRowIndex = reveicedBytes[dataStartIndex++];//从0开始
       /* byte pixs[] = new byte[mImageMode.getSize() * 2];//每个图像像素有2个字节组成
        for (int i = 0; i < mImageMode.getSize() * 2; i++) {
            pixs[i] = reveicedBytes[dataStartIndex++];
        }*/


        //处于温度循环状态
        boolean readed = false;//本通道数据是否已经读完
        int channelIndex = -1;
        String chip = "Chip#unknow";
        if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_0.getValue()) {
            //通道1图像数据返回
            mChannelStatusList.get(0).setCurReadRow((curRowIndex + 1));//
            readed = mChannelStatusList.get(0).readed();
            channelIndex = 1;
            chip = "Chip#1";
        } else if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_1.getValue()) {
            //通道2图像数据返回
            mChannelStatusList.get(1).setCurReadRow((curRowIndex + 1));//
            readed = mChannelStatusList.get(1).readed();
            channelIndex = 2;
            chip = "Chip#2";
        } else if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_2.getValue()) {
            //通道3图像数据返回
            mChannelStatusList.get(2).setCurReadRow((curRowIndex + 1));//
            readed = mChannelStatusList.get(2).readed();
            channelIndex = 3;
            chip = "Chip#3";
        } else if (type == PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_3.getValue()) {
            //通道4图像数据返回
            mChannelStatusList.get(3).setCurReadRow((curRowIndex + 1));//
            readed = mChannelStatusList.get(3).readed();
            channelIndex = 4;
            chip = "Chip#4";
        }

        StringBuilder hex = new StringBuilder(reveicedBytes.length * 2);
        for (byte b : reveicedBytes) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }


        File file = null, sourceFile = null;
        if (mInCycling) {
            System.out.println("通道" + channelIndex + "图像数据第" + curRowIndex + ":" + hex.toString().toLowerCase());
            file = DataFileUtil.getDtImageDataFile(mHistoryExperiment);
            sourceFile = DataFileUtil.getDtImageDataSourceFile(mHistoryExperiment);
        } else if (mInMeltCurve) {
            System.out.println("溶解曲线通道" + channelIndex + "图像数据第" + curRowIndex + ":" + hex.toString().toLowerCase());
            file = DataFileUtil.getMeltImageDateFile(mHistoryExperiment);
            sourceFile = DataFileUtil.getMeltImageDataSourceFile(mHistoryExperiment);
        }
        //保存到本地文件中
        if (curRowIndex == 0) {

            appendToFile(chip, file);
            sourceAppendToFile(chip, sourceFile);
        }
        //保存图像板返回的原始数据到文件
        //将0x1717后面的数据抹去
        String source=hex.toString().toLowerCase();
        int index=source.indexOf("1717");
        source=source.substring(0,index+4);
        sourceAppendToFile(source, sourceFile);
       // if (mInCycling) {
            //TODO 循环过程数据转换待理解
            String imageData = transferImageData(channelIndex, curRowIndex, reveicedBytes);
            System.out.println("trim后的图像数据："+imageData);
            //保存经过矫正图像板数据到文件
            appendToFile(imageData, file);
        //}
        //检查本通道是否已经读取完毕
        if (readed) {
            //执行下一个通道的读取操作
            int next = -1;
            for (int i = 0; i < mChannelStatusList.size(); i++) {
                ChannelImageStatus status = mChannelStatusList.get(i);
                if (status.isReadable() && !status.readed()) {
                    next = i;
                    break;
                }
            }
            if (next == -1) {//全部通道已经全部读取完

                //TODO 绘制图形
                //mExecutorService.execute(mRun);
                showChart();
                /**
                 * 读循环状态
                 */
                step5();
            } else {
                step7(mChannelStatusList.get(next).getPctImageCmd());
            }
        }



}

    private String transferImageData(int chan, int k, byte[] reveicedBytes) {
        int count;
        if (mInCycling){
            count=mImageMode.getSize() + 1;
        }else {
            count=mImageMode.getSize()+2;
        }
        int[] txData = new int[count];

        //aa 00 02 1c 02 0b 07112b12001117115012bb1063118b0a6311741bb50e000ed07a0d1717
        for (int numData = 0; numData < mImageMode.getSize(); numData++) {
            byte high = reveicedBytes[numData * 2 + 7];
            byte low = reveicedBytes[numData * 2 + 6];

            int value = TrimReader.getInstance()
                    .tocalADCCorrection(numData, high, low,
                            mImageMode.getSize(), chan,
                            CommData.gain_mode, 0);

            txData[numData] = value;
        }
        //当前行号
        txData[mImageMode.getSize()] = reveicedBytes[5];
        if (mInMeltCurve){
            if ( reveicedBytes[5]==0){
                //TODO 待验证
                byte[] buffers = new byte[4];
                buffers[0] = reveicedBytes[CommData.imgFrame * 2 + 6];
                buffers[1] = reveicedBytes[CommData.imgFrame * 2 + 7];
                buffers[2] = reveicedBytes[CommData.imgFrame * 2 + 8];
                buffers[3] = reveicedBytes[CommData.imgFrame * 2 + 9];
                //float t = BitConverter.ToSingle(buffers, 0);
                float t =ByteUtil.getFloat(buffers);
                txData[count - 1] = (int) t;
            }
        }
        String res = "";
        String newres = "";
        for (int i = 0; i < txData.length; i++) {
            if (i == 0) {
                res = txData[i] + "";
                newres = txData[i] + "";
            } else {
                if (i != 11 && i != 23) {
                    res += " " + txData[i];
                    newres += "    " + txData[i];
                } else {
                    if (k == 11 || k == 23) {
                        res += " " + mFactUpdater.GetFactValueByXS(chan);
                        newres += "    " + mFactUpdater.GetFactValueByXS(chan);
                    } else {
                        res += " " + txData[i];
                        newres += "    " + txData[i];
                    }

                }

            }

        }


        return res;
    }


    /*
     * 一个循环图像及数据显示处理完毕,询问是否继续polling图像板

    private void askIfContinuePolling() {
        PcrCommand command = new PcrCommand();
        command.step7();
        mBluetoothService.write(command);
    } */

    /**
     * 是处于温度循环中还是溶解曲线中
     */
    private boolean mInCycling = true;


    private void sourceAppendToFile(String txt, File file) {

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            if (!file.exists()) {
                boolean hasFile = file.createNewFile();
               /* if (hasFile) {
                    System.out.println("file not exists, create new file");
                }*/
                fos = new FileOutputStream(file);
            } else {
                // System.out.println("file exists");
                fos = new FileOutputStream(file, true);
            }

            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(txt); //写入内容
            osw.write("\r\n");  //换行
        } catch (Exception e) {
            e.printStackTrace();
        } finally {   //关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void appendToFile(String txt, File file) {
     /*   String fileName=DateUtil.get(mHistoryExperiment.getMillitime(),"yyyy_MM_dd_hh_mm_ss")+".txt";
        String filePath= C.Value.IMAGE_DATA+fileName;*/

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;

        try {
            if (!file.exists()) {
                boolean hasFile = file.createNewFile();
               /* if (hasFile) {
                    System.out.println("file not exists, create new file");
                }*/
                fos = new FileOutputStream(file);
            } else {
                // System.out.println("file exists");
                fos = new FileOutputStream(file, true);
            }

            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(txt); //写入内容
            osw.write("\r\n");  //换行
        } catch (Exception e) {
            e.printStackTrace();
        } finally {   //关闭流
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 溶解曲线是否已经启动
     */
    private boolean mMeltingCurveStarted;

    private void startMeltingCurveSync() {
        mMeltingCurveStarted = true;
        PcrCommand command = new PcrCommand();
        float startT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getStartTemperature());
        float endT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getEndTemperature());
        float speed = 1;
        command.meltingCurve(PcrCommand.Control.START, startT, endT, speed);

        mCommunicationService.sendPcrCommandSync(command);
        //mUsbService.sendPcrCommandSync(command);

        // delayAskTriggerStatus();
    }

    private void stopMeltingCurve() {
        PcrCommand command = new PcrCommand();
        float startT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getStartTemperature());
        float endT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getEndTemperature());
        float speed = 1;
        command.stopMelting(startT, endT, speed);
        mCommunicationService.sendPcrCommandSync(command);
        //mUsbService.sendPcrCommandSync(command);
    }

    private void stopCycling() {
        PcrCommand command = new PcrCommand();
        List<Stage> cyclingSteps = mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
        CyclingStage cyclingStage = (CyclingStage) cyclingSteps.get(mCyclingStageIndex);
        int cyclingCount = cyclingStage.getCyclingCount();
        List<Stage> stageList = mHistoryExperiment.getSettingSecondInfo().getSteps();
        StartStage startStage = (StartStage) stageList.get(0);
        EndStage endStage = (EndStage) stageList.get(stageList.size() - 1);

        PcrCommand.TempDuringCombine predenaturationCombine = new PcrCommand.TempDuringCombine(startStage.getTemp(), startStage.getDuring());
        PcrCommand.TempDuringCombine extendCombine = new PcrCommand.TempDuringCombine(endStage.getTemp(), endStage.getDuring());

        command.stopCycling(cyclingCount, PcrCommand.CmdMode.NORMAL, predenaturationCombine, extendCombine);
        mCommunicationService.sendPcrCommandSync(command);
        //mUsbService.sendPcrCommandSync(command);
    }

    private List<Stage> getCyclingSteps() {
        return mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
    }
}
