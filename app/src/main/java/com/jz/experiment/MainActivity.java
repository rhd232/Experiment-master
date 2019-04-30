package com.jz.experiment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.jz.experiment.chart.CommData;
import com.jz.experiment.module.analyze.AnalyzeFragment;
import com.jz.experiment.module.bluetooth.CommunicationService;
import com.jz.experiment.module.data.ExpeDataTabFragment;
import com.jz.experiment.module.expe.HistoryExperimentsFragment;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.settings.event.LogoutEvent;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.DeviceProxyHelper;
import com.jz.experiment.util.TrimReader;
import com.jz.experiment.util.UsbManagerHelper;
import com.wind.base.BaseActivity;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Exiter;
import com.wind.base.utils.Navigator;
import com.wind.toastlib.ToastUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    public static final int TAB_INDEX_EXPE = 0;
    public static final int TAB_INDEX_DATA = 1;
    public static final int TAB_INDEX_ANALYZE = 2;
    @BindView(R.id.view_pager)
    ViewPager view_pager;
    MainPagerAdapter mAdapter;
    @BindView(R.id.layout_expe)
    View layout_expe;
    @BindView(R.id.layout_data)
    View layout_data;

    @BindView(R.id.layout_analyze)
    View layout_analyze;
    Fragment[] fragments;
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

        fragments = new Fragment[3];
        fragments[0] = new HistoryExperimentsFragment();
        fragments[1] = new ExpeDataTabFragment();
        fragments[2] = new AnalyzeFragment();

        mAdapter = new MainPagerAdapter(getSupportFragmentManager(), fragments);
        view_pager.setAdapter(mAdapter);
        view_pager.setOffscreenPageLimit(3);
        onViewClick(layout_expe);


       // startBluetoothService();

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
                if (!ActivityUtil.isFinish(getActivity())) {
                    //判断是否已经连接
                    CommunicationService service = DeviceProxyHelper.getInstance(getActivity())
                            .getCommunicationService();
                    if (!service.isConnected()) {
                        connectUsbDevice();
                    }else {
                        //读取trim，最好再缓存到本地文件，以便下次可以直接读取


                    }
                }

            }
        }, 1000);


        AndPermission.with(getActivity())
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        DataFileUtil.createAppFolder();
                        //读取dataposition文件
                        CommData.ReadDatapositionFile(getActivity());
                        //trim文件读取到CommonData中
                        TrimReader.getInstance().ReadTrimFile(getActivity());
                    }
                }).onDenied(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                ToastUtil.showToast(getActivity(), "拒绝访问sd卡权限将无法新建实验");
            }
        }).start();
    }

    private void connectUsbDevice() {
        UsbManagerHelper.connectUsbDevice(this);
    }

    /*Intent mServiceIntent;

    private void startBluetoothService() {
        mServiceIntent = new Intent(this, BluetoothService.class);
        startService(mServiceIntent);

    }*/


    @Override
    public int getStatusBarColor() {
        return getResources().getColor(R.color.color686868);
    }

    @OnClick({R.id.layout_expe, R.id.layout_data,R.id.layout_analyze})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.layout_expe:
                //检查是否有未保存的实验
               /* ExpeDataTabFragment expeDataTabFragment= (ExpeDataTabFragment) fragments[1];
                if (expeDataTabFragment.hasUnSaveExpe()){

                }*/

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
                    //TODO 下个版本刷新就行
                    //expeDataTabFragment.reload();
                    tab = null;
                }
                break;
            case R.id.layout_analyze:
                resetBottomBar();
                layout_analyze.setActivated(true);
                view_pager.setCurrentItem(TAB_INDEX_ANALYZE, false);
                break;

        }
    }

    private void resetBottomBar() {

        layout_expe.setActivated(false);
        layout_data.setActivated(false);
        layout_analyze.setActivated(false);


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
        DeviceProxyHelper.getInstance(getApplicationContext()).unbindService(getApplicationContext());
       // stopService(mServiceIntent);
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onBackPressed() {
        Exiter.exit2Click(getActivity());
    }

    private Tab tab;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        tab = Navigator.getParcelableExtra(this);
        if (tab==null){
            return;
        }
        if (tab.getIndex() == TAB_INDEX_DATA) {
            onViewClick(layout_data);
        } else if (tab.getIndex() == TAB_INDEX_EXPE) {
            tab = null;
            HistoryExperimentsFragment f = (HistoryExperimentsFragment) mAdapter.getItem(TAB_INDEX_EXPE);
            if (f != null) {
                f.loadData();
            }
            onViewClick(layout_expe);
        }
    }


}
