package com.jz.experiment.module.expe.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.util.ByteHelper;
import com.jz.experiment.widget.ChartMarkerView;
import com.jz.experiment.widget.DuringView;
import com.wind.base.BaseActivity;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.PartStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
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

public class ExpeRunningActivity extends BaseActivity implements BluetoothConnectionListener {
    public static final int WHAT_REFRESH_CHART = 1;

    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, ExpeRunningActivity.class, experiment);
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

    @BindView(R.id.tv_received_msg)
    TextView tv_received_msg;
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
        mHistoryExperiment = Navigator.getParcelableExtra(this);

        tv_cur_mode.setText("当前模式：变温扩增");
        //启动计时器
        tv_duration.start();

        initChart();

        bindService();



        /*Thread thread = new Thread(mRun);
        thread.start();*/
    }

    private void bindService() {
        Intent serviceIntent = new Intent(getActivity(), BluetoothService.class);
        getActivity().bindService(serviceIntent, mBluetoothServiceConnection, BIND_AUTO_CREATE);

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

    BluetoothService mBluetoothService;
    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
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
    };

    private void initChart() {
        mDataSets = new ArrayList<>();
        mLineColors = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {//4组数据

            List<Entry> expeData = new ArrayList<>();

            /*for (int j = 0; j < 10; j++) {
                float y = 0;

                if (i == 1) {
                    y = j;
                } else if (i == 2) {
                    y = j * 10;
                } else if (i == 3) {
                    y = (float) Math.pow(j, 2);
                } else if (i == 4) {
                    y = j * 20;
                }
                Entry entry = new Entry(j, y);
                expeData.add(entry);
            }*/

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
            while (i < 100) {
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
            }

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
        return Integer.parseInt(value + "", 16);

    }

    private void doReceiveStep1(Data data) {
        int headIndex = 0;
        int ackIndex = 1;
        int cmdIndex = 2;
        int lengthIndex = 3;
        int typeIndex = 4;

        byte[] reveicedBytes = data.getBuffer();
        try {
            String msg=new String(reveicedBytes,"ISO-8859-1");
            tv_received_msg.append(msg);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int type = hexToDecimal(reveicedBytes[typeIndex]);

        switch (type) {
            case PcrCommand.STEP_1_TYPE:
                //执行step2
                PcrCommand command = new PcrCommand();
                float temp = 0;
                short during = 10;
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


        command.step3(cyclingCount, mCurCycling, picIndex, cyclingStage.getPartStageList().size(), combines);
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

    private void doReceiveStep5(Data data) {
        int typeIndex = 4;
        int dataIndex = 5;
        byte[] reveicedBytes = data.getBuffer();
        int type = hexToDecimal(reveicedBytes[typeIndex]);
        if (type == PcrCommand.STEP_5_TYPE) {
            int status = hexToDecimal(reveicedBytes[dataIndex]);
            if (status > 0) {
                mNextPcrImage = PcrCommand.PCR_IMAGE.PCR1;
                //图像板已经准备好数据
                readPcrData();
            } else {
                //图像板还未准备好,继续询问
                delayAskTriggerStatus();

            }

        }
    }

    private PcrCommand.PCR_IMAGE mNextPcrImage;

    /**
     * 读取PCR数据
     */
    private void readPcrData() {
        PcrCommand command = new PcrCommand();
        command.step6(mNextPcrImage);
        mBluetoothService.write(command);
    }

    private void doReceiveStep6(Data data) {
        /*
         *  TODO 1，解析图像数据，绘制到图表中，需要判断是扩增阶段，还是溶解阶段。
         *       2，如果没有读取到最后一行，就继续读取
         *
         *       下位机在进入溶解曲线功能后，回复的图像时，第一行图像数据包的最后，会增加温度信息 （float， 4byte）
         *       上位机通过polling下位机温度循环状态，可判断下位机是在温度循环中还是溶解曲线中，据此判断图像数据报中是否包含温度信息。
         */

        if (mInCycling) {
            //处于温度循环状态
        } else {
             //处于溶解曲线中
        }
        if (mNextPcrImage != PcrCommand.PCR_IMAGE.PCR4) {
            //继续获取图像数据
            mNextPcrImage = mNextPcrImage.getNext();
            readPcrData();
        } else {
            /*
             * 一个循环图像及数据显示处理完毕,询问是否继续polling图像板
             */
            askIfContinuePolling();

        }
    }

    /*
     * 一个循环图像及数据显示处理完毕,询问是否继续polling图像板
     */
    private void askIfContinuePolling() {
        PcrCommand command = new PcrCommand();
        command.step7();
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
        if (type == PcrCommand.STEP_7_TYPE) {
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

                CyclingStage cyclingStage = (CyclingStage) getCyclingSteps().get(mCyclingStageIndex);
                boolean curCyclingOn = cyclingStage.getCyclingCount() > mCurCycling;
                if (curCyclingOn) {
                    mCurCycling++;
                    step3();
                } else {
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
                }

            } else if (hasNewParam == 1) {
                //有新参数在等待当前循环结束
            }
        }


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
