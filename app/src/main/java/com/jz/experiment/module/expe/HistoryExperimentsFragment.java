package com.jz.experiment.module.expe;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.anitoa.Anitoa;
import com.anitoa.bean.FlashData;
import com.anitoa.cmd.PcrCommand;
import com.anitoa.event.AnitoaDisConnectedEvent;
import com.anitoa.service.CommunicationService;
import com.anitoa.util.AnitoaLogUtil;
import com.anitoa.util.ThreadUtil;
import com.jz.experiment.R;
import com.jz.experiment.chart.CommData;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.expe.activity.DeviceListActivity;
import com.jz.experiment.module.expe.activity.UserSettingsStep1Activity;
import com.jz.experiment.module.expe.adapter.HistoryExperimentAdapter;
import com.jz.experiment.module.expe.event.RefreshExpeItemsEvent;
import com.jz.experiment.module.expe.event.ToExpeSettingsEvent;
import com.jz.experiment.module.settings.UserSettingsActivity;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.util.FlashTrimReader;
import com.jz.experiment.util.StatusChecker;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.recyclerview.decoration.DividerGridItemDecoration;
import com.wind.base.response.BaseResponse;
import com.wind.coder.annotations.Api;
import com.wind.coder.annotations.Heros;
import com.wind.coder.annotations.Param;
import com.wind.data.expe.bean.AddExperiment;
import com.wind.data.expe.bean.HistoryExperiment;
import com.wind.data.expe.datastore.ExpeDataStore;
import com.wind.data.expe.request.DelExpeRequest;
import com.wind.data.expe.response.DelExpeResponse;
import com.wind.data.expe.response.FindExpeResponse;
import com.wind.toastlib.ToastUtil;
import com.wind.view.DisplayUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private Anitoa sAnitoa;
  //  private ExecutorService mExecutorService;
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

     //   mExecutorService = Executors.newSingleThreadExecutor();
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

        GridLayoutManager manager=new GridLayoutManager(getContext(),2);
        rv.setLayoutManager(manager);
        mAdapter = new HistoryExperimentAdapter(getActivity());
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new DividerGridItemDecoration(DisplayUtil.dip2px(getActivity(), 8)));

        Observable.timer(300, TimeUnit.MILLISECONDS,Schedulers.io())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mExpeDataStore = new ExpeDataStore(ProviderModule.getInstance()
                                .getBriteDb(getActivity()
                                        .getApplicationContext()));
                        loadData();

                        //初始化blutoothservice
                        sAnitoa = Anitoa
                                .getInstance(getActivity());
                    }
                });


        AnitoaLogUtil.writeFileLog("HistoryExperimentsFragment onViewCreated");
    }




    @Override
    public void onResume() {
        super.onResume();

        if (sAnitoa!=null&&sAnitoa.getUsbService() != null && sAnitoa.getUsbService().isConnected()) {

            tv_device_state.setText( getString(R.string.device_status_bar_connected));
            tv_device_state.setActivated(true);
        } else {
            tv_device_state.setText( getString(R.string.device_status_bar_no_device_connected));
            tv_device_state.setActivated(false);
        }

        loadData();
       // System.out.println("HistoryExperimentsFragment onResume");
    }

    /**
     * 设备连接断开
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothDisConnectedEvent(AnitoaDisConnectedEvent event) {
        tv_device_state.setText( getString(R.string.device_status_bar_no_device_connected));
        tv_device_state.setActivated(false);
    }

    public void loadData() {
        if (mExpeDataStore==null){
            return;
        }
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
    private boolean mReadLiding;
    private Observable<Integer> readLid() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {

            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                PcrCommand cmd = PcrCommand.ofLidAndApaptorStatusCmd();
                //mCommunicationService.stopReadThread();
               /* try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                byte[] reveicedBytes = mCommunicationService.sendPcrCommandSync(cmd);
                int count=0;
                while (reveicedBytes==null || reveicedBytes[0]==0){
                    if (count>=3){
                        break;
                    }
                    reveicedBytes = mCommunicationService.sendPcrCommandSync(cmd);
                    count++;
                    ThreadUtil.sleep(100);
                }
              /*  String lid=ByteUtil.getHexStr(reveicedBytes,reveicedBytes.length);
                DataFileUtil.writeFileLog(lid);*/

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
            mCommunicationService=sAnitoa.getCommunicationService();
        }

        //判断是否已经连接设备
        AndPermission.with(getActivity())
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        if (mReadLiding){
                            return;
                        }
                        mReadLiding=true;

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
                        //UserSettingsStep1Activity.start(getActivity(), event.getExperiment());


                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                        ToastUtil.showToast(getActivity(),  getResString(R.string.tip_sd_permission));

                    }
                }).start();


    }

    private void doNextByStatus(Integer status, ToExpeSettingsEvent event) {
        switch (status) {
            case CODE_SUCCESS:

                //TODO 判断是否读取了下位机的trim
                if (CommData.sTrimFromFile) {
                    UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                    mReadLiding=false;
                } else {
                    if (FlashData.flash_inited) {
                        mReadLiding=false;
                        UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
                    } else {
                        readTrimDataFromInstrument(event);
                    }
                }
                break;

            case CODE_LID_ERROR:
                LoadingDialogHelper.hideOpLoading();
                mReadLiding=false;
                ToastUtil.showToast(getActivity(), getResString(R.string.tip_close_heating_cover));
                break;
            case CODE_ADAPTOR_ERROR:
                LoadingDialogHelper.hideOpLoading();
                mReadLiding=false;
                ToastUtil.showToast(getActivity(), getResString(R.string.tip_power_cord));
                break;
            case CODE_NOT_CONECTED:
                LoadingDialogHelper.hideOpLoading();
                mReadLiding=false;
                ToastUtil.showToast(getActivity(), getResString(R.string.check_hid_connection));
                break;
        }
    }

    public String getResString(int res){
        return getActivity().getString(res);
    }
    private void readTrimDataFromInstrument(final ToExpeSettingsEvent event) {

        final FlashTrimReader reader = new
                FlashTrimReader(mCommunicationService);
        reader.setOnReadFlashListener(new FlashTrimReader.OnReadFlashListener() {
            //读取flash成功返回
            @Override
            public void onReadFlashSuccess() {
                reader.destroy();
                mReadLiding=false;
                UserSettingsStep1Activity.start(getActivity(), event.getExperiment());
            }

        });
        reader.setOnDeviceDisconnectionListener(new FlashTrimReader.OnDeviceDisconnectionListener() {
            @Override
            public void onDeviceDisconnected() {
                showConnectionTip();
            }
        });
        reader.readTrimDataFromInstrument();

    }

    private void showConnectionTip(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingDialogHelper.hideOpLoading();
                AppDialogHelper.showNormalDialog(getActivity(),  getResString(R.string.check_hid_connection), new AppDialogHelper.DialogOperCallback() {
                    @Override
                    public void onDialogConfirmClick() {

                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AnitoaLogUtil.writeFileLog("HistoryExperimentsFragment onDestroyView");
        EventBus.getDefault().unregister(this);
    }

   /* public int getContextMenuPosition() {
        return mAdapter.getContextMenuPosition();
    }*/

    public void doItemLongClick() {
        if(mAdapter==null){
            return;
        }
        HistoryExperiment experiment=mAdapter.getLongClingItemData();
        if (experiment==null){
            return;
        }
        final DelExpeRequest request=new DelExpeRequest();
        request.setId(experiment.getId());
        mExpeDataStore.delExpe(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DelExpeResponse>() {
                    @Override
                    public void call(DelExpeResponse response) {

                        loadData();

                        EventBus.getDefault().post(new RefreshExpeItemsEvent());
                    }
                });
    }
}
