package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aigestudio.wheelpicker.utils.WheelPickerFactory;
import com.aigestudio.wheelpicker.widget.IWheelVo;
import com.aigestudio.wheelpicker.widget.WheelSimpleVo;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.FactUpdater;
import com.jz.experiment.chart.FlashData;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.bluetooth.CommunicationService;
import com.jz.experiment.module.bluetooth.Data;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.module.bluetooth.ble.BluetoothConnectionListener;
import com.jz.experiment.module.expe.adapter.StageAdapter;
import com.jz.experiment.module.expe.event.AddCyclingStageEvent;
import com.jz.experiment.module.expe.event.AddStartStageEvent;
import com.jz.experiment.module.expe.event.DelCyclingStageEvent;
import com.jz.experiment.module.expe.event.DelStartStageEvent;
import com.jz.experiment.module.expe.event.ExpeNormalFinishEvent;
import com.jz.experiment.module.expe.event.RefreshStageAdapterEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.ByteUtil;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.DeviceProxyHelper;
import com.jz.experiment.util.StatusChecker;
import com.jz.experiment.util.TrimReader;
import com.wind.base.BaseActivity;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.EndStage;
import com.wind.base.bean.MeltingStage;
import com.wind.base.bean.Stage;
import com.wind.base.bean.StartStage;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.response.BaseResponse;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.LogUtil;
import com.wind.base.utils.Navigator;
import com.wind.data.DbOpenHelper;
import com.wind.data.expe.bean.DtMode;
import com.wind.data.expe.bean.ExpeSettingSecondInfo;
import com.wind.data.expe.bean.ExperimentStatus;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.bean.Mode;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.request.InsertExpeRequest;
import com.wind.data.expe.response.InsertExpeResponse;
import com.wind.toastlib.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

import static com.jz.experiment.util.ThreadUtil.sleep;

public class UserSettingsStep2Activity extends BaseActivity implements BluetoothConnectionListener {

    public static void start(Context context, HistoryExperiment experiment) {

        Navigator.navigate(context, UserSettingsStep2Activity.class, experiment);
    }

    RecyclerView rv;
    StageAdapter mStageAdapter;

    @BindView(R.id.tv_mode)
    TextView tv_mode;
    @BindView(R.id.layout_melt)
    LinearLayout layout_melt;
    @BindView(R.id.tv_start_temp)
    TextView tv_start_temp;
    @BindView(R.id.tv_end_temp)
    TextView tv_end_temp;

    private Handler handler = new Handler();
    private HistoryExperiment mHistoryExperiment;
    private ExpeDataStore mExpeDataStore;
    Subscription mReadTrimSubscription;

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("用户设置2");
        mTitleBar.setRightIcon(R.drawable.icon_program_save);

        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存实验设置
                LoadingDialogHelper.showOpLoading(getActivity());
                buildExperiment();
                final InsertExpeRequest request = new InsertExpeRequest();
                request.setExperiment(mHistoryExperiment);
                mExpeDataStore.insertExpe(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<InsertExpeResponse>() {
                            @Override
                            public void call(InsertExpeResponse response) {
                                LoadingDialogHelper.hideOpLoading();
                                String msg;
                                if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                                    //保存成功
                                    msg = "保存成功";
                                } else {
                                    msg = "保存失败";
                                }
                                ToastUtil.showToast(getActivity(), msg);
                            }
                        });
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting_step2);
        ButterKnife.bind(this);
        mHistoryExperiment = Navigator.getParcelableExtra(getActivity());
        mCommunicationService = DeviceProxyHelper.getInstance(getActivity()).getCommunicationService();

        mExpeDataStore = new ExpeDataStore(
                ProviderModule
                        .getInstance()
                        .provideBriteDb(DbOpenHelper.getInstance(getApplicationContext())));

        EventBus.getDefault().register(this);
        rv = findViewById(R.id.rv);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv.setLayoutManager(manager);
        rv.setNestedScrollingEnabled(false);
        mStageAdapter = new StageAdapter(getActivity());
        rv.setAdapter(mStageAdapter);

        ExpeSettingSecondInfo expeSettingSecondInfo = mHistoryExperiment.getSettingSecondInfo();
        if (expeSettingSecondInfo == null) {
            List<DisplayItem> list = new ArrayList<>();
            list.add(new StartStage());
            list.add(new CyclingStage());
            list.add(new EndStage());
            mStageAdapter.addAll(list);
            mModes = new ArrayList<>();
            mModes.add(new DtMode("变温扩增"));
        } else {
            mModes = expeSettingSecondInfo.getModes();
            buildModeShowName();
            List<DisplayItem> list = new ArrayList<>();
            List<Stage> stageList = expeSettingSecondInfo.getSteps();
            list.addAll(stageList);
            mStageAdapter.replace(list);

        }

        rv.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStageAdapter.buildLink();
            }
        }, 500);


    }

    @Subscribe
    public void onAddStartStageEvent(AddStartStageEvent event) {
        //判断预变性温度，最多只能有两个
        Stage stage = (Stage) mStageAdapter.getItems().get(1);
        if (stage instanceof StartStage) {
            ToastUtil.showToast(getActivity(), "最多两个预变性阶段");
            return;
        }
        mStageAdapter.add(event.getPosition(), new StartStage());
        buildLink();
        Log.i("StartStage", "onAddStartStageEvent");
    }

    @Subscribe
    public void onDelStartStageEvent(DelStartStageEvent event) {
        //判断预变性温度，最少有一个
        Stage stage = (Stage) mStageAdapter.getItems().get(1);
        if (stage instanceof StartStage) {
            mStageAdapter.remove(event.getPosition());
            buildLink();
            Log.i("ChangeStage", "onDelStartStageEvent");
        } else {
            ToastUtil.showToast(getActivity(), "最少一个预变性阶段");
            return;
        }


    }

    @Subscribe
    public void onRefreshStageAdapterEvent(RefreshStageAdapterEvent event) {
        mStageAdapter.notifyDataSetChanged();
        buildLink();
        Log.i("ChangeStage", "onRefreshStageAdapterEvent");
    }

    @Subscribe
    public void onAddCyclingStage(AddCyclingStageEvent event) {
        mStageAdapter.add(event.getPosition(), new CyclingStage());

        buildLink();

        Log.i("ChangeStage", "onAddCyclingStage");
    }

    @Subscribe
    public void onDelCyclingStageEvent(DelCyclingStageEvent event) {
        mStageAdapter.remove(event.getPosition());
        buildLink();
        Log.i("ChangeStage", "onDelCyclingStageEvent");
    }

    private void buildLink() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStageAdapter.buildLink();
                mStageAdapter.notifyDataSetChanged();
            }
        }, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCommunicationService != null) {
            mCommunicationService.setNotify(null);
        }

        if (mReadTrimSubscription != null) {
            if (!mReadTrimSubscription.isUnsubscribed()) {
                mReadTrimSubscription.unsubscribe();
            }
        }
        mReadTrimSubscription = null;
        EventBus.getDefault().unregister(this);
    }

    private void buildExperiment() {
        ExpeSettingSecondInfo secondInfo = mHistoryExperiment.getSettingSecondInfo();
        String deviceName = "未知设备";
        if (mCommunicationService != null && mCommunicationService.getConnectedDevice() != null) {
            deviceName = mCommunicationService.getConnectedDevice().getDeviceName();
        }

        mHistoryExperiment.setDevice(deviceName);
        ExperimentStatus status = new ExperimentStatus();
        status.setStatus(ExperimentStatus.STATUS_NOT_START);
        status.setDesc("未启动");
        mHistoryExperiment.setStatus(status);
        if (secondInfo == null) {
            secondInfo = new ExpeSettingSecondInfo();
            mHistoryExperiment.setSettingSecondInfo(secondInfo);
        }
      /*  String startT = tv_start_temp.getText().toString().trim();
        String endT = tv_end_temp.getText().toString().trim();*/
        String startT = "0";
        String endT = "0";
        if (mModes.size() > 1) {
            Stage s=(MeltingStage)(mStageAdapter.getItem(mStageAdapter.getItemCount()-2));
            Stage e=(MeltingStage)(mStageAdapter.getItem(mStageAdapter.getItemCount()-1));
            startT=s.getTemp()+"";
            endT=e.getTemp()+"";
        }

        secondInfo.setStartTemperature(startT);
        secondInfo.setEndTemperature(endT);
        secondInfo.setModes(mModes);
        List<Stage> stageList = new ArrayList<>();
        for (int i = 0; i < mStageAdapter.getItemCount(); i++) {
            Stage stage = (Stage) mStageAdapter.getItem(i);
            stageList.add(stage);
        }
        secondInfo.setSteps(stageList);
    }

    public void setSensorAndInTime(int c, float inTime) {
        sleep(50);
        PcrCommand sensorCmd = new PcrCommand();
        sensorCmd.setSensor(c);
        mCommunicationService.sendPcrCommandSync(sensorCmd);

        sleep(50);
        PcrCommand inTimeCmd = new PcrCommand();
        inTimeCmd.setIntergrationTime(inTime);
        mCommunicationService.sendPcrCommandSync(inTimeCmd);

    }

    private List<Mode> mModes;
    CommunicationService mCommunicationService;

    @OnClick({R.id.rl_mode_sel, R.id.tv_next, R.id.tv_start_temp, R.id.tv_end_temp})
    public void onViewClick(View v) {
        int start = 20;
        List<IWheelVo> data = new ArrayList<>();
        switch (v.getId()) {
            case R.id.tv_start_temp:

                for (int i = start; i <= 100; i++) {
                    WheelSimpleVo simpleVo = new WheelSimpleVo(i, i + "");
                    data.add(simpleVo);
                }
                WheelPickerFactory.showWheelAPicker(tv_start_temp, new WheelPickerFactory.OnWheelClickListener() {
                    @Override
                    public void onResult(View v, IWheelVo[] result, int[] indexs, String[] unit) {
                        tv_start_temp.setText(result[0].getLabel());
                    }
                }, data, "℃", 30);
                break;
            case R.id.tv_end_temp:
              /*  Stage stage= (Stage) mStageAdapter.getItem(mStageAdapter.getItemCount()-1);
                int temp= (int) stage.getTemp();
                tv_start_temp.setText( temp+"");
                int start= (int) Math.ceil(temp);*/
                for (int i = start; i <= 100; i++) {
                    WheelSimpleVo simpleVo = new WheelSimpleVo(i, i + "");
                    data.add(simpleVo);
                }
                WheelPickerFactory.showWheelAPicker(tv_start_temp, new WheelPickerFactory.OnWheelClickListener() {
                    @Override
                    public void onResult(View v, IWheelVo[] result, int[] indexs, String[] unit) {
                        tv_end_temp.setText(result[0].getLabel());
                    }
                }, data, "℃", 50);


                break;
            case R.id.tv_next:
                if (validate()) {

                    LoadingDialogHelper.showOpLoading(getActivity());
                    buildExperiment();

                    if (CommData.sTrimFromFile) {
                        FlashData.flash_loaded = false;
                        //设置积分时间
                        setIntergrationTime()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean aBoolean) {
                                        LoadingDialogHelper.hideOpLoading();
                                        ExpeRunningActivity.start(getActivity(), mHistoryExperiment);
                                    }
                                });
                    } else {
                        readTrimDataFromInstrument();
                    }


                }

                break;
            case R.id.rl_mode_sel:
                if (mModes == null) {
                    mModes = new ArrayList<>();
                    mModes.add(new DtMode("变温扩增"));
                }
                AppDialogHelper.showModeSelectDialog(getActivity(), mModes, new AppDialogHelper.OnModeSelectListener() {
                    @Override
                    public void onModeSelected(List<Mode> modes) {
                        mModes = modes;
                        if (modes.size() > 1) {
                            Stage stage = (Stage) mStageAdapter.getItem(mStageAdapter.getItemCount() - 1);
                            int temp = (int) stage.getTemp();
                            tv_start_temp.setText(temp + "");


                            addMeltingStage();
                        } else {
                            removeMeltingStage();
                        }

                        buildModeShowName();
                    }
                });
                break;
        }
    }

    private void removeMeltingStage() {
        if (mStageAdapter.getItem(mStageAdapter.getItemCount() - 1)
                instanceof MeltingStage) {
            mStageAdapter.remove(mStageAdapter.getItemCount() - 1);
            mStageAdapter.remove(mStageAdapter.getItemCount() - 1);
        }
    }

    private void addMeltingStage() {
        if (mStageAdapter.getItem(mStageAdapter.getItemCount() - 1)
                instanceof MeltingStage) {

        } else {
            mStageAdapter.add(new MeltingStage());
            mStageAdapter.add(new MeltingStage());

            buildLink();
        }
    }

    private int mReadTrimCount;

    private void readTrimDataFromInstrument() {
        mReadTrimSubscription = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (mReadTrimCount == 3) {
                            mReadTrimSubscription.unsubscribe();
                            showConnectionTip();
                            return;
                        }
                        if (mReadTrimCount >= 2) {
                            mReadTrimSubscription.unsubscribe();
                            verifyConnection();
                        } else {
                            mReadTrimCount++;
                            mCommunicationService.setNotify(UserSettingsStep2Activity.this);
                            TrimReader.getInstance().ReadTrimDataFromInstrument(mCommunicationService);
                        }

                    }
                });

    }

    private void verifyConnection() {
        PcrCommand cmd = PcrCommand.ofLidAndApaptorStatusCmd();
        byte[] reveicedBytes = mCommunicationService.sendPcrCommandSync(cmd);
        if (reveicedBytes != null) {
            try {
                int statusIndex = 1;
                int status = reveicedBytes[statusIndex];
                //TODO 检查返回的包是否正确
                boolean succ = StatusChecker.checkStatus(status);
                if (succ) {
                    readTrimDataFromInstrument();
                } else {
                    showConnectionTip();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showConnectionTip();
            }

        } else {
            showConnectionTip();
        }
    }

    private void showConnectionTip() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingDialogHelper.hideOpLoading();
                AppDialogHelper.showNormalDialog(getActivity(), "请检查HID设备连接情况", new AppDialogHelper.DialogOperCallback() {
                    @Override
                    public void onDialogConfirmClick() {

                    }
                });
            }
        });
    }

    public Observable<Boolean> setIntergrationTime() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {


                doSetIntergrationTime();
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });
    }

    private void doSetIntergrationTime() {
        //删除日志文件
        DataFileUtil.removeLogFile();
        if (!TextUtils.isEmpty(dp_str)) {
            //生成数据文件，插入dp_str
            File file = DataFileUtil.getDtImageDataFile(mHistoryExperiment);
            DataFileUtil.writeToFile(file, dp_str);
            boolean hasMelting = mHistoryExperiment.getSettingSecondInfo().getModes().size() == 2;
            if (hasMelting) {
                File meltFile = DataFileUtil.getMeltImageDateFile(mHistoryExperiment);
                DataFileUtil.writeToFile(meltFile, dp_str);
            }
        }
        //初始化设备
        resetTrim();


        FactUpdater factUpdater = FactUpdater.getInstance(mCommunicationService);
        factUpdater.SetInitData();
        int integrationTime = mHistoryExperiment.getIntegrationTime();
        if (integrationTime > 0) {
            //获取积分时间
            factUpdater.int_time_1 = mHistoryExperiment.getIntegrationTime();
            factUpdater.int_time_2 = mHistoryExperiment.getIntegrationTime();
            factUpdater.int_time_3 = mHistoryExperiment.getIntegrationTime();
            factUpdater.int_time_4 = mHistoryExperiment.getIntegrationTime();
        }
        if (mCommunicationService != null) {
            sleep(50);
            PcrCommand gainCmd = new PcrCommand();
            gainCmd.setGainMode();
            mCommunicationService.sendPcrCommandSync(gainCmd);

            setSensorAndInTime(0, factUpdater.int_time_1);
            setSensorAndInTime(1, factUpdater.int_time_2);
            setSensorAndInTime(2, factUpdater.int_time_3);
            setSensorAndInTime(3, factUpdater.int_time_4);

        }
    }

    private void resetTrim() {
        CommunicationService service = mCommunicationService;
        if (service == null) {
            return;
        }

        PcrCommand cmd = new PcrCommand();
        for (int i = 1; i <= 4; i++) {
            cmd.reset();
            cmd.SelSensor(i);
            service.sendPcrCommandSync(cmd);
            sleep(50);
            cmd.reset();
            cmd.ResetParams();
            service.sendPcrCommandSync(cmd);
            sleep(50);
        }
        int[] chan_campgen = {
                CommData.chan1_rampgen,
                CommData.chan2_rampgen,
                CommData.chan3_rampgen,
                CommData.chan4_rampgen};

        int[] v15 = {
                CommData.chan1_auto_v15,
                CommData.chan2_auto_v15,
                CommData.chan3_auto_v15,
                CommData.chan4_auto_v15};
        for (int i = 1; i <= 4; i++) {
            cmd.reset();
            cmd.SelSensor(i);
            service.sendPcrCommandSync(cmd);
            sleep(50);

            cmd.reset();
            cmd.SetRampgen(chan_campgen[i - 1]);
            service.sendPcrCommandSync(cmd);
            sleep(50);

            cmd.reset();
            cmd.SetTXbin((byte) 0xf);
            service.sendPcrCommandSync(cmd);
            sleep(50);

            cmd.reset();
            cmd.SetRange((byte) 0x0f);
            service.sendPcrCommandSync(cmd);
            sleep(50);

            cmd.reset();
            cmd.SetV15(v15[i - 1]);
            service.sendPcrCommandSync(cmd);
            sleep(50);
        }


        CommData.gain_mode = 0;// initialize to high gain mode, consistent with HW default
        sleep(50);
        doSetV20(service, CommData.chan1_auto_v20[1], 1);
        doSetV20(service, CommData.chan2_auto_v20[1], 2);
        doSetV20(service, CommData.chan3_auto_v20[1], 3);
        doSetV20(service, CommData.chan4_auto_v20[1], 4);

        CommData.int_time1 = CommData.int_time2 = CommData.int_time3 = CommData.int_time4 = 10;
        sleep(50);
        doSetLEDConfig(service);
        sleep(50);
    }

    private void doSetV20(CommunicationService service, int v20, int index) {
        PcrCommand cmd = new PcrCommand();

        cmd.SelSensor(index);
        service.sendPcrCommandSync(cmd);
        sleep(50);
        cmd.reset();
        cmd.SetV20(v20);
        service.sendPcrCommandSync(cmd);
        sleep(50);
    }

    private void doSetLEDConfig(CommunicationService service) {
        PcrCommand cmd = new PcrCommand();
        cmd.reset();
        cmd.SetLEDConfig(1, 1, 1, 1, 1);
        service.sendPcrCommandSync(cmd);
        sleep(50);
        cmd.reset();
        cmd.SetLEDConfig(1, 0, 0, 0, 0);
        service.sendPcrCommandSync(cmd);
    }

    public void buildModeShowName() {
        StringBuilder sBuilder = new StringBuilder(mModes.get(0).getName());
        if (mModes.size() == 2) {
            sBuilder.append("+")
                    .append(mModes.get(1).getName());
            //  layout_melt.setVisibility(View.VISIBLE);
        } else {
            // layout_melt.setVisibility(View.GONE);
        }
        tv_mode.setText(sBuilder.toString());
    }

    private boolean validate() {
        if (mModes == null) {
            ToastUtil.showToast(getActivity(), "请选择程序模式");
            return false;
        }

       /* if (mModes.size() > 1) {
            //温度必须填写
            String startTemp = tv_start_temp.getText().toString();
            String endTemp = tv_end_temp.getText().toString();
            if (TextUtils.isEmpty(endTemp)) {
                ToastUtil.showToast(getActivity(), "请输入熔解曲线结束温度");
                return false;
            }
        }*/
        return true;
    }

    @Subscribe
    public void onExpeNormalFinishEvent(ExpeNormalFinishEvent event) {
        ActivityUtil.finish(getActivity());
    }

    @Override
    public void onConnectSuccess() {

    }

    @Override
    public void onConnectCancel() {

    }

    @Override
    public void onDoThing() {

    }

    //dataposition.dat文件内容
    private String dp_str;

    @Override
    public void onReceivedData(Data data) {
        if (mReadTrimSubscription == null) {
            return;
        }
        mReadTrimSubscription.unsubscribe();
        //TODO 读取trim数据和dataposition数据
        byte[] reveicedBytes = data.getBuffer();
        int statusIndex = 1;
        int status = reveicedBytes[statusIndex];
        //TODO 检查返回的包是否正确
        boolean succ = StatusChecker.checkStatus(status);
        if (!succ) {
            return;
        }
        int cmdIndex = 2;
        int cmd = reveicedBytes[cmdIndex];

        int typeIndex = 4;
        int type = (reveicedBytes[typeIndex]);
        if (cmd == PcrCommand.READ_TRIM_CMD && type == PcrCommand.READ_TRIM_TYPE) {

            //接收到数据
            int length = reveicedBytes[3];
            int chanIndex = reveicedBytes[5];
            int curIndex = reveicedBytes[7];        // 当前是index个回复包，  0 -（total-1）
            int total = reveicedBytes[6];    //总共有多少个回复包
            MoveToEEPBuffer(reveicedBytes, curIndex);
            //header status   cmd  length   type  cIndex  total   curIndex
            //aa      00      04     39      2d    00      16       00     780000040406090602060306040702070307040802080308040906060607060807060707070808060807080809020202030204038b0b1717
            //判断是否读取完毕
            if (curIndex == (total - 1)) {
                List<Integer> rlist = new ArrayList<>();      // row index
                List<Integer> clist = new ArrayList<>();      // col index

                byte[] trim_buff = new byte[2048];

                for (int j = 0; j < EPKT_SZ; j++) {           // parity not copied
                    trim_buff[j] = EepromBuff[0][j];        // copy first page
                }

                int k = 0;

                char version = (char) trim_buff[k];
                k++;
                int sn1 = trim_buff[k];
                k++;
                int sn2 = trim_buff[k];
                k++;

                int num_channels = trim_buff[k];//通道数
                k++;
                int num_wells = trim_buff[k];//孔数
                k++;
                int num_pages = trim_buff[k];
                k++;

                CommData.KsIndex = num_wells;

                //4.13新增
                CommData.sn1 = sn1;
                CommData.sn2 = sn2;
                for (int i = 1; i < num_pages; i++) {
                    for (int j = 0; j < EPKT_SZ; j++) {           // parity not copied
                        trim_buff[i * EPKT_SZ + j] = EepromBuff[i][j];
                    }
                }

                for (int i = 0; i < num_channels; i++) {
                    for (int j = 0; j < num_wells; j++) {
                        int n = trim_buff[k];
                        k++;
                        rlist.clear();
                        clist.clear();
                        for (int l = 0; l < n; l++) {
                            int row = trim_buff[k++]; // k++;
                            int col = trim_buff[k];
                            k++;

                            rlist.add(row);
                            clist.add(col);
                        }
                        FlashData.row_index[i][j] = new ArrayList<>(rlist);
                        FlashData.col_index[i][j] = new ArrayList<>(clist);
                    }
                }
                dp_str = Buf2String(trim_buff, k);
                for (int ci = 0; ci < num_channels; ci++) {
                    int index_start = num_pages + ci * NUM_EPKT;
                    k = 0;

                    for (int i = 0; i < NUM_EPKT; i++) {
                        for (int j = 0; j < EPKT_SZ; j++) {           // parity not copied
                            trim_buff[i * EPKT_SZ + j] = EepromBuff[i + index_start][j];
                        }
                    }

                    byte b0 = trim_buff[k];
                    k++;        // Extract chip name
                    byte b1 = trim_buff[k];
                    k++;
                    byte b2 = trim_buff[k];
                    k++;

                    for (int i = 0; i < TRIM_IMAGER_SIZE; i++) {
                        for (int j = 0; j < 6; j++) {
                            FlashData.kbi[ci][i][j] = Buf2Int(trim_buff, k);
                            k += 2;
                        }
                    }

                    for (int i = 0; i < TRIM_IMAGER_SIZE; i++) {
                        FlashData.fpni[ci][0][i] = Buf2Int(trim_buff, k);
                        k += 2;
                        FlashData.fpni[ci][1][i] = Buf2Int(trim_buff, k);
                        k += 2;
                    }

                    FlashData.rampgen[ci] = trim_buff[k];
                    k++;
                    FlashData.range[ci] = trim_buff[k];
                    k++;
                    FlashData.auto_v20[ci][0] = trim_buff[k];
                    k++;
                    FlashData.auto_v20[ci][1] = trim_buff[k];
                    k++;
                    FlashData.auto_v15[ci] = trim_buff[k];
                    k++;
                }

                CommData.chan1_rampgen = FlashData.rampgen[0];
                CommData.chan2_rampgen = FlashData.rampgen[1];
                CommData.chan3_rampgen = FlashData.rampgen[2];
                CommData.chan4_rampgen = FlashData.rampgen[3];

                CommData.chan1_auto_v15 = FlashData.auto_v15[0];
                CommData.chan2_auto_v15 = FlashData.auto_v15[1];
                CommData.chan3_auto_v15 = FlashData.auto_v15[2];
                CommData.chan4_auto_v15 = FlashData.auto_v15[3];

                CommData.chan1_auto_v20 = FlashData.auto_v20[0];
                CommData.chan2_auto_v20 = FlashData.auto_v20[1];
                CommData.chan3_auto_v20 = FlashData.auto_v20[2];
                CommData.chan4_auto_v20 = FlashData.auto_v20[3];

                CommData.chan1_range = FlashData.range[0];
                CommData.chan2_range = FlashData.range[1];
                CommData.chan3_range = FlashData.range[2];
                CommData.chan4_range = FlashData.range[3];
                FlashData.flash_loaded = true;


                //读取trim和dataposition成功
                setIntergrationTime()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                LoadingDialogHelper.hideOpLoading();
                                ExpeRunningActivity.start(getActivity(), mHistoryExperiment);
                            }
                        });


            }
        }
    }

    private int Buf2Int(byte[] buff, int k) {
        byte[] x = {buff[k + 1],
                buff[k]};
        int y = ByteUtil.getShort(x);
        //int y = (int) BitConverter.ToInt16(x, 0);

        return y;
    }

    int EPKT_SZ = 52;
    int NUM_EPKT = 4;
    int TRIM_IMAGER_SIZE = 12;
    byte[][] EepromBuff = new byte[8 + 4 * NUM_EPKT][EPKT_SZ + 1];

    private void MoveToEEPBuffer(byte[] inputdatas, int index) {
        byte eeprom_parity = 0;

        for (int i = 0; i < EPKT_SZ + 1; i++) {
            EepromBuff[index][i] = inputdatas[8 + i];

            if (i < EPKT_SZ) {
                eeprom_parity += inputdatas[8 + i];
            } else {
                if (eeprom_parity != inputdatas[8 + i]) {
                    LogUtil.e("Packet parity error!");
                }
            }
        }
    }

    private String Buf2String(byte[] buff, int size) {
        //String rstr;
        StringBuilder sBuilder = new StringBuilder();
        //rstr = "Chipdp\r\n";
        sBuilder.append("Chipdp\r\n");
        for (int i = 0; i < size; i++) {
            sBuilder.append(buff[i] + " ");
            //String str = String.format("{0} ", buff[i]);
            //rstr += str;
        }
        // rstr += "\r\n";
        sBuilder.append("\r\n");
        return sBuilder.toString();
    }
}
