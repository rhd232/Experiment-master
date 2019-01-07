package com.jz.experiment.module.expe.mvp.impl;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.jz.experiment.R;
import com.jz.experiment.module.bluetooth.BluetoothReceiver;
import com.jz.experiment.module.bluetooth.BluetoothService;
import com.jz.experiment.module.bluetooth.ble.BluetoothConnectionListener;
import com.jz.experiment.module.expe.adapter.DeviceAdapter;
import com.jz.experiment.module.expe.event.ConnectRequestEvent;
import com.wind.base.mvp.view.BaseFragment;
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

public class DeviceListFragment extends BaseFragment implements BluetoothConnectionListener {
    public static final String TAG = "Device";
    public static final int REQUEST_ENABLE_BT = 1212;
    @BindView(R.id.rv_devices)
    RecyclerView rv_devices;

    DeviceAdapter mAdapter;

    @BindView(R.id.layout_loading)
    LoadingLayout layout_loading;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothReceiver mBluetoothReceiver;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_device_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_devices.setLayoutManager(manager);
        mAdapter = new DeviceAdapter(getActivity(), R.layout.item_device);
        rv_devices.setAdapter(mAdapter);


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

                // mBluetoothAdapter.enable();
            } else {
                checkBluetoothPermission();
            }

            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            //蓝牙连接成功
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            //蓝牙请求断开连接
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            //蓝牙已断开连接
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

            getActivity().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        }


        Intent intent = new Intent(getActivity(), BluetoothService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mBluetoothReceiver = new BluetoothReceiver();
        mBluetoothReceiver.setBluetoothConnectInteface(this);
        getActivity().registerReceiver(mBluetoothReceiver,
                makeIntentFilter());


    }

    private BluetoothService mBluetoothService;
    private boolean mBinded;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.initialize();
            mBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinded = false;
        }
    };

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
                        device.fetchUuidsWithSdp();
                        ParcelUuid parcelUuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
                        if (parcelUuid != null) {
                            String uuid = parcelUuid.getUuid().toString();
                            Log.e(TAG, "uuid:" + uuid);
                        }
                        //提取强度信息
                        // int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                        //Log.e(TAG, device.getName() + "\n" + device.getAddress() + "\n强度：" + rssi);
                        layout_loading.showContent();
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDevice(device);
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
            }
        }
    };


    // 扫描蓝牙设备的回调
    /*private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             final byte[] scanRecord) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layout_loading.showContent();
                    Log.d("SacanLeDevice", "2 step  add device");
                    DeviceInfo deviceInfo=new DeviceInfo();
                    deviceInfo.setName(device.getName());
                    deviceInfo.setAddress(device.getAddress());
                    if (!deviceInfoSet.contains(deviceInfo)){
                        deviceInfoSet.add(deviceInfo);
                        List<DeviceInfo> list=new ArrayList<>(deviceInfoSet);
                        mAdapter.replace(list);

                        if ("DingDing".equals(device.getName())){
                            mPlayerBluetoothDevice=device;
                        }
                    }
                }
            });
        }
    };*/

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
            if (requestCode == Activity.RESULT_OK) {
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
        if (mBinded){
            mBluetoothService.connect(deviceInfo.getDevice());
        }


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(mReceiver);
        getActivity().unregisterReceiver(mBluetoothReceiver);
        scanDevice(false);
    }

    @Override
    public void onConnectSuccess() {
        ToastUtil.showToast(getActivity(),"连接成功");
    }

    @Override
    public void onConnectCancel() {
        ToastUtil.showToast(getActivity(),"连接失败");
    }

    @Override
    public void onDoThing() {
        ToastUtil.showToast(getActivity(),"可以进行写操作了");
    }

    @Override
    public void onReceivedData(String data) {
        Log.e(TAG,"onReceivedData:"+data);
        //AT+BRSF=191
        ToastUtil.showToast(getActivity(),"蓝牙设备发来了消息："+data);
    }
}
