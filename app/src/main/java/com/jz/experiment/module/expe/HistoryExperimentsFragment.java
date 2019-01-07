package com.jz.experiment.module.expe;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.di.ProviderModule;
import com.jz.experiment.module.bluetooth.BluetoothService;
import com.jz.experiment.module.expe.activity.DeviceListActivity;
import com.jz.experiment.module.expe.activity.UserSettingsStep1Activity;
import com.jz.experiment.module.expe.adapter.HistoryExperimentAdapter;
import com.jz.experiment.module.expe.event.ToExpeSettingsEvent;
import com.jz.experiment.module.settings.UserSettingsActivity;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ezy.ui.layout.LoadingLayout;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.Context.BIND_AUTO_CREATE;

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
                        UserSettingsStep1Activity.start(getActivity());
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

        bindService();
    }

    private void bindService() {
        Intent serviceIntent=new Intent(getActivity(),BluetoothService.class);
        getActivity().bindService(serviceIntent,mBluetoothServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothService!=null && mBluetoothService.isConnected()){
            tv_device_state.setText("设备已连接");
            tv_device_state.setActivated(true);
        }else {
            tv_device_state.setText("设备未连接");
            tv_device_state.setActivated(false);
        }
    }

    private void loadData() {

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

    @OnClick({R.id.tv_dev_list, R.id.tv_user})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.tv_user:
                UserSettingsActivity.start(getActivity());
                break;
            case R.id.tv_dev_list:
                DeviceListActivity.start(getActivity());
                break;
        }
    }


    BluetoothService mBluetoothService;
    private ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    /**
     * 去实验设置页面
     * @param event
     */
    @Subscribe
    public void onToExpeSettingsEvent(ToExpeSettingsEvent event){
        //判断是否已经连接设备
        if (mBluetoothService!=null && mBluetoothService.isConnected()){
            UserSettingsStep1Activity.start(getActivity(),event.getExperiment());
        }else {
            ToastUtil.showToast(getActivity(),"请先连接设备");
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unbindService(mBluetoothServiceConnection);
        EventBus.getDefault().unregister(this);
    }
}
