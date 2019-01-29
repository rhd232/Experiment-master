package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
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
import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.module.bluetooth.BluetoothReceiver;
import com.jz.experiment.module.bluetooth.BluetoothService;
import com.jz.experiment.module.bluetooth.Data;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.module.bluetooth.ble.BluetoothConnectionListener;
import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;
import com.jz.experiment.module.expe.bean.ChannelImageStatus;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.ByteHelper;
import com.jz.experiment.util.DeviceProxyHelper;
import com.jz.experiment.util.StatusChecker;
import com.jz.experiment.widget.ChartMarkerView;
import com.jz.experiment.widget.DuringView;
import com.wind.base.BaseActivity;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Navigator;
import com.wind.data.expe.bean.ChartData;
import com.wind.data.expe.bean.ColorfulEntry;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Mode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action1;

public class ExpeRunningActivityBackup extends BaseActivity implements BluetoothConnectionListener {
    public static final int WHAT_REFRESH_CHART = 1;

    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, ExpeRunningActivityBackup.class, experiment);
    }

    @BindView(R.id.chart)
    LineChart chart;
    ChartMarkerView mChartMarkerView;
    LineData mLineData;
    ArrayList<ILineDataSet> mDataSets;

    @BindView(R.id.tv_cur_mode)
    TextView tv_cur_mode;
    @BindView(R.id.tv_duration)
    DuringView tv_duration;

    @BindView(R.id.ll_cycling)
    View ll_cycling;
    @BindView(R.id.tv_cycling)
    TextView tv_cycling;

    private List<Integer> mLineColors;
    private HistoryExperiment mHistoryExperiment;
    BluetoothReceiver mBluetoothReceiver;
    BluetoothService mBluetoothService;
    @BindView(R.id.tv_received_msg)
    TextView tv_received_msg;
    /**
     * 当前图像格式
     */
    private PcrCommand.IMAGE_MODE mImageMode;

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("运行");
        mTitleBar.setRightText("筛选");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expe_running);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        mImageMode = PcrCommand.IMAGE_MODE.IMAGE_12;
        mHistoryExperiment = Navigator.getParcelableExtra(this);

        tv_cur_mode.setText("当前模式：变温扩增");
        //启动计时器
        tv_duration.start();

        initChart();

        bindService();



       Thread thread = new Thread(mRun);
        thread.start();
    }

    private void bindService() {
      /*  Intent serviceIntent = new Intent(getActivity(), BluetoothService.class);
        getActivity().bindService(serviceIntent, mBluetoothServiceConnection, BIND_AUTO_CREATE);*/
        mBluetoothService = DeviceProxyHelper.getInstance(getActivity()).getBluetoothService();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ActivityUtil.isFinish(getActivity())) {
                    //TODO 给设备发指令 step1-
                    PcrCommand cmd = new PcrCommand();
                    int[] channelOp = {1, 1, 1, 1};//
                    cmd.step1(channelOp);
                    mBluetoothService.write(cmd);
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


  /*  private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.initialize();

            //TODO 给设备发指令 step1-
            PcrCommand cmd = new PcrCommand();
            cmd.step1(0);
            mBluetoothService.write(cmd);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };*/

    private void initChart() {
        mDataSets = new ArrayList<>();
        mLineColors = new ArrayList<>();


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

        mLineData = new LineData(mDataSets);

        chart.setMarker(mChartMarkerView = new ChartMarkerView(getActivity(), new ChartMarkerView.OnPointSelectedListener() {
            @Override
            public void onPointSelected(Entry e) {

                int index = -1;
                for (int i = 0; i < mDataSets.size(); i++) {
                    LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                    index = lineDataSet.getEntryIndex(e);
                    if (index != -1) {
                        break;
                    }
                }
                if (index == -1) {
                    return;
                }
                List<ColorfulEntry> entries = new ArrayList<>();
                for (int i = 0; i < mDataSets.size(); i++) {
                    LineDataSet lineDataSet = (LineDataSet) mDataSets.get(i);
                    Entry entry = lineDataSet.getEntryForIndex(index);
                    ColorfulEntry colorfulEntry = new ColorfulEntry();
                    colorfulEntry.setEntry(entry);
                    colorfulEntry.setColor(mLineColors.get(i));
                    entries.add(colorfulEntry);

                }

                mChartMarkerView.getAdapter().replaceAll(entries);

            }
        }));
        chart.setTouchEnabled(false);
        chart.setDrawBorders(false);
        chart.setData(mLineData);
        chart.invalidate(); // refresh
    }

    private int i = 10;
    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            for (int i = 1; i <= 1; i++) {//4组数据

                List<Entry> expeData = new ArrayList<>();

                Entry entry1 = new Entry(0, 513.07654f);
                Entry entry2 = new Entry(1, 1026.0233f);
                Entry entry3 = new Entry(2, 1.3509406f);
                Entry entry4 = new Entry(3, 1.2815096f);
                Entry entry5 = new Entry(4, -5.757593f);
                Entry entry6 = new Entry(5, -2.4374743f);
                Entry entry7 = new Entry(6, 5.735752f);
                Entry entry8 = new Entry(7, 7.3161335f);
                Entry entry9 = new Entry(8, 6.3230915f);
                Entry entry10 = new Entry(9, 15.161514f);
                Entry entry11= new Entry(10, 17.563675f);
                Entry entry12 = new Entry(11, 24.937712f);
                Entry entry13= new Entry(12, 37.510605f);
                Entry entry14 = new Entry(13, 57.656525f);
                Entry entry15 = new Entry(14, 78.551384f);
                Entry entry16 = new Entry(15, 132.91096f);
                Entry entry17 = new Entry(16, 187.66565f);
                Entry entry18 = new Entry(17, 277.10416f);
                Entry entry19 = new Entry(18, 420.12866f);
                Entry entry20 = new Entry(19, 618.17224f);
                Entry entry21 = new Entry(20, 895.62524f);
                Entry entry22 = new Entry(21, 1272.7334f);
                Entry entry23 = new Entry(22, 1705.8959f);
                Entry entry24 = new Entry(23, 2199.485f);
                Entry entry25 = new Entry(24, 2689.909f);
                Entry entry26 = new Entry(25, 3176.8867f);
                Entry entry27 = new Entry(26, 3611.0205f);
                Entry entry28 = new Entry(27, 3975.5513f);
                Entry entry29 = new Entry(28, 4248.9106f);
                Entry entry30 = new Entry(29, 4484.503f);
                Entry entry31 = new Entry(30, 4631.2974f);
                Entry entry32 = new Entry(31, 4740.372f);
                Entry entry33 = new Entry(32, 4828.014f);
                Entry entry34 = new Entry(33, 4898.9326f);
                Entry entry35 = new Entry(34, 4932.4243f);
                Entry entry36 = new Entry(35, 4974.2417f);
                Entry entry37 = new Entry(36, 4987.021f);
                Entry entry38 = new Entry(37, 5006.285f);
                Entry entry39 = new Entry(38, 5014.3647f);
                Entry entry40 = new Entry(39, 5056.863f);
                Entry entry41 = new Entry(40, 5035.4336f);
                Entry entry42 = new Entry(41, 5052.5146f);
                Entry entry43 = new Entry(42, 5047.646f);
                Entry entry44 = new Entry(43, 5038.6465f);
                Entry entry45 = new Entry(44, 5044.394f);
                Entry entry46 = new Entry(45, 5039.099f);
                Entry entry47 = new Entry(46, 5056.568f);
                Entry entry48 = new Entry(47, 5051.838f);
                Entry entry49 = new Entry(48, 5055.3086f);
                Entry entry50 = new Entry(49, 5062.1226f);
                Entry entry51 = new Entry(50, 0.0f);

                expeData.add(entry1);
                expeData.add(entry2);
                expeData.add(entry3);
                expeData.add(entry4);
                expeData.add(entry5);
                expeData.add(entry6);
                expeData.add(entry7);
                expeData.add(entry8);
                expeData.add(entry9);
                expeData.add(entry10);
                expeData.add(entry11);
                expeData.add(entry12);
                expeData.add(entry13);
                expeData.add(entry14);
                expeData.add(entry15);
                expeData.add(entry16);
                expeData.add(entry17);
                expeData.add(entry18);
                expeData.add(entry19);
                expeData.add(entry20);
                expeData.add(entry21);
                expeData.add(entry22);
                expeData.add(entry23);
                expeData.add(entry24);
                expeData.add(entry25);
                expeData.add(entry26);
                expeData.add(entry27);
                expeData.add(entry28);
                expeData.add(entry29);
                expeData.add(entry30);
                expeData.add(entry31);
                expeData.add(entry32);
                expeData.add(entry33);
                expeData.add(entry34);
                expeData.add(entry35);
                expeData.add(entry36);
                expeData.add(entry37);
                expeData.add(entry38);
                expeData.add(entry39);
                expeData.add(entry40);
                expeData.add(entry41);
                expeData.add(entry42);
                expeData.add(entry43);
                expeData.add(entry44);
                expeData.add(entry45);
                expeData.add(entry46);
                expeData.add(entry47);
                expeData.add(entry48);
                expeData.add(entry49);
                expeData.add(entry50);
                expeData.add(entry51);
                LineDataSet dataSet = new LineDataSet(expeData, "通道" + i);
                int color = -1;
                switch (i) {
                    case 1:
                        color = Color.parseColor("#355ABB");
                        dataSet.setColor(color);
                        break;
                    case 2:
                        dataSet.setColor(color = Color.parseColor("#1F994A"));
                        break;
                    case 3:
                        dataSet.setColor(color = Color.parseColor("#DBAE11"));
                        break;
                    case 4:
                        dataSet.setColor(color = Color.parseColor("#F13B3B"));
                        break;
                }
                dataSet.setDrawCircles(false);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setDrawValues(false);
                mLineColors.add(color);
                mDataSets.add(dataSet);
            }
            mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
           /* while (i < 100) {
                i++;
                for (int j = 1; j <= 4; j++) {
                    float y = 0;
                    if (j == 1) {
                        y = i;
                    } else if (j == 2) {
                        y = i * 10;
                    } else if (j == 3) {
                        y = (float) Math.pow(i, 2);
                    } else if (j == 4) {
                        y = i * 20;
                    }
                    Entry entry = new Entry(i, y);
                    mDataSets.get(j - 1).addEntry(entry);
                }

                mHandler.sendEmptyMessage(WHAT_REFRESH_CHART);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

        }
    };


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mLineData.notifyDataChanged();
            chart.notifyDataSetChanged(); // let the chart know it's data changed
            chart.invalidate(); // refresh
        }
    };


    @OnClick({R.id.tv_stop})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_stop:
                //下一步

                mHistoryExperiment.setDuring(tv_duration.getDuring());
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
                MainActivity.start(getActivity(), tab);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        try {
            String msg = new String(reveicedBytes, "ISO-8859-1");
            tv_received_msg.append(msg);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int type = hexToDecimal(reveicedBytes[typeIndex]);

        switch (type) {
            case PcrCommand.STEP_1_TYPE:
                //执行step2
                PcrCommand command = new PcrCommand();
                float temp = 105;
                short during = 0;
                command.step2(temp, during);
                mBluetoothService.write(command);
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
            mCurCycling = 1;

            step3();

        }
    }

    /**
     * 设置循环参数
     */
    private void step3() {
        //执行step3
        PcrCommand command = new PcrCommand();
        CyclingStage cyclingStage = (CyclingStage) getCyclingSteps().get(mCyclingStageIndex);
        int cyclingCount = cyclingStage.getCyclingCount();
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


        //command.step3(cyclingCount, mCurCycling, picIndex, cyclingStage.getPartStageList().size(), combines);
        int rsvd=0;
        command.step3(rsvd,picIndex, cyclingStage.getPartStageList().size(), combines);
        mBluetoothService.write(command);
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
                } else {
                    delayAskTriggerStatus();
                }

                break;
            case PcrCommand.STEP_4_TYPE:
                //启动循环返回，设置timer定时查询sensor是否已准备好图像数据
                delayAskTriggerStatus();
                /* mTimerSubscription=Observable
                        .interval(100,300,TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {

                    }
                });*/
                break;
        }
    }

    /**
     * 询问图像板是否准备好数据
     */
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
    }

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
        command.step4(PcrCommand.Control.START, cyclingCount, PcrCommand.CmdMode.CONTINU,
                predenaturationCombine, extendCombine);

        mBluetoothService.write(command);


    }

    private List<ChannelImageStatus> mChannelStatusList;

    private void doReceiveStep5(Data data) {
        mChannelStatusList = new ArrayList<>();
        int typeIndex = 4;
        int dataIndex = 5;
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        if (type == PcrCommand.STEP_5_TYPE) {
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
            if (readableIndex==-1){
                //图像板还未准备好,继续询问
                delayAskTriggerStatus();
            }else {
                //读取通道图像数据
                readPcrImageData(mChannelStatusList.get(readableIndex).getPctImageCmd());
            }

        }
    }

    //  private PcrCommand.PCR_IMAGE mNextPcrImage;

    private void readPcrImageData(PcrCommand.PCR_IMAGE pcr_image) {
        PcrCommand command = new PcrCommand();
      //  command.step6(pcr_image);
        mBluetoothService.write(command);
    }

    /**
     * 读取PCR数据
     * <p>
     * private void readPcrData() {
     * PcrCommand command = new PcrCommand();
     * command.step6(mNextPcrImage);
     * mBluetoothService.write(command);
     * }
     */

    private void doReceiveStep6(Data data) {
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

        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        int length = hexToDecimal(reveicedBytes[lengthIndex]);
        int total =reveicedBytes[dataStartIndex++];
        int curRowIndex =reveicedBytes[dataStartIndex++];//从0开始
        byte pixs[]=new byte[mImageMode.getSize()*2];//每个图像像素有2个字节组成
        for (int i=0;i<mImageMode.getSize()*2;i++){
            pixs[i]=reveicedBytes[dataStartIndex++];
        }

        if (mInCycling) {
            boolean readed=false;//本通道数据是否已经读完
            //处于温度循环状态
            if (type==PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_0.getValue()){
                //通道1图像数据返回
                mChannelStatusList.get(0).setCurReadRow((curRowIndex+1));//
                readed=mChannelStatusList.get(0).readed();
            }else if (type==PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_1.getValue()){
                //通道2图像数据返回
                mChannelStatusList.get(1).setCurReadRow((curRowIndex+1));//
                readed=mChannelStatusList.get(1).readed();
            }else if (type==PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_2.getValue()){
                //通道3图像数据返回
                mChannelStatusList.get(2).setCurReadRow((curRowIndex+1));//
                readed=mChannelStatusList.get(2).readed();
            }else if (type==PcrCommand.PCR_IMAGE.PCR_12_CHANNEL_3.getValue()){
                //通道4图像数据返回
                mChannelStatusList.get(3).setCurReadRow((curRowIndex+1));//
                readed=mChannelStatusList.get(3).readed();
            }
            //检查本通道是否已经读取完毕
            if(readed){
                //执行下一个通道的读取操作
                int next=-1;
                for (int i=0;i<mChannelStatusList.size();i++){
                    ChannelImageStatus status=mChannelStatusList.get(i);
                    if (status.isReadable()&& !status.readed()){
                        next=i;
                        break;
                    }
                }
                if (next==-1){//全部通道已经全部读取完
                    /*
                     * 一个循环图像及数据显示处理完毕,询问是否继续polling图像板
                     */
                    askIfContinuePolling();
                }else {
                    readPcrImageData(mChannelStatusList.get(next).getPctImageCmd());
                }
            }
        } else {
            //处于溶解曲线中
        }

        /*if (mNextPcrImage != PcrCommand.PCR_IMAGE.PCR4) {
            //继续获取图像数据
            mNextPcrImage = mNextPcrImage.getNext();
            readPcrData();
        } else {
            *//*
             * 一个循环图像及数据显示处理完毕,询问是否继续polling图像板
             *//*
            askIfContinuePolling();

        }*/
    }

    /*
     * 一个循环图像及数据显示处理完毕,询问是否继续polling图像板
     */
    private void askIfContinuePolling() {
        PcrCommand command = new PcrCommand();
        //command.step7();
        mBluetoothService.write(command);
    }

    /**
     * 是处于温度循环中还是溶解曲线中
     */
    private boolean mInCycling = true;

    private void doReceiveStep7(Data data) {
        int typeIndex = 4;
        int dataIndex = 5;
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
       // if (type == PcrCommand.STEP_7_TYPE) {
            //当前循环状态
            byte status = reveicedBytes[dataIndex];
            switch (status) {
                case 0://IDLE
                    mInCycling = false;
                    break;
                case 1://LID HEAT
                    mInCycling = false;
                    break;
                case 2://CYCLING
                    mInCycling = true;
                    break;
                case 3://COOL DOWN
                    mInCycling = false;
                    break;
            }

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

           //     CyclingStage cyclingStage = (CyclingStage) getCyclingSteps().get(mCyclingStageIndex);
              /*  boolean curCyclingOn = cyclingStage.getCyclingCount() > mCurCycling;
                if (curCyclingOn) {
                    mCurCycling++;
                    step3();
                } else {*/
                    mCyclingStageIndex++;
                    CyclingStage nextCyclingStage = (CyclingStage) getCyclingSteps().get(mCyclingStageIndex);
                    if (nextCyclingStage != null) {
                        mCurCycling = 1;
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
                                startMeltingCurve();
                            }
                        } else {

                        }

                    }
               // }

            } else if (hasNewParam == 1) {
                //有新参数在等待当前循环结束
            }
       // }


    }

    /**
     * 溶解曲线是否已经启动
     */
    private boolean mMeltingCurveStarted;

    private void startMeltingCurve() {
        PcrCommand command = new PcrCommand();
        float startT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getStartTemperature());
        float endT = Float.parseFloat(mHistoryExperiment.getSettingSecondInfo().getEndTemperature());
        float speed = 1;
        command.meltingCurve(PcrCommand.Control.START, startT, endT, speed);

        mBluetoothService.write(command);

        delayAskTriggerStatus();
    }

    private List<Stage> getCyclingSteps() {
        return mHistoryExperiment.getSettingSecondInfo().getCyclingSteps();
    }
}
