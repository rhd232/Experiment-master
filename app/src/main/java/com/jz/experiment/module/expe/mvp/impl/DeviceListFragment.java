package com.jz.experiment.module.expe.mvp.impl;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jz.experiment.R;
import com.jz.experiment.module.bluetooth.BluetoothConnectionListener;
import com.jz.experiment.module.bluetooth.BluetoothLeService;
import com.jz.experiment.module.bluetooth.GattupdateReceiver;
import com.jz.experiment.module.expe.adapter.DeviceAdapter;
import com.wind.data.expe.bean.DeviceInfo;
import com.jz.experiment.module.expe.event.ConnectRequestEvent;
import com.wind.base.utils.LogUtil;
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

public class DeviceListFragment extends Fragment implements BluetoothConnectionListener {
    public static final String TAG="Device";
    public static final int REQUEST_ENABLE_BT=1212;
    @BindView(R.id.rv_devices)
    RecyclerView rv_devices;

    DeviceAdapter mAdapter;

    @BindView(R.id.layout_loading)
    LoadingLayout layout_loading;

    BluetoothAdapter mBluetoothAdapter;
    GattupdateReceiver mGattUpdateReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        return view;
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
        //mAdapter.replace(deviceInfoList);

        BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //检测是否已经打开蓝牙
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            });

           // mBluetoothAdapter.enable();
        }else {
            checkBluetoothPermission();
        }


        Intent intent=new Intent(getActivity(),BluetoothLeService.class);
        getActivity().bindService(intent,mConnection,Context.BIND_AUTO_CREATE);

        mGattUpdateReceiver = new GattupdateReceiver();
        mGattUpdateReceiver.setBluetoothConnetinteface(this);
        getActivity().registerReceiver(mGattUpdateReceiver,
                makeGattUpdateIntentFilter());


    }


    private void checkBluetoothPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            scanLeDevice(true);
        }else {
            AndPermission.with(this)
                    .runtime()
                    .permission( Manifest.permission.ACCESS_COARSE_LOCATION)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            scanLeDevice(true);
                        }
                    }).onDenied(new Action<List<String>>() {
                @Override
                public void onAction(List<String> data) {
                    ToastUtil.showToast(getActivity(),"打开蓝牙权限才能继续使用");
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
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        layout_loading.showContent();
                    }
                }
            }, SCAN_PERIOD);
            mScanning = true;
            // F000E0FF-0451-4000-B000-000000000000
            deviceInfoSet.clear();
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            mHandler.sendEmptyMessage(0);
            mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描设备
        } else {
            Log.d("SacanLeDevice", "4 step  stop  Sacan device");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }




    //蓝牙设备
   // private List<BluetoothDevice> liDevices = new ArrayList<>();
    private Set<DeviceInfo> deviceInfoSet= new HashSet<>();
    private BluetoothDevice mPlayerBluetoothDevice;

    // 扫描蓝牙设备的回调
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

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


                    //

                    // mHandler.sendEmptyMessage(1);
                }
            });
        }
    };

    public static IntentFilter makeGattUpdateIntentFilter() { // 注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }


    /*********蓝牙相关操作************************/
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
    public void onReceivedData(String data) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_ENABLE_BT){
            if (requestCode== Activity.RESULT_OK){
                //蓝牙开启成功
                checkBluetoothPermission();
            }
        }
    }

    /**
     * 连接蓝牙设备
     * @param event
     */
    @Subscribe
    public void onConnectRequestEvent(ConnectRequestEvent event){
        String addr=event.getDeviceInfo().getAddress();
        if (mBinded){
            boolean flag=mBluetoothLeService.connect(addr);
            LogUtil.e(TAG,"connect:"+flag);
        }



    }



    private BluetoothLeService mBluetoothLeService;
    private boolean mBinded;
    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder binder= (BluetoothLeService.LocalBinder) service;
            mBluetoothLeService=binder.getService();
            mBluetoothLeService.initialize();
            mBinded=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinded=false;
        }
    };
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        scanLeDevice(false);
    }

}
