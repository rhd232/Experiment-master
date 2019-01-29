package com.jz.experiment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.jz.experiment.module.bluetooth.BluetoothService;
import com.jz.experiment.module.bluetooth.DeviceRepo;
import com.jz.experiment.module.data.ExpeDataTabFragment;
import com.jz.experiment.module.expe.HistoryExperimentsFragment;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.expe.bean.UsbDeviceInfo;
import com.jz.experiment.module.settings.event.LogoutEvent;
import com.jz.experiment.util.DeviceProxyHelper;
import com.wind.base.BaseActivity;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Navigator;
import com.wind.toastlib.ToastUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    public static final int TAB_INDEX_EXPE = 0;
    public static final int TAB_INDEX_DATA = 1;
    @BindView(R.id.view_pager)
    ViewPager view_pager;
    MainPagerAdapter mAdapter;
    @BindView(R.id.layout_expe)
    View layout_expe;
    @BindView(R.id.layout_data)
    View layout_data;

    public static void start(Context context) {
        Navigator.navigate(context, MainActivity.class);
    }

    public static void start(Context context, Tab tab) {

        Navigator.navigate(context, MainActivity.class, tab);


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);

        Fragment[] fragments = new Fragment[2];
        fragments[0] = new HistoryExperimentsFragment();
        fragments[1] = new ExpeDataTabFragment();

        mAdapter = new MainPagerAdapter(getSupportFragmentManager(), fragments);
        view_pager.setAdapter(mAdapter);
        onViewClick(layout_expe);


        startBluetoothService();
        view_pager.post(new Runnable() {
            @Override
            public void run() {
                //TODO bindService

            }
        });

       /* Config config=ConfigRepo.getInstance().get(getActivity());
        if (!TextUtils.isEmpty(config.getBluetoothDeviceAddress())){
            //自动连接设备
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter!=null && bluetoothAdapter.isEnabled()){

            }

        }*/
        DeviceProxyHelper.getInstance(getActivity());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                connectUsbDevice();
            }
        }, 1000);


        AndPermission.with(getActivity())
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                }).onDenied(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                ToastUtil.showToast(getActivity(),"拒绝访问sd卡权限将无法新建实验");
            }
                })
                .start();
    }

    private void connectUsbDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<String> printerList = new ArrayList<>();
        int printerCount = 0;//打印机台数
        String[] strDev = new String[usbManager.getDeviceList().size()];
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                UsbInterface usbInterface = device.getInterface(i);
                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_HID) {
                    printerCount++;
                    printerList.add(device.getDeviceName());
                }
            }
        }
        strDev = new String[printerCount];
        for (int i = 0; i < printerCount; i++) {
            strDev[i] = printerList.get(i);

        }

        if (printerCount == 1) {//默认为小票打印机
            for (UsbDevice device : usbManager.getDeviceList().values()) {
                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface usbInterface = device.getInterface(i);

                    UsbDeviceInfo info = new UsbDeviceInfo();
                    info.setDeviceName(device.getDeviceName());
                    DeviceRepo.getInstance().store(getActivity(), info);
                    DeviceProxyHelper.getInstance(getActivity()).getUsbService().connect(device.getDeviceName());
                    // PrinterHelper.getInstance(getActivity()).connectDevice(device.getDeviceName());
                    break;

                }
            }
        }
    }

    Intent mServiceIntent;

    private void startBluetoothService() {
        mServiceIntent = new Intent(this, BluetoothService.class);
        startService(mServiceIntent);

    }


    @Override
    public int getStatusBarColor() {
        return getResources().getColor(R.color.color686868);
    }

    @OnClick({R.id.layout_expe, R.id.layout_data})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.layout_expe:
                resetBottomBar();
                layout_expe.setActivated(true);
                view_pager.setCurrentItem(TAB_INDEX_EXPE, false);

                break;
            case R.id.layout_data:
                resetBottomBar();
                layout_data.setActivated(true);
                view_pager.setCurrentItem(TAB_INDEX_DATA, false);
                if (tab != null) {
                    ExpeDataTabFragment expeDataTabFragment = (ExpeDataTabFragment) mAdapter.getItem(TAB_INDEX_DATA);
                    expeDataTabFragment.setExpe(tab.getExtra());
                    tab = null;
                }
                break;

        }
    }

    private void resetBottomBar() {

        layout_expe.setActivated(false);
        layout_data.setActivated(false);


    }

    private class MainPagerAdapter extends FragmentPagerAdapter {

        Fragment[] fragments;

        public MainPagerAdapter(FragmentManager fm, Fragment[] fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }


    @Subscribe
    public void onLogoutEvent(LogoutEvent event) {
        ActivityUtil.finish(getActivity());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mServiceIntent);
        EventBus.getDefault().unregister(this);
    }


    private Tab tab;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        tab = Navigator.getParcelableExtra(this);
        if (tab.getIndex() == TAB_INDEX_DATA) {
            onViewClick(layout_data);
        }
    }


}
