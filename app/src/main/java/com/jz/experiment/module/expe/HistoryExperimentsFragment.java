package com.jz.experiment.module.expe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.bluetooth.CommunicationService;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;
import com.jz.experiment.module.expe.activity.DeviceListActivity;
import com.jz.experiment.module.expe.activity.UserSettingsStep1Activity;
import com.jz.experiment.module.expe.adapter.HistoryExperimentAdapter;
import com.jz.experiment.module.expe.event.ToExpeSettingsEvent;
import com.jz.experiment.module.settings.UserSettingsActivity;
import com.jz.experiment.util.DeviceProxyHelper;
import com.jz.experiment.util.TrimReader;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.recyclerview.decoration.VerticalSpacesItemDecoration;
import com.wind.base.response.BaseResponse;
import com.wind.coder.annotations.Api;
import com.wind.coder.annotations.Heros;
import com.wind.coder.annotations.Param;
import com.wind.data.expe.bean.AddExperiment;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.response.FindExpeResponse;
import com.wind.toastlib.ToastUtil;
import com.wind.view.DisplayUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ezy.ui.layout.LoadingLayout;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@Heros(
        param = @Param(
                viewCanonicalName = "com.jz.experiment.module.expe.mvp.HistoryExperimentsView",
                responseCanonicalName = "com.wind.data.expe.HistoryExperimentsResponse",
                requestCanonicalName = "com.wind.data.expe.HistoryExperimentsRequest"
        ),
        api = @Api(httpMethod = Api.HttpMethod.POST, url = "/login")
)
public class HistoryExperimentsFragment extends BaseFragment {

    @BindView(R.id.rv)
    RecyclerView rv;
    HistoryExperimentAdapter mAdapter;
    ExpeDataStore mExpeDataStore;
    @BindView(R.id.layout_loading)
    LoadingLayout layout_loading;
    @BindView(R.id.tv_device_state)
    TextView tv_device_state;

    private DeviceProxyHelper sDeviceProxyHelper;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_hostory_experiments;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        layout_loading.setEmpty(R.layout.layout_expe_empty);
        layout_loading.setOnEmptyInflateListener(new LoadingLayout.OnInflateListener() {
            @Override
            public void onInflate(View inflated) {
                inflated.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onToExpeSettingsEvent(new ToExpeSettingsEvent(null));
                    }
                });
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        mAdapter = new HistoryExperimentAdapter(getActivity());
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new VerticalSpacesItemDecoration(DisplayUtil.dip2px(getActivity(), 8)));
        mExpeDataStore = new ExpeDataStore(ProviderModule.getInstance()
                .getBriteDb(getActivity()
                        .getApplicationContext()));
        loadData();

        //初始化blutoothservice
        sDeviceProxyHelper = DeviceProxyHelper
                .getInstance(getActivity());

       /* */
    }

    /**
     * 下位机复位
     */
    public void resetTrim() {
        CommunicationService service = sDeviceProxyHelper.getCommunicationService();
        if (service.getConnectedDevice() != null &&
                service.isConnected()) {
            LoadingDialogHelper.showOpLoading(getActivity());
            doResetTrim(service)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            LoadingDialogHelper.hideOpLoading();
                        }
                    });
        }else {
            ToastUtil.showToast(getActivity(),"未连接设备");
        }
    }

    private Observable<Boolean> doResetTrim(final CommunicationService service) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                PcrCommand cmd = new PcrCommand();
                for (int i = 1; i <= 4; i++) {
                    cmd.reset();
                    cmd.SelSensor(i);
                    service.sendPcrCommandSync(cmd);
                    cmd.reset();
                    cmd.ResetParams();
                    service.sendPcrCommandSync(cmd);
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

                    cmd.reset();
                    cmd.SetRampgen(chan_campgen[i - 1]);
                    service.sendPcrCommandSync(cmd);

                    cmd.reset();
                    cmd.SetTXbin((byte) 0xf);
                    service.sendPcrCommandSync(cmd);

                    cmd.reset();
                    cmd.SetRange((byte) 0x0f);
                    service.sendPcrCommandSync(cmd);

                    cmd.reset();
                    cmd.SetV15(v15[i - 1]);
                    service.sendPcrCommandSync(cmd);
                }


                CommData.gain_mode = 0;// initialize to high gain mode, consistent with HW default

                doSetV20(service, CommData.chan1_auto_v20[1], 1);
                doSetV20(service, CommData.chan2_auto_v20[1], 2);
                doSetV20(service, CommData.chan3_auto_v20[1], 3);
                doSetV20(service, CommData.chan4_auto_v20[1], 4);

                CommData.int_time1 = CommData.int_time2 = CommData.int_time3 = CommData.int_time4 = 1;

                doSetLEDConfig(service);

                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });


    }

    private void doSetLEDConfig(CommunicationService service) {
        PcrCommand cmd = new PcrCommand();
        cmd.reset();
        cmd.SetLEDConfig(1, 1, 1, 1, 1);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cmd.reset();
        cmd.SetLEDConfig(1, 0, 0, 0, 0);
        service.sendPcrCommandSync(cmd);
    }

    private void doSetV20(CommunicationService service, int v20, int index) {
        PcrCommand cmd = new PcrCommand();

        cmd.SelSensor(index);
        service.sendPcrCommandSync(cmd);

        cmd.reset();
        cmd.SetV20(v20);
        service.sendPcrCommandSync(cmd);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sDeviceProxyHelper.isConnected()) {
            tv_device_state.setText("设备已连接");
            tv_device_state.setActivated(true);
        } else {
            tv_device_state.setText("设备未连接");
            tv_device_state.setActivated(false);
        }
    }

    /**
     * 设备连接断开
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothDisConnectedEvent(BluetoothDisConnectedEvent event) {
        tv_device_state.setText("设备未连接");
        tv_device_state.setActivated(false);
    }

    public void loadData() {

        mExpeDataStore
                .findAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FindExpeResponse>() {
                    @Override
                    public void call(FindExpeResponse response) {
                        if (response.getErrCode() == BaseResponse.CODE_SUCCESS) {
                            List<HistoryExperiment> experiments = response.getItems();
                            if (experiments == null || experiments.isEmpty()) {
                                layout_loading.showEmpty();
                            } else {
                                mAdapter.getItems().clear();
                                mAdapter.getItems().add(new AddExperiment());
                                mAdapter.getItems().addAll(experiments);
                                mAdapter.notifyDataSetChanged();
                                layout_loading.showContent();
                            }

                        } else {
                            //显示空白页
                            layout_loading.showEmpty();
                        }
                    }
                });

    }

    @OnClick({R.id.tv_device_state, R.id.tv_user, R.id.tv_reset_device})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.tv_reset_device:
                resetTrim();
                break;
            case R.id.tv_user:
                UserSettingsActivity.start(getActivity());
                break;
            case R.id.tv_device_state:
                DeviceListActivity.start(getActivity());
                break;
        }
    }


    private boolean mNeedReadTrimFile;
    /**
     * 去实验设置页面
     *
     * @param event
     */
    @Subscribe
    public void onToExpeSettingsEvent(final ToExpeSettingsEvent event) {
        mNeedReadTrimFile=false;
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED) {//没有sd卡读取权限
            mNeedReadTrimFile=true;
        }


        //判断是否已经连接设备
        AndPermission.with(getActivity())
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        if (mNeedReadTrimFile) {
                            //读取dataposition文件
                            CommData.ReadDatapositionFile(getActivity());
                            //trim文件读取到CommonData中
                            TrimReader.getInstance().ReadTrimFile(getActivity());
                            mNeedReadTrimFile=false;
                        }
                        UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                      /*  if (sDeviceProxyHelper.getUsbService().hasPermission()){
                            UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                        }else {
                            sDeviceProxyHelper.getUsbService().requestPermission();
                        }*/


                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                        ToastUtil.showToast(getActivity(), "拒绝访问sd卡权限将无法新建实验");

                    }
                }).start();
      /*  if (sDeviceProxyHelper.isConnected(DeviceProxyHelper.ConnectMode.USB)){

            AndPermission.with(getActivity())
                    .runtime()
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                      *//*  if (sDeviceProxyHelper.getUsbService().hasPermission()){
                            UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                        }else {
                            sDeviceProxyHelper.getUsbService().requestPermission();
                        }*//*


                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {

                            ToastUtil.showToast(getActivity(), "拒绝访问sd卡权限将无法新建实验");

                        }
                    }).start();
        }else {

            sDeviceProxyHelper.getUsbService().requestPermission();
        }*/
       /* if (sDeviceProxyHelper.isConnected()){
            UserSettingsStep1Activity.start(getActivity(),event.getExperiment());
        }else {
            ToastUtil.showToast(getActivity(),"请先连接设备");
        }*/


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
