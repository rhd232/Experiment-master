package com.jz.experiment.module.expe.mvp.impl;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anitoa.Anitoa;
import com.anitoa.bean.Data;
import com.anitoa.listener.AnitoaConnectionListener;
import com.anitoa.receiver.BluetoothReceiver;
import com.anitoa.service.BluetoothService;
import com.jz.experiment.R;
import com.jz.experiment.module.expe.adapter.DeviceAdapter;
import com.jz.experiment.module.expe.event.ConnectRequestEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.bean.Config;
import com.wind.base.dialog.LoadingDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.repo.ConfigRepo;
import com.wind.base.utils.ActivityUtil;
import com.wind.data.expe.bean.DeviceInfo;
import com.wind.toastlib.ToastUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezy.ui.layout.LoadingLayout;

public class DeviceListFragment extends BaseFragment implements
        AnitoaConnectionListener {
    public static final String TAG = "Device";
    public static final int REQUEST_ENABLE_BT = 1212;
    @BindView(R.id.rv_devices)
    RecyclerView rv_devices;

    DeviceAdapter mAdapter;

    @BindView(R.id.layout_loading)
    LoadingLayout layout_loading;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothReceiver mBluetoothReceiver;

    @BindView(R.id.rl_connected)
    RelativeLayout rl_connected;
    @BindView(R.id.tv_connected_dev_name)
    TextView tv_connected_dev_name;

    @BindView(R.id.checkbox)
    ImageView checkbox;

    private Anitoa sAnitoa;
    private BluetoothService mBluetoothService;
    private BluetoothDevice mConnectedDevice;
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_device_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        rl_connected.setVisibility(View.GONE);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_devices.setLayoutManager(manager);
        mAdapter = new DeviceAdapter(getActivity(), R.layout.item_device);
        rv_devices.setAdapter(mAdapter);

        registerScanReceiver();
        mBluetoothReceiver = new BluetoothReceiver();
        mBluetoothReceiver.setBluetoothConnectInteface(this);
        getActivity().registerReceiver(mBluetoothReceiver,
                makeIntentFilter());

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //当前设备不支持蓝牙
            ToastUtil.showToast(getActivity(), "当前设备不支持蓝牙");
            ActivityUtil.finish(getActivity());
        } else {
            //检测是否已经打开蓝牙
            if (!mBluetoothAdapter.isEnabled()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });


            } else {
                checkBluetoothPermission();
            }



        }

        bindService();





        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkbox.isActivated()&& mBluetoothService.isConnected()){
                    AppDialogHelper.showNormalDialog(getActivity(), "您将断开与该设备的连接", new AppDialogHelper.DialogOperCallback() {
                        @Override
                        public void onDialogConfirmClick() {
                            mBluetoothService.cancel();
                            checkbox.setActivated(false);
                            rl_connected.setVisibility(View.GONE);
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.setDevice(mConnectedDevice);

                            deviceInfoSet.add(deviceInfo);
                            List<DeviceInfo> list = new ArrayList<>(deviceInfoSet);
                            mAdapter.replace(list);
                        }
                    });
                }
            }
        });


    }

    private void registerScanReceiver() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
       /*     //蓝牙连接成功
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            //蓝牙请求断开连接
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            //蓝牙已断开连接
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);*/

        getActivity().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    }

    private void bindService() {
        sAnitoa=Anitoa.getInstance(getActivity());
        mBluetoothService=sAnitoa.getBluetoothService();
        mConnectedDevice=mBluetoothService.getConnectedBluetoothDevice();
        if (mConnectedDevice!=null){
            checkbox.setActivated(true);
            rl_connected.setVisibility(View.VISIBLE);
            tv_connected_dev_name.setText(mConnectedDevice.getName());
        }else{
            //TODO 连接上次的设备
        }
    }



    /* private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.initialize();
           // 获取已经连接的设备
            mConnectedDevice=mBluetoothService.getConnectedDevice();
            if (mConnectedDevice!=null){
                checkbox.setActivated(true);
                rl_connected.setVisibility(View.VISIBLE);
                tv_connected_dev_name.setText(mConnectedDevice.getName());
            }else{
                //TODO 连接上次的设备
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService=null;
        }
    };*/

    private void checkBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            scanDevice(true);
        } else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            scanDevice(true);
                        }
                    }).onDenied(new Action<List<String>>() {
                @Override
                public void onAction(List<String> data) {
                    ToastUtil.showToast(getActivity(), "打开蓝牙权限才能继续使用");
                    getActivity().finish();
                }
            }).start();
        }
    }

    private Handler mHandler = new Handler();
    private boolean mScanning;
    public static final long SCAN_PERIOD = 10 * 1000;

    /**
     * 扫描蓝牙设备
     *
     * @param enable
     */
    private void scanDevice(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mScanning = false;
                        mBluetoothAdapter.cancelDiscovery();
                        layout_loading.showContent();
                    }
                }
            }, SCAN_PERIOD);
            mScanning = true;
            // F000E0FF-0451-4000-B000-000000000000
            deviceInfoSet.clear();
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            //mHandler.sendEmptyMessage(0);
            //mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描设备
            mBluetoothAdapter.startDiscovery();
        } else {
            mScanning = false;
            mBluetoothAdapter.cancelDiscovery();
        }
    }


    //蓝牙设备
    // private List<BluetoothDevice> liDevices = new ArrayList<>();
    private Set<DeviceInfo> deviceInfoSet = new HashSet<>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            //当扫描到设备的时候
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 获取设备对象
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                     /*   ParcelUuid parcelUuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
                        if (parcelUuid != null) {
                            String uuid = parcelUuid.getUuid().toString();
                            Log.e(TAG, "uuid:" + uuid);
                        }*/
                        //提取强度信息
                        // int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                        //Log.e(TAG, device.getName() + "\n" + device.getAddress() + "\n强度：" + rssi);
                        layout_loading.showContent();
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDevice(device);
                        if (mConnectedDevice!=null){
                            boolean eq=device.getAddress().equals(mConnectedDevice.getAddress());
                            if (eq){
                                return;
                            }
                        }

                        if (!deviceInfoSet.contains(deviceInfo)) {
                            deviceInfoSet.add(deviceInfo);
                            List<DeviceInfo> list = new ArrayList<>(deviceInfoSet);
                            mAdapter.replace(list);

                        }

                    }
                });

            } //搜索完成
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, "onReceive: 搜索完成");
                layout_loading.showContent();
                /*if (!deviceInfoSet.contains(mConnectedDevice)){
                    checkbox.setActivated(false);
                    rl_connected.setVisibility(View.GONE);
                    mConnectedDevice=null;
                }*/
            }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                Log.e(TAG, "onReceive: 蓝牙连接成功");
            }else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
                Log.e(TAG, "onReceive: 蓝牙请求断开连接");
            }else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                Log.e(TAG, "onReceive: 蓝牙已断开连接");
            }
        }
    };




    public static IntentFilter makeIntentFilter() { // 注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_DEVICE_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_DEVICE_CONNECT_FAILED);
        intentFilter
                .addAction(BluetoothService.ACTION_DEVICE_COMMUNICATION_ENABLED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        // intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                //蓝牙开启成功
                checkBluetoothPermission();
            }
        }
    }

    /**
     * 连接蓝牙设备
     *
     * @param event
     */
    @Subscribe
    public void onConnectRequestEvent(ConnectRequestEvent event) {

        DeviceInfo deviceInfo = event.getDeviceInfo();
        if (mBluetoothService!=null){
            LoadingDialogHelper.showOpLoading(getActivity());
            mBluetoothService.connect(deviceInfo.getDevice());
        }


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (mReceiver!=null) {
            getActivity().unregisterReceiver(mReceiver);
        }
        if (mBluetoothReceiver!=null) {
            getActivity().unregisterReceiver(mBluetoothReceiver);
        }
        if (mBluetoothAdapter!=null)
            scanDevice(false);
    }

    @Override
    public void onConnectSuccess() {

        ToastUtil.showToast(getActivity(),"连接成功");


        rl_connected.setVisibility(View.VISIBLE);
        mConnectedDevice=mBluetoothService.getConnectedBluetoothDevice();
        if (mConnectedDevice==null){
            return;
        }
        tv_connected_dev_name.setText(mConnectedDevice.getName());
        checkbox.setActivated(true);
        DeviceInfo deviceInfo=new DeviceInfo();
        deviceInfo.setDevice(mConnectedDevice);
        deviceInfoSet.remove(deviceInfo);
        List<DeviceInfo> list = new ArrayList<>(deviceInfoSet);
        mAdapter.replace(list);

        //TODO 保存最后连接的设置，下次进来自动连接该设备
        Config config=new Config();
        config.setBluetoothDeviceName(mConnectedDevice.getName());
        config.setBluetoothDeviceAddress(mConnectedDevice.getAddress());
        ConfigRepo.getInstance().store(getActivity(),config);
    }

    @Override
    public void onConnectCancel() {
        LoadingDialogHelper.hideOpLoading();
        AppDialogHelper.showSingleBtnDialog(getActivity(), "连接失败请重试", new AppDialogHelper.DialogOperCallback() {
            @Override
            public void onDialogConfirmClick() {

            }
        });

    }

    @Override
    public void onDoThing() {
        LoadingDialogHelper.hideOpLoading();
        //连接蓝牙设备成功，并且已经建立socket通信


    }

    @Override
    public void onReceivedData(Data data) {
        Log.e(TAG,"onReceivedData:"+data);
        //AT+BRSF=191
        //ToastUtil.showToast(getActivity(),"蓝牙设备发来了消息："+data);
    }



}
