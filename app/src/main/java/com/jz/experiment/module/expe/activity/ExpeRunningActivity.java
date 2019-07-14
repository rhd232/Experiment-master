package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.anitoa.Anitoa;
import com.anitoa.ExpeType;
import com.anitoa.bean.Data;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.event.AnitoaDisConnectedEvent;
import com.anitoa.listener.AnitoaConnectionListener;
import com.anitoa.receiver.BluetoothReceiver;
import com.anitoa.service.BluetoothService;
import com.anitoa.service.CommunicationService;
import com.anitoa.service.UsbService;
import com.anitoa.util.AnitoaLogUtil;
import com.anitoa.util.ByteUtil;
import com.anitoa.util.ThreadUtil;
import com.anitoa.well.Well;
import com.github.mikephil.charting.charts.LineChart;
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.DtChart;
import com.jz.experiment.chart.FactUpdater;
import com.jz.experiment.chart.MeltingChart;
import com.jz.experiment.chart.TempChart;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.data.FilterActivity;
import com.jz.experiment.module.expe.bean.ChannelImageStatus;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.expe.event.ExpeNormalFinishEvent;
import com.jz.experiment.module.expe.event.FilterEvent;
import com.jz.experiment.module.expe.event.SavedExpeDataEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.ByteHelper;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.ExpeJsonGenerator;
import com.jz.experiment.util.ImageDataReader;
import com.jz.experiment.util.StatusChecker;
import com.jz.experiment.util.TrimReader;
import com.jz.experiment.widget.DuringView;
import com.wind.base.BaseActivity;
import com.wind.base.C;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Navigator;
import com.wind.data.expe.bean.Channel;
import com.wind.data.expe.bean.ExpeSettingSecondInfo;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Mode;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.request.GenerateExpeJsonRequest;
import com.wind.data.expe.request.InsertExpeRequest;
import com.wind.data.expe.response.InsertExpeResponse;
import com.wind.toastlib.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class ExpeRunningActivity extends BaseActivity implements AnitoaConnectionListener {


    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, ExpeRunningActivity.class, experiment);
    }

    @BindView(R.id.chart_dt)
    LineChart chart_dt;
    @BindView(R.id.chart_melt)
    LineChart chart_melt;

    @BindView(R.id.chart_temp)
    LineChart chart_temp;

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
    private TempChart mTempChart;
    private FactUpdater mFactUpdater;
    /**
     * 是否包含两个StartStage
     */
    private boolean mHasTwoStartStage;
    private Handler mHander;
    @Override
    protected void setTitle() {
        String running=getString(R.string.title_running);
        String filter=getString(R.string.running_filter);
        mTitleBar.setTitle(running);
        mTitleBar.setRightText(filter);
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterActivity.start(getActivity());
            }
        });
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expe_running);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        mHander=new Handler();
        mImageMode = PcrCommand.IMAGE_MODE.IMAGE_12;
        mHistoryExperiment = Navigator.getParcelableExtra(this);
        Stage stage = mHistoryExperiment.getSettingSecondInfo().getSteps().get(1);
        if (stage instanceof StartStage) {
            mHasTwoStartStage = true;
        }
        List<Mode> modeList = mHistoryExperiment.getSettingSecondInfo().getModes();
        if (modeList.size() > 1) {
            mHasMeltingCurve = true;
        }
        printHistoryInfo();
        String msg=getString(R.string.running_mode_pcr);
        tv_cur_mode.setText(msg);
        tv_cycling_desc.setText("");
        tv_cycling.setText("");
        //启动计时器
        tv_duration.start();


        initChart();


        bindService();
        init();


        mExecutorService = Executors.newCachedThreadPool();

        //mDtChart.show(ChanList,KSList,null);
       /* Thread thread = new Thread(mRun);
        thread.start();*/

       mExecutorService.execute(new Runnable() {
           @Override
           public void run() {
               //删除factor_log.txt文件
               AnitoaLogUtil.getOrCreateFile("factor_log.txt").delete();

               List<Stage> cyclingSteps=mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
               StringBuilder sb=new StringBuilder();
               sb.append("=========================\n");
               for (int j=0;j<cyclingSteps.size();j++){

                   CyclingStage c= (CyclingStage) cyclingSteps.get(j);
                   sb.append("循环阶段"+(j+1));
                   sb.append(" ");
                   sb.append("循环数："+c.getCyclingCount());
                   sb.append(" ");
                   int picIndex=-1;
                   for (int i = 0; i < c.getPartStageList().size(); i++) {
                       PartStage partStage = c.getPartStageList().get(i);
                       sb.append("阶段"+(i+1));
                       sb.append(" ");
                       sb.append("温度："+  partStage.getTemp());
                       sb.append(" ");
                       sb.append("持续时间："+  partStage.getDuring());
                       sb.append(" ");
                       if (partStage.isTakePic()){
                           picIndex=(i+1);
                       }
                   }
                   if (picIndex==-1){
                       sb.append("拍照阶段：无");
                   }else {
                       sb.append("拍照阶段：" + picIndex);
                   }
                   sb.append("\n");
               }
               sb.append("=========================\n");
               AnitoaLogUtil.writeFileLog(sb.toString());
           }
       });

    }

    private void printHistoryInfo() {
        ExpeSettingSecondInfo secondInfo = mHistoryExperiment.getSettingSecondInfo();
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < secondInfo.getSteps().size(); i++) {
            Stage stage = secondInfo.getSteps().get(i);


            if (stage instanceof CyclingStage) {
                CyclingStage cyclingStage = (CyclingStage) stage;
                sBuilder.append("循环数:" + cyclingStage.getCyclingCount());
                List<PartStage> partStages = cyclingStage.getPartStageList();
                for (int j = 0; j < partStages.size(); j++) {
                    PartStage partStage = partStages.get(j);
                    partStage.getTemp();
                    partStage.getDuring();

                    sBuilder.append("第" + i + "-" + j + "个温度:" + partStage.getTemp())
                            .append("时间:" + partStage.getDuring());
                }

            } else {
                sBuilder.append("第" + i + "个温度:" + stage.getTemp())
                        .append("时间:" + stage.getDuring());
            }
            sBuilder.append("\n");

        }
        AnitoaLogUtil.writeFileLog(sBuilder.toString());
    }

    private void bindService() {

//        mBluetoothService = Anitoa.getInstance(getActivity()).getBluetoothService();
//        mUsbService = Anitoa.getInstance(getActivity()).getUsbService();
        mCommunicationService.setNotify(this);
        chart_dt.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ActivityUtil.isFinish(getActivity())) {
                    AnitoaLogUtil.writeFileLog("===========开始实验==========");
                    step0();
                    step1();
                }
            }
        }, 500);


      /*  mBluetoothReceiver = new BluetoothReceiver();
        mBluetoothReceiver.setBluetoothConnectInteface(this);
        getActivity().registerReceiver(mBluetoothReceiver,
                makeIntentFilter());*/


    }

    private void step0() {
        PcrCommand cmd = new PcrCommand();
        cmd.setChan();
        mCommunicationService.sendPcrCommandSync(cmd);
        ThreadUtil.sleep(50);
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
        if (mCommunicationService instanceof UsbService && !mClosedPcrProgram) {
            UsbService usbService = (UsbService) mCommunicationService;
            usbService.startReadThread();
        }

        // 给设备发指令 step1
        PcrCommand cmd = new PcrCommand();
        List<Channel> channels = mHistoryExperiment.getSettingsFirstInfo().getChannels();
        int[] channelOp = new int[channels.size()];
        /*for (int i = 0; i < channels.size(); i++) {
            String value = channels.get(i).getValue();
            if (TextUtils.isEmpty(value)) {
                channelOp[i] = 0;
            } else {
                channelOp[i] = 1;
            }
        }*/
        for (int i = 0; i < 4; i++) {
            channelOp[i] = 1;
        }
        cmd.step1(channelOp);
        mCommunicationService.sendPcrCommand(cmd);

    }

    private void initChart() {
        mCommunicationService = Anitoa.getInstance(getActivity()).getCommunicationService();
        mFactUpdater = FactUpdater.getInstance(mCommunicationService);
        mFactUpdater.SetInitData();
        //统计总共有多少循环（不拍照的不包括）
        int totalCyclingCount=0;
        List<Stage> cyclingSteps=mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
        for (int i=0;i<cyclingSteps.size();i++){
            CyclingStage cyclingStage= (CyclingStage) cyclingSteps.get(i);
            boolean pic=false;
            for (PartStage partStage:cyclingStage.getPartStageList()){
                if (partStage.isTakePic()){
                    pic=true;
                    break;
                }
            }
            if (pic){
                totalCyclingCount+=cyclingStage.getCyclingCount();
            }
        }

        mDtChart = new DtChart(chart_dt,totalCyclingCount, mFactUpdater);
        mDtChart.setRunning(true);
        mMeltingChart = new MeltingChart(chart_melt, mFactUpdater);
        if (mHasMeltingCurve){
            float start;
            try {
                 start=Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getStartTemperature());
            }catch (Exception e){
                e.printStackTrace();
                start=40;
            }
            mMeltingChart.setStartTemp(start);
            mMeltingChart.setAxisMinimum(start);
        }
        mMeltingChart.setRunning(true);


        mTempChart = new TempChart(chart_temp);

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

        KSList = Well.getWell().getKsList();
    }


    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            showChart();
        }
    };

    private void showChart() {
        if (mInCycling)
            showDtChart();
        if (mInMeltCurve) {
            showMeltChart();
        }
    }

    private void showMeltChart() {
        if (chart_melt.getVisibility() == View.GONE) {
            chart_melt.setVisibility(View.VISIBLE);
        }
        if (chart_dt.getVisibility() == View.VISIBLE) {
            chart_dt.setVisibility(View.GONE);
        }
        mMeltingChart.show(ChanList, KSList, DataFileUtil.getMeltImageDateFile(mHistoryExperiment),null);
    }

    private void showDtChart() {
        if (chart_melt.getVisibility() == View.VISIBLE) {
            chart_melt.setVisibility(View.GONE);
        }
        if (chart_dt.getVisibility() == View.GONE) {
            chart_dt.setVisibility(View.VISIBLE);
        }

        mDtChart.show(ChanList, KSList, DataFileUtil.getDtImageDataFile(mHistoryExperiment),null);

    }

    private boolean mBackPressed;

    @Override
    public void onBackPressed() {
        if (mBackPressed) {
            return;
        }
        mBackPressed = true;
        if (!mExpeFinished) {

            String msg = getString(R.string.running_dialog_stop_msg);
            AppDialogHelper.showNormalDialog(getActivity(), msg, new AppDialogHelper.DialogOperCallback() {
                @Override
                public void onDialogCancelClick() {
                    super.onDialogCancelClick();
                    mBackPressed = false;
                }

                @Override
                public void onDialogConfirmClick() {
                    LoadingDialogHelper.showOpLoading(getActivity());

                    closeDevice()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            LoadingDialogHelper.hideOpLoading();
                            EventBus.getDefault().post(new ExpeNormalFinishEvent());
                            ActivityUtil.finish(getActivity());
                        }
                    });


                }
            });
        } else {
            EventBus.getDefault().post(new ExpeNormalFinishEvent());
            ActivityUtil.finish(getActivity());
        }
    }
    private Observable<Boolean> closeDeviceAndMeltingAutoInt() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    stopSubscription();
                    long startTime = System.currentTimeMillis();
                    while (mInReadingImg) {
                        long during = System.currentTimeMillis() - startTime;
                        if (during > 5000) {
                            break;
                        }
                    }
                 /*   if (mCommunicationService instanceof UsbService) {
                        UsbService usbService = (UsbService) mCommunicationService;
                        usbService.stopReadThread();
                    }*/

                    Thread.sleep(100);

                    if (!mExpeFinished) {
                        stopCycling();
                    }
                    Thread.sleep(100);

                    if (mHistoryExperiment.isAutoIntegrationTime()) {
                        autoInt(mFactUpdater);
                    }
                    subscriber.onNext(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }
    private Observable<Boolean> closeDevice() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    stopSubscription();
                    long startTime = System.currentTimeMillis();
                    while (mInReadingImg) {
                        long during = System.currentTimeMillis() - startTime;
                        if (during > 5000) {
                            break;
                        }
                    }
                 /*   if (mCommunicationService instanceof UsbService) {
                        UsbService usbService = (UsbService) mCommunicationService;
                        usbService.stopReadThread();
                    }*/

                    Thread.sleep(100);

                    if (!mExpeFinished) {
                        if (mHasMeltingCurve) {
                            if (mInMeltCurve) {
                                stopMeltingCurve();
                            }
                        }

                        stopCycling();

                    }

                    subscriber.onNext(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    @OnClick({R.id.tv_stop})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_stop:
                //强行终止：停止循环和停止溶解曲线
                onBackPressed();


                //toAnalyzePage();

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
    public void onBluetoothDisConnectedEvent(AnitoaDisConnectedEvent event) {

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

    private void stopSubscription() {
        if (step4Subscription != null && !step4Subscription.isUnsubscribed()) {
            step4Subscription.unsubscribe();
            step4Subscription = null;
        }
        if (step5Subscription != null && !step5Subscription.isUnsubscribed()) {
            step5Subscription.unsubscribe();
            step5Subscription = null;
        }
        if (step6Subscription != null && !step6Subscription.isUnsubscribed()) {
            step6Subscription.unsubscribe();
            step6Subscription = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCommunicationService!=null){
            mCommunicationService.setNotify(null);

        }


        if (tv_duration != null) {
            tv_duration.stop();
        }
        stopSubscription();
        //getActivity().unregisterReceiver(mBluetoothReceiver);
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
       // System.out.println("onReceivedData");
        //接收到数据 TODO 根据command和type执行操作
        byte[] reveicedBytes = data.getBuffer();
        int statusIndex = 1;
        int status = hexToDecimal(reveicedBytes[statusIndex]);
        //TODO 检查返回的包是否正确
        boolean succ = StatusChecker.checkStatus(status);
        if (!succ) {
            //readTemperature();
            /*if (!ActivityUtil.isFinish(getActivity())) {
                AppDialogHelper.showSingleBtnDialog(getActivity(), StatusChecker.getStatusDesc(status), new AppDialogHelper.DialogOperCallback() {
                    @Override
                    public void onDialogConfirmClick() {

                    }
                });
                AnitoaLogUtil.writeFileLog("返回错误：" + ByteUtil.getHexStr(reveicedBytes, reveicedBytes.length));
            }*/
            /*
                报出比较多的 图像命令超时错误
                这里的处理改为继续查询循环状态

                图像命令超时 是查询图像中断的指令报出的。
             */
            // 图像命令超时可以继续查询循环状态 避免报出图像命令超时错误
            // step5Subscription();
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
       // System.out.println("doReceiveStep1");
        switch (type) {
            case PcrCommand.STEP_1_TYPE:
                //System.out.println("doReceiveStep1STEP_1_TYPE");
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


            if (mHasMeltingCurve && mPcrFinished){
                startMeltingCurve().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                step5();
                            }
                        });
            }else {
                mCyclingStageIndex = 0;
                step3();
            }


        }
    }

    private int mCyclingStageCount;

    private CyclingStage getCurCyclingStage() {
        List<Stage> stageList = getCyclingSteps();
        mCyclingStageCount = stageList.size();
        return (CyclingStage) stageList.get(mCyclingStageIndex);
    }

    private void step3() {
        step3(false);
    }

    /**
     * 设置循环参数
     */
    private void step3(boolean sync) {
        //执行step3
        PcrCommand command = new PcrCommand();
        CyclingStage cyclingStage = getCurCyclingStage();
        //int cyclingCount = cyclingStage.getCyclingCount();

        //int picIndex = cyclingStage.getPartStageList().size();//默认最后一个阶段
        int picIndex = cyclingStage.getPartStageList().size() + 1;//（阶段数+1）表示不拍照
        List<PcrCommand.TempDuringCombine> combines = new ArrayList<>();
        for (int i = 0; i < cyclingStage.getPartStageList().size(); i++) {
            PartStage partStage = cyclingStage.getPartStageList().get(i);
            if (partStage.isTakePic()) {
                picIndex = i + 1;
            }
            PcrCommand.TempDuringCombine combine = new PcrCommand.TempDuringCombine(partStage.getTemp(),
                    partStage.getDuring());
            combines.add(combine);
            StringBuilder sBuilder = new StringBuilder();
            sBuilder.append("温度+" + i + ":" + partStage.getTemp() + "时间:" + partStage.getDuring());
            System.out.println(sBuilder.toString());
            AnitoaLogUtil.writeFileLog(sBuilder.toString());
        }

        int rsvd = cyclingStage.getCyclingCount();
       /* if (mHistoryExperiment.getSettingSecondInfo().getModes().size()>1){
            rsvd=1;
        }*/
        //command.step3(cyclingCount, mCurCycling, picIndex, cyclingStage.getPartStageList().size(), combines);
        command.step3(rsvd, picIndex, cyclingStage.getPartStageList().size(), combines);
        if (sync) {
            mCommunicationService.sendPcrCommandSync(command);
        } else {
            mCommunicationService.sendPcrCommand(command);
        }


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
                    //step4Subscription();
                }

                break;
            case PcrCommand.STEP_4_TYPE:
                //启动循环返回，查询循环状态
                //askIfContinuePolling();
              /*  mStep4Responsed = true;
                if (step4Subscription != null && !step4Subscription.isUnsubscribed()) {
                    step4Subscription.unsubscribe();
                    step4Subscription = null;
                }*/
                step5();
                //step5Subscription();
                break;
            case PcrCommand.STEP_MELTING_TYPE:
                //启动溶解曲线返回，查询查询循环状态
                //step5();/
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
        //如果含有熔解曲线，那么最后两个是熔解曲线的温度，而不是endstage
        EndStage endStage;
        if (!mHasMeltingCurve) {
            endStage = (EndStage) stageList.get(stageList.size() - 1);
        }else {
            endStage = (EndStage) stageList.get(stageList.size() - 1-2);
        }
        List<PcrCommand.TempDuringCombine> predenaturationCombines = new ArrayList<>();
        PcrCommand.TempDuringCombine predenaturationCombine = new PcrCommand.TempDuringCombine(startStage.getTemp(), startStage.getDuring());
        PcrCommand.TempDuringCombine extendCombine = new PcrCommand.TempDuringCombine(endStage.getTemp(), endStage.getDuring());
        predenaturationCombines.add(predenaturationCombine);

        if (mHasTwoStartStage) {
            StartStage startStage2 = (StartStage) stageList.get(1);
            PcrCommand.TempDuringCombine predenaturationCombine2 = new PcrCommand.TempDuringCombine(startStage2.getTemp(),
                    startStage2.getDuring());
            predenaturationCombines.add(predenaturationCombine2);
        }
        PcrCommand.CmdMode cmdMode = PcrCommand.CmdMode.NORMAL;
        if (cyclingSteps.size() > 1) {
            cmdMode = PcrCommand.CmdMode.CONTINU;
        }


        StringBuilder sBuilder = new StringBuilder();
        for (PcrCommand.TempDuringCombine pre : predenaturationCombines) {
            sBuilder.append("预变性温度：" + pre.getTemp() + "时间：" + pre.getDuring());
        }
        sBuilder.append("结束温度：" + endStage.getTemp() + "时间：" + endStage.getDuring());
        System.out.println(sBuilder.toString());
        AnitoaLogUtil.writeFileLog(sBuilder.toString());


        command.step4(PcrCommand.Control.START, cyclingCount, cmdMode,
                predenaturationCombines, extendCombine);

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

    }

    /**
     * 处于溶解曲线阶段
     */
    private boolean mInMeltCurve;
    private boolean mExpeFinished;
    private boolean mStep5Responsed;
    Subscription step5Subscription;

    private boolean mStartCyclingNum = true;
    private int mRunningCyclingStageIndex = -1;


    private long mPrevReadTempTime;

    private void readTemperature() {
        final long curTime = System.currentTimeMillis();
        if (curTime - mPrevReadTempTime >= 1500) {
            mPrevReadTempTime = curTime;
            float lidTemp = onReadTemperature(PcrCommand.Temperature.LID);
            float peltierTemp = onReadTemperature(PcrCommand.Temperature.PELTIER);
            System.out.println("lidTemp:"+lidTemp+" peltierTemp:"+peltierTemp);
            StringBuilder tempBuilder=new StringBuilder();
            if (mInMeltCurve) {
                tempBuilder.append("熔解曲线中-->");
                tempBuilder.append("lidTemp:" + lidTemp).append(" peltierTemp:" + peltierTemp);
                AnitoaLogUtil.writeFileLog(tempBuilder.toString(),
                        mExecutorService);
            }
            if (lidTemp < 10 || peltierTemp < 10  || (lidTemp==peltierTemp)) {
                return;
            }
            mTempChart.addTemp(lidTemp, peltierTemp);
        }


    }

    private void realReadTemperature(){
        float lidTemp = onReadTemperature(PcrCommand.Temperature.LID);

        float peltierTemp = onReadTemperature(PcrCommand.Temperature.PELTIER);
        System.out.println("lidTemp:"+lidTemp+" peltierTemp:"+peltierTemp);
        if (lidTemp < 10 || peltierTemp < 10) {
            return;
        }
        mTempChart.addTemp(lidTemp, peltierTemp);
    }
    public void readTemperatureSync(){
        realReadTemperature();
    }
    public void readTemperatureAsync(){
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                realReadTemperature();
            }

        });
    }

    public float onReadTemperature(PcrCommand.Temperature temperature) {
        float temp = 0;
        try {
            PcrCommand cmd = PcrCommand.ofReadTemperatureCmd(temperature);
            byte[] bytes = mCommunicationService.sendPcrCommandSync(cmd);
            int statusIndex = 1;
            int cmdIndex=2;
            int typeIndex=4;
            int cmd_=bytes[cmdIndex];
            int type_=bytes[typeIndex];
            if (cmd_==0x10 && type_==0x02) {
                int status = hexToDecimal(bytes[statusIndex]);
                boolean succ = StatusChecker.checkStatus(status);
                if (succ) {
                    byte[] buffers = new byte[4];
                    buffers[0] = bytes[5];
                    buffers[1] = bytes[6];
                    buffers[2] = bytes[7];
                    buffers[3] = bytes[8];
                    temp = ByteUtil.getFloat(buffers);
                }
            }
            //同步读取后将同步buffer置空
            if (mCommunicationService instanceof UsbService){
                UsbService usbService= (UsbService) mCommunicationService;
                usbService.clearSyncBuffer();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return temp;
    }


    private void onExpeCycling(){
        mInCycling = true;
        PcrCommand cyclingInfoCmd = new PcrCommand();
        cyclingInfoCmd.getCyclingInfo();
        byte[] bytes = mCommunicationService.sendPcrCommandSync(cyclingInfoCmd);
        //获取循环数和循环阶段
        if (bytes != null) {
            //检查cmd和type
            int cycling_cmd = bytes[2];
            int cycling_type = bytes[4];
            if (cycling_cmd == 0x14 && cycling_type == 0x1) {
                int cyclingNum = bytes[5];
                int cyclingStep = bytes[6];
                System.out.println("cyclingStep:" + cyclingStep + " cyclingNum:" + cyclingNum);

                mRunningCyclingStageIndex= bytes[7];
                            /*if (cyclingNum == 0) {//只有一个循环时会出问题
                                if (mStartCyclingNum) {
                                    mRunningCyclingStageIndex++;
                                    mStartCyclingNum = false;
                                }

                            } else {
                                mStartCyclingNum = true;
                            }*/
                String c=getString(R.string.running_cycle);
                if (mCyclingStageCount > 1) {
                    String stage=getString(R.string.running_stage);

                    String stepDesc = stage + (mRunningCyclingStageIndex + 1) + c;
                    tv_cycling_desc.setText(stepDesc);

                } else {

                    tv_cycling_desc.setText(c);
                }

                StringBuilder sBuilder=new StringBuilder();
                sBuilder.append("====================\n");
                sBuilder.append("下位机返回的cyclingNum："+cyclingNum+"\n");
                sBuilder.append("下位机返回的cyclingStep："+cyclingStep+"\n");
                sBuilder.append("计算得到的当前阶段"+(mRunningCyclingStageIndex + 1)+"\n");
                sBuilder.append("====================\n");
                AnitoaLogUtil.writeFileLog(sBuilder.toString(),mExecutorService);

                try{
                    List<Stage> stageList = getCyclingSteps();
                    CyclingStage cyclingStage = (CyclingStage) stageList.get(mRunningCyclingStageIndex);
                    tv_cycling.setText((cyclingNum + 1) + "/" + cyclingStage.getCyclingCount());
                }catch (Exception e){
                    AnitoaLogUtil.writeFileLog(e.getMessage(),mExecutorService);
                }


            }
        }
    }

    private Object mLock = new Object();
    private void autoInt(FactUpdater factUpdater) {
        synchronized (mLock) {

            AnitoaLogUtil.writeFileLog("===========开始熔解曲线自动积分==========", mExecutorService);
            ImageDataReader imageDataReader = new ImageDataReader(mCommunicationService,
                    mHistoryExperiment, factUpdater, mExecutorService, ExpeType.MELTING);
            imageDataReader.autoInt()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {

                            synchronized (mLock) {

                                AnitoaLogUtil.writeFileLog("===========结束熔解曲线自动积分==========", mExecutorService);
                                mLock.notifyAll();
                            }
                        }
                    });
            try {
                //等待自动积分完成
                mLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean mPcrFinished;
    private boolean mClosedPcrProgram;
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

        readTemperature();

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

                    tv_cycling_desc.setText(getString(R.string.running_heating));
                    tv_cycling.setText("");
                    break;
                case 2://CYCLING
                    onExpeCycling();

                    break;
                case 3://COOL DOWN
                    tv_cycling_desc.setText(getString(R.string.running_cooling));
                    tv_cycling.setText("");
                    mInCycling = false;
                    mPcrFinished=true;

                    mExpeFinished = false;
                    if (mHasMeltingCurve) {
                        if (!mMeltingCurveStarted && !mClosedPcrProgram) {
                            mClosedPcrProgram=true;
                            //熔解曲线自动积分中
                            LoadingDialogHelper.showOpLoading(getActivity());
                            //TODO 执行熔解曲线自动积分时间
                            mFactUpdater.SetInitData();
                            closeDeviceAndMeltingAutoInt()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean aBoolean) {
                                            LoadingDialogHelper.hideOpLoading();
                                            mCommunicationService.setNotify(ExpeRunningActivity.this);
                                            //开始熔解曲线参数设置
                                            step0();
                                            step1();
                                        }
                                    });

                        }
                    }
                    break;
                case 4:
                    tv_cycling_desc.setText("");
                    tv_cycling.setText("");
                    //熔解曲线预热阶段

                    tv_cur_mode.setText( getString(R.string.running_mode_melting));
                    mInCycling = false;
                    mInMeltCurve = true;
                    break;
                case 5:
                    tv_cur_mode.setText( getString(R.string.running_mode_melting));
                    tv_cycling_desc.setText("");
                    tv_cycling.setText("");
                    //熔解曲线阶段
                    mInCycling = false;
                    mInMeltCurve = true;
                    break;
            }
            if (status == 1 /*|| status == 3*/) {
               /* mHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        step5();
                    }
                },1000);
              */
                mStep5Responsed = false;
                if (step5Subscription == null) {
                    step5Subscription = Observable.interval(10, 2000,
                            TimeUnit.MILLISECONDS)
                            .onBackpressureLatest()
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
                                    AnitoaLogUtil.writeFileLog(throwable.getMessage());
                                }
                            });
                }


                return;
            }
            if (step5Subscription != null && !step5Subscription.isUnsubscribed()) {
                step5Subscription.unsubscribe();
                step5Subscription = null;
            }
            if (step6Subscription != null && !step6Subscription.isUnsubscribed()) {
                step6Subscription.unsubscribe();
                step6Subscription = null;
            }
            if (status == 0 || (status==3&& !mHasMeltingCurve)) {//实验已经结束
                if (!mBackPressed) {

                    LoadingDialogHelper.showOpLoading(getActivity());

                    closeDevice()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {

                            ToastUtil.showToast(getActivity(), getString(R.string.running_test_finished));
                            //顺便关闭实验

                            //TODO 自动跳转到实验数据分析页面

                            toAnalyzePage();

                            EventBus.getDefault().post(new ExpeNormalFinishEvent());
                            ActivityUtil.finish(getActivity());
                        }
                    });



                }

                return;
            }
            if(!mClosedPcrProgram) {
                checkHasNewParam(reveicedBytes);
            }
            if (mInCycling || mInMeltCurve) {
                //温度循环中，执行step6
                step6Subscription();
            }


        }

    }

    private void toAnalyzePage() {
        mHistoryExperiment.setDuring(tv_duration.getDuring());
        mHistoryExperiment.setFinishMilliTime(System.currentTimeMillis());

        //直接保存实验
        saveExpe(mHistoryExperiment).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BaseResponse>() {
                    @Override
                    public void call(BaseResponse response) {
                        if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                            EventBus.getDefault().post(new SavedExpeDataEvent());
                           // ToastUtil.showToast(getActivity(), "已保存到本地");
                            Tab tab = new Tab();
                            tab.setIndex(MainActivity.TAB_INDEX_DATA);
                            tab.setExtra(mHistoryExperiment);
                            MainActivity.start(getActivity(), tab);
                        } else {
                            String saveError=getString(R.string.setup_save_error);
                            ToastUtil.showToast(getActivity(), saveError);
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        String saveError=getString(R.string.setup_save_error);
                        ToastUtil.showToast(getActivity(), saveError);
                    }
                });

       /* Tab tab = new Tab();
        tab.setIndex(MainActivity.TAB_INDEX_DATA);
        tab.setExtra(mHistoryExperiment);
        MainActivity.start(getActivity(), tab);*/

    }


    private Observable<BaseResponse> saveExpe(HistoryExperiment experiment) {
        ExperimentStatus status = new ExperimentStatus();
        status.setStatus(ExperimentStatus.STATUS_COMPLETED);
        String finished=getString(R.string.test_status_finished);
        status.setDesc(finished);
        experiment.setStatus(status);
        //新插入一条数据
        experiment.setId(HistoryExperiment.ID_NONE);

        InsertExpeRequest request = new InsertExpeRequest();
        request.setExperiment(experiment);


        //TODO 还需保存到json文件中
        GenerateExpeJsonRequest jsonRequest=new GenerateExpeJsonRequest();
        jsonRequest.setExperiment(mHistoryExperiment);
        Observable<BaseResponse> jsonObservable=
                ExpeJsonGenerator.getInstance().generateExpeJson(jsonRequest);

        // 保存实验数据
        Observable<InsertExpeResponse> dbObservable=ExpeDataStore
                .getInstance(ProviderModule.getInstance().getBriteDb(getActivity().getApplicationContext()))
                .insertExpe(request);

        return Observable.merge(jsonObservable,dbObservable);

    }



    private void checkHasNewParam(byte[] reveicedBytes) {
        int dataIndex = 5;
        byte cfg = reveicedBytes[dataIndex + 1];
        int mode = ByteHelper.getHigh4(cfg);
        System.out.println("cmd_mode:" + mode);
        int preMode = ByteHelper.getLow4(cfg);
        System.out.println("预变性模式:" + preMode);
        int hasNewParam = reveicedBytes[dataIndex + 2];
        System.out.println("hasNewParam:" + hasNewParam);
        if (hasNewParam == 0) {
            //还没有设置新参数
            /*
             * 查看当前cyclingstage循环是否已经结束，
             *  a.如果当前还有没有执行的循环，继续下一个循环
             *  b.当前cyclingstage循环已经全部执行，查看是否还有一个cyclingstage，有的话继续
             */

            int cyclingSteps = getCyclingSteps().size();
            if (cyclingSteps > mCyclingStageIndex + 1) {
                //此时并没有执行新循环。
                mCurCycling = 1;
                mCyclingStageIndex++;
                System.out.println("mCyclingStageIndex:" + mCyclingStageIndex);
                step3(true);
            } else {
                /*
                 * TODO 没有新参数需要设置了，查看是否有设置溶解曲线模式
                 * a.没有就结束实验,跳转到数据分析页面
                 * b.设置了溶解曲线，开始该模式
                 */

               /* List<Mode> modeList = mHistoryExperiment.getSettingSecondInfo().getModes();
                if (modeList.size() > 1) {
                    if (!mMeltingCurveStarted) {
                        startMeltingCurveSync();
                    }
                }*/

            }
            // }

        } else if (hasNewParam == 1) {
            //有新参数在等待当前循环结束
        }
    }

    //  private PcrCommand.PCR_IMAGE mNextPcrImage;
    private void doReceiveStep6(Data data) {
        mStep6Responsed = true;
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
            List<Channel> channels = mHistoryExperiment.getSettingsFirstInfo().getChannels();
            //防止读出其他通道数据(设置了chip2第一条可能读出chip3，下位机bug)
            boolean channel0Selected = !TextUtils.isEmpty(channels.get(0).getValue());
            boolean channel1Selected = !TextUtils.isEmpty(channels.get(1).getValue());
            boolean channel2Selected = !TextUtils.isEmpty(channels.get(2).getValue());
            boolean channel3Selected = !TextUtils.isEmpty(channels.get(3).getValue());
            ChannelImageStatus channel0ImageStatus = new ChannelImageStatus(0, mImageMode.getSize());
            channel0ImageStatus.setReadable(channel0 == 1 && channel0Selected);
            mChannelStatusList.add(channel0ImageStatus);

            ChannelImageStatus channel1ImageStatus = new ChannelImageStatus(1, mImageMode.getSize());
            channel1ImageStatus.setReadable(channel1 == 1 && channel1Selected);
            mChannelStatusList.add(channel1ImageStatus);

            ChannelImageStatus channel2ImageStatus = new ChannelImageStatus(2, mImageMode.getSize());
            channel2ImageStatus.setReadable(channel2 == 1 && channel2Selected);
            mChannelStatusList.add(channel2ImageStatus);

            ChannelImageStatus channel3ImageStatus = new ChannelImageStatus(3, mImageMode.getSize());
            channel3ImageStatus.setReadable(channel3 == 1 && channel3Selected);
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

                //图像板还未准备好,应该询问循环状态
                step5Subscription();

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

    private boolean mStep6Responsed;
    private Subscription step6Subscription;

    private void step6Subscription() {
        if (step6Subscription == null) {
            mStep6Responsed = false;
            step6Subscription = Observable.interval(10, 2000, TimeUnit.MILLISECONDS)
                    /* .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())*/
                    .onBackpressureLatest()
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            if (!mStep6Responsed) {
                                readTemperature();
                                step6();
                            }

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            AnitoaLogUtil.writeFileLog(throwable.getMessage());
                        }
                    });
        }
    }

    Subscription step4Subscription;
    boolean mStep4Responsed;

    private void step4Subscription() {
        if (step4Subscription == null) {
            mStep4Responsed = false;
            step4Subscription = Observable.interval(10, 1000, TimeUnit.MILLISECONDS)
                    .onBackpressureLatest()
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            if (!mStep4Responsed) {
                                startCycling();
                            }

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            // AnitoaLogUtil.writeFileLog(throwable.getMessage());
                        }
                    });
        }
    }

    private void step5Subscription(long period) {
        if (step5Subscription == null) {
            mStep5Responsed = false;
            step5Subscription = Observable.interval(10, period, TimeUnit.MILLISECONDS)
                    /* .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())*/
                    .onBackpressureLatest()
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
                            AnitoaLogUtil.writeFileLog(throwable.getMessage());
                        }
                    });
        }
    }

    private void step5Subscription() {
        step5Subscription(2000);
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
    //图像板返回的单次数据（包含chip1-4）
    private Map<String, List<String>> mItemData = new LinkedHashMap<>();

    /**
     * 读取PCR数据
     * <p>
     * private void readPcrData() {
     * PcrCommand command = new PcrCommand();
     * command.step6(mNextPcrImage);
     * mBluetoothService.write(command);
     * }
     */

    private synchronized void doReceiveStep7(Data data) {
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
       /* System.out.println("curRowIndex:" + curRowIndex);
        if (curRowIndex<0|| curRowIndex>11){
            String error="========= errorCurRowIndex:"+curRowIndex+" =========";
            AnitoaLogUtil.writeFileLog(error);
        }*/
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

        /*StringBuilder hex = new StringBuilder(reveicedBytes.length * 2);
        for (byte b : reveicedBytes) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }*/


        File file = null, sourceFile = null;
        if (mInCycling) {

            //System.out.println("通道" + channelIndex + "图像数据第" + curRowIndex);
            file = DataFileUtil.getDtImageDataFile(mHistoryExperiment);
            // sourceFile = AnitoaLogUtil.getDtImageDataSourceFile(mHistoryExperiment);
        } else if (mInMeltCurve) {
            //System.out.println("熔解曲线通道" + channelIndex + "图像数据第" + curRowIndex);
            file = DataFileUtil.getMeltImageDateFile(mHistoryExperiment);
            // sourceFile = AnitoaLogUtil.getMeltImageDataSourceFile(mHistoryExperiment);
        }
        //保存到本地文件中
        if (curRowIndex == 0) {
            //appendToFile(chip, file);
            // sourceAppendToFile(chip, sourceFile);
        }
        //保存图像板返回的原始数据到文件
        //将0x1717后面的数据抹去
      /*  String source=hex.toString().toLowerCase();
        int index=source.indexOf("1717");
        source=source.substring(0,index+4);
        sourceAppendToFile(source, sourceFile);*/
        // if (mInCycling) {
        //TODO 循环过程数据转换待理解
        String imageData = transferImageData(channelIndex, curRowIndex, reveicedBytes);
        //System.out.println("trim后的图像数据："+imageData);
        //保存经过矫正图像板数据到文件
        //appendToFile(imageData, file);
        if (mItemData.get(chip) == null) {
            mItemData.put(chip, new ArrayList<String>());
        }
        mItemData.get(chip).add(imageData);

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
            if (next == -1) {
                //全部通道已经全部读取完
                //将数据保存到文件中
                List<Channel> channels = mHistoryExperiment.getSettingsFirstInfo().getChannels();
                boolean channel0Selected = !TextUtils.isEmpty(channels.get(0).getValue());
                boolean channel1Selected = !TextUtils.isEmpty(channels.get(1).getValue());
                boolean channel2Selected = !TextUtils.isEmpty(channels.get(2).getValue());
                boolean channel3Selected = !TextUtils.isEmpty(channels.get(3).getValue());
                boolean needSave = true;
                if (channel0Selected) {
                    List<String> list = mItemData.get("Chip#1");
                    if (list == null || list.size() != 12) {
                        needSave = false;
                    }
                }
                if (channel1Selected) {
                    List<String> list = mItemData.get("Chip#2");
                    if (list == null || list.size() != 12) {
                        needSave = false;
                    }
                }
                if (channel2Selected) {
                    List<String> list = mItemData.get("Chip#3");
                    if (list == null || list.size() != 12) {
                        needSave = false;
                    }
                }
                if (channel3Selected) {
                    List<String> list = mItemData.get("Chip#4");
                    if (list == null || list.size() != 12) {
                        needSave = false;
                    }
                }
                if (needSave) {
                    Set<Map.Entry<String, List<String>>> entries = mItemData.entrySet();
                    for (Map.Entry<String, List<String>> entry : entries) {
                        String key = entry.getKey();
                        List<String> value = entry.getValue();
                        StringBuilder sBuilder = new StringBuilder();
                        sBuilder.append(key)
                                .append(C.Char.NEW_LINE);
                        for (String item : value) {
                            sBuilder.append(item)
                                    .append(C.Char.NEW_LINE);
                        }
                        //System.out.println("图像内容："+ sBuilder.toString());
                        AnitoaLogUtil.writeToFile(file, sBuilder.toString());
                    }
                }
                mItemData.clear();
                // 绘制图形
                showChart();
                /**
                 * 读循环状态
                 */
                if (step5Subscription != null && !step5Subscription.isUnsubscribed()) {
                    step5Subscription.unsubscribe();
                    step5Subscription = null;
                }
                //step5Subscription(500);

                step5();
              /*  if (step5Subscription != null && !step5Subscription.isUnsubscribed()) {
                    step5Subscription.unsubscribe();
                    step5Subscription=null;
                }
                step5Subscription();*/
            } else {
                step7(mChannelStatusList.get(next).getPctImageCmd());
            }
        }


    }

    private String transferImageData(int chan, int k, byte[] reveicedBytes) {
        int count;
        if (mInCycling) {
            count = mImageMode.getSize() + 1;
        } else {
            count = mImageMode.getSize() + 5;
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
        if (mInMeltCurve) {
            if (reveicedBytes[5] == 0) {
                byte[] buffers = new byte[4];
                buffers[0] = reveicedBytes[CommData.imgFrame * 2 + 6];
                buffers[1] = reveicedBytes[CommData.imgFrame * 2 + 7];
                buffers[2] = reveicedBytes[CommData.imgFrame * 2 + 8];
                buffers[3] = reveicedBytes[CommData.imgFrame * 2 + 9];
                //float t = BitConverter.ToSingle(buffers, 0);
                //float t = ByteUtil.getFloat(buffers);
                //txData[count - 1] = (int) t;

                txData[count - 4] = buffers[0];
                txData[count - 3] = buffers[1];
                txData[count - 2] = buffers[2];
                txData[count - 1] = buffers[3];
            }
        }
        String res = "";
        //   String newres = "";
        for (int i = 0; i < txData.length; i++) {
            if (i == 0) {
                res = txData[i] + "";
                //  newres = txData[i] + "";
            } else {
                if (i != 11 && i != 23) {
                    res += " " + txData[i];
                    //   newres += "    " + txData[i];
                } else {
                    if (k == 11 || k == 23) {
                        res += " " + mFactUpdater.GetFactValueByXS(chan);
                        //   newres += "    " + mFactUpdater.GetFactValueByXS(chan);
                    } else {
                        res += " " + txData[i];
                        //  newres += "    " + txData[i];
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
        System.out.println("熔解曲线温度startT：" + startT + " endT:" + endT);
        AnitoaLogUtil.writeFileLog("熔解曲线温度startT：" + startT + " endT:" + endT);
        mCommunicationService.sendPcrCommandSync(command);
        //mUsbService.sendPcrCommandSync(command);

        // delayAskTriggerStatus();
    }

    private Observable<Boolean> startMeltingCurve() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try{
                    startMeltingCurveSync();
                    subscriber.onNext(true);
                }catch (Exception e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }

            }
        });
    }

    private void stopMeltingCurve() {
        PcrCommand command = new PcrCommand();
        float startT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getStartTemperature());
        float endT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getEndTemperature());
        float speed = 1;
        command.stopMelting(startT, endT, speed);
        doStopMelting(command);

    }

    public void doStopMelting(PcrCommand command) {
        byte[] bytes = mCommunicationService.sendPcrCommandSync(command);
        while (bytes == null) {
            mStopMeltingRunNum++;
            bytes = mCommunicationService.sendPcrCommandSync(command);
            if (mStopMeltingRunNum > 5) {
                return;
            }
        }
        int cmd = bytes[2];
        int type = bytes[4];
        if (cmd == 0x13 && type == 0xb) {
            //结束成功
        } else {
            doStopMelting(command);
        }
    }

    private void stopCycling() {
        System.out.println("stopCycling");
        PcrCommand command = new PcrCommand();
        List<Stage> cyclingSteps = mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
        CyclingStage cyclingStage = (CyclingStage) cyclingSteps.get(mCyclingStageIndex);
        int cyclingCount = cyclingStage.getCyclingCount();
        List<Stage> stageList = mHistoryExperiment.getSettingSecondInfo().getSteps();
        StartStage startStage = (StartStage) stageList.get(0);

        EndStage endStage;
        if (!mHasMeltingCurve) {
            endStage = (EndStage) stageList.get(stageList.size() - 1);
        }else {
            endStage = (EndStage) stageList.get(stageList.size() - 1-2);
        }
      //  EndStage endStage = (EndStage) stageList.get(stageList.size() - 1);

        List<PcrCommand.TempDuringCombine> predenaturationCombines = new ArrayList<>();
        PcrCommand.TempDuringCombine predenaturationCombine = new PcrCommand.TempDuringCombine(startStage.getTemp(), startStage.getDuring());
        PcrCommand.TempDuringCombine extendCombine = new PcrCommand.TempDuringCombine(endStage.getTemp(), endStage.getDuring());

        predenaturationCombines.add(predenaturationCombine);
        if (mHasTwoStartStage) {
            StartStage startStage2 = (StartStage) stageList.get(1);
            PcrCommand.TempDuringCombine predenaturationCombine2 = new PcrCommand.TempDuringCombine(startStage2.getTemp(), startStage2.getDuring());
            predenaturationCombines.add(predenaturationCombine2);
        }

        PcrCommand.CmdMode cmdMode = PcrCommand.CmdMode.NORMAL;
        if (cyclingSteps.size() > 1) {
            cmdMode = PcrCommand.CmdMode.CONTINU;
        }
        command.stopCycling(cyclingCount, cmdMode, predenaturationCombines, extendCombine);
        if (mCommunicationService instanceof UsbService) {
            mCommunicationService.sendPcrCommand(command);
        } else {
            doStopCycling(command);
        }


    }

    private int mStopCyclingRunNum;
    private int mStopMeltingRunNum;

    public void doStopCycling(PcrCommand command) {

        byte[] bytes = mCommunicationService.sendPcrCommandSync(command);
        while (bytes == null) {
            mStopCyclingRunNum++;
            bytes = mCommunicationService.sendPcrCommandSync(command);
            if (mStopCyclingRunNum > 5) {
                return;
            }
        }
        int cmd = bytes[2];
        int type = bytes[4];
        if (cmd == 0x13 && type == 0x4) {
            //结束成功
            return;
        } else {
            doStopCycling(command);
        }
    }

    private List<Stage> getCyclingSteps() {
        return mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
    }
}
