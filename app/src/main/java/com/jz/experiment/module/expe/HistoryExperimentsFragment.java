package com.jz.experiment.module.expe;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.chart.FlashData;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.bluetooth.CommunicationService;
import com.jz.experiment.module.bluetooth.PcrCommand;
import com.jz.experiment.module.bluetooth.event.BluetoothDisConnectedEvent;
import com.jz.experiment.module.expe.activity.DeviceListActivity;
import com.jz.experiment.module.expe.activity.UserSettingsStep1Activity;
import com.jz.experiment.module.expe.adapter.HistoryExperimentAdapter;
import com.jz.experiment.module.expe.event.ToExpeSettingsEvent;
import com.jz.experiment.module.settings.UserSettingsActivity;
import com.jz.experiment.util.ByteUtil;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.DeviceProxyHelper;
import com.jz.experiment.util.FlashTrimReader;
import com.jz.experiment.util.StatusChecker;
import com.jz.experiment.util.ThreadUtil;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService mExecutorService;
    private FlashTrimReader mFlashTrimReader;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_hostory_experiments;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);

        mExecutorService = Executors.newSingleThreadExecutor();
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


        System.out.println("HistoryExperimentFragment onViewCreated");
    }


    @Override
    public void onResume() {
        super.onResume();

        if (sDeviceProxyHelper.getBluetoothService() != null && sDeviceProxyHelper.getBluetoothService().isConnected()) {
            tv_device_state.setText("已连接");
            tv_device_state.setActivated(true);
        } else {
            tv_device_state.setText("未连接");
            tv_device_state.setActivated(false);
        }

        loadData();
    }

    /**
     * 设备连接断开
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothDisConnectedEvent(BluetoothDisConnectedEvent event) {
        tv_device_state.setText("未连接");
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
                // resetTrim();
                break;
            case R.id.tv_user:
                UserSettingsActivity.start(getActivity());
                break;
            case R.id.tv_device_state:
                DeviceListActivity.start(getActivity());
                break;
        }
    }

    private Observable<Integer> readLid() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {

            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                PcrCommand cmd = PcrCommand.ofLidAndApaptorStatusCmd();
                mCommunicationService.stopReadThread();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] reveicedBytes = mCommunicationService.sendPcrCommandSync(cmd);
                int count=0;
                while (reveicedBytes==null || reveicedBytes[0]==0){
                    if (count>=50){
                        break;
                    }
                    reveicedBytes = mCommunicationService.sendPcrCommandSync(cmd);
                    count++;
                    ThreadUtil.sleep(100);
                }
                String lid=ByteUtil.getHexStr(reveicedBytes,reveicedBytes.length);
                DataFileUtil.writeFileLog(lid);

                if (reveicedBytes == null || reveicedBytes[0] == 0) {
                    subscriber.onNext(CODE_NOT_CONECTED);
                    return;
                }
                int statusIndex = 1;
                int status = reveicedBytes[statusIndex];
                //TODO 检查返回的包是否正确
                boolean succ = StatusChecker.checkStatus(status);
                if (succ) {
                    //检查
                    int lidAndStatusByte = reveicedBytes[5];
                    int lidStatus = lidAndStatusByte & 0x1;
                    int adaptorStatus = (lidAndStatusByte >> 1) & 0x1;
                    if (lidStatus == 1) {
                        subscriber.onNext(CODE_LID_ERROR);
                        return;
                    }
                    if (adaptorStatus == 1) {
                        subscriber.onNext(CODE_ADAPTOR_ERROR);
                        return;
                    }
                    subscriber.onNext(CODE_SUCCESS);
                } else {
                    subscriber.onNext(CODE_NOT_CONECTED);
                }
            }
        });
    }

    public static final int CODE_NOT_CONECTED = -1;
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_LID_ERROR = 1;
    public static final int CODE_ADAPTOR_ERROR = 2;
    private boolean mNeedReadTrimFile;
    private CommunicationService mCommunicationService;
    /**
     * 去实验设置页面
     *
     * @param event
     */
    @Subscribe
    public void onToExpeSettingsEvent(final ToExpeSettingsEvent event) {
        if (mCommunicationService==null){
            mCommunicationService=sDeviceProxyHelper.getCommunicationService();
        }

        //判断是否已经连接设备
        AndPermission.with(getActivity())
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        /*if (mNeedReadTrimFile) {
                            //读取dataposition文件
                            CommData.ReadDatapositionFile(getActivity());
                            //trim文件读取到CommonData中
                            TrimReader.getInstance().ReadTrimFile(getActivity());
                            mNeedReadTrimFile=false;
                        }*/
                        LoadingDialogHelper.showOpLoading(getActivity());
                        //读取下位机是否插入了电源以及热盖的开闭
                        readLid().subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Integer>() {
                                    @Override
                                    public void call(Integer status) {
                                        doNextByStatus(status, event);

                                    }
                                });


                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                        ToastUtil.showToast(getActivity(), "拒绝访问sd卡权限将无法新建实验");

                    }
                }).start();


    }

    private void doNextByStatus(Integer status, ToExpeSettingsEvent event) {
        switch (status) {
            case CODE_SUCCESS:

                //TODO 判断是否读取了下位机的trim
                if (CommData.sTrimFromFile) {
                    UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                } else {
                    if (FlashData.flash_inited) {
                        UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                    } else {
                        readTrimDataFromInstrument(event);
                    }
                }
                break;

            case CODE_LID_ERROR:
                LoadingDialogHelper.hideOpLoading();
                ToastUtil.showToast(getActivity(), "请先关闭热盖");
                break;
            case CODE_ADAPTOR_ERROR:
                LoadingDialogHelper.hideOpLoading();
                ToastUtil.showToast(getActivity(), "请先插入电源适配器");
                break;
            case CODE_NOT_CONECTED:
                LoadingDialogHelper.hideOpLoading();
                ToastUtil.showToast(getActivity(), "请检查HID设备是否已连接");
                break;
        }
    }

    private void readTrimDataFromInstrument(final ToExpeSettingsEvent event) {

        FlashTrimReader reader = new
                FlashTrimReader(getActivity(),
                mCommunicationService);
        reader.setOnReadFlashListener(new FlashTrimReader.OnReadFlashListener() {
            //读取flash成功返回
            @Override
            public void onReadFlashSuccess() {

                UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
            }
        });
        reader.readTrimDataFromInstrument();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
    }
}
