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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.anitoa.Anitoa;
import com.anitoa.event.AnitoaConnectedEvent;
import com.anitoa.service.CommunicationService;
import com.anitoa.util.AnitoaLogUtil;
import com.jz.experiment.module.analyze.AnalyzeFragment;
import com.jz.experiment.module.data.ExpeDataTabFragment;
import com.jz.experiment.module.expe.HistoryExperimentsFragment;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.settings.event.LogoutEvent;
import com.jz.experiment.util.DataFileUtil;
import com.jz.experiment.util.UsbManagerHelper;
import com.jz.experiment.widget.DeviceStateBar;
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
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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
    @BindView(R.id.main_device_state_bar)
    DeviceStateBar main_device_state_bar;
    @BindView(R.id.layout_analyze)
    View layout_analyze;
    Fragment[] fragments;

    public static void start(Context context) {
        Navigator.navigate(context, MainActivity.class);
    }

    public static void start(Context context, Tab tab) {
        Navigator.navigate(context, MainActivity.class, tab);
    }

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);

        mOnSaveInstanceStateCalled=false;
        fragments = new Fragment[3];
        fragments[0] = new HistoryExperimentsFragment();
        fragments[1] = new ExpeDataTabFragment();
        fragments[2] = new AnalyzeFragment();

        mAdapter = new MainPagerAdapter(getSupportFragmentManager(), fragments);
        view_pager.setAdapter(mAdapter);
        view_pager.setOffscreenPageLimit(3);
        onViewClick(layout_expe);


        Anitoa.getInstance(getActivity());

        mHandler.postDelayed(mConnectRunnable, 500);

        checkStoragePermission();

        AnitoaLogUtil.writeFileLog("MainActivity onCreate");

    }



    private Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {
            tryConnectDevice();
        }
    };
    private int mTryConnectCount;

    private void tryConnectDevice() {
        if (!ActivityUtil.isFinish(getActivity())) {
            //判断是否已经连接
            mTryConnectCount++;
            CommunicationService service = Anitoa.getInstance(getActivity())
                    .getCommunicationService();
            if (service != null) {
                if (!service.isConnected()) {
                    connectUsbDevice();
                } else {
                    AnitoaConnectedEvent event = new AnitoaConnectedEvent(service.getConnectedDevice().getDeviceName());
                    main_device_state_bar.onBluetoothConnectedEvent(event);
                }
            } else {
                if (mTryConnectCount <= 3)
                    mHandler.postDelayed(mConnectRunnable, 500);
            }
        }
    }

    private void checkStoragePermission() {
        AndPermission.with(getActivity())
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                        createAppFolderAsync();
                        //读取dataposition文件
                        // CommData.ReadDatapositionFile(getActivity());
                        //trim文件读取到CommonData中
                        //TrimReader.getInstance().ReadTrimFile(getActivity());
                    }
                }).onDenied(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                ToastUtil.showToast(getActivity(), getString(R.string.tip_sd_permission));
            }
        }).start();
    }

    private void createAppFolderAsync() {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                DataFileUtil.createAppFolder();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {

                    }
                });
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

    @OnClick({R.id.layout_expe, R.id.layout_data, R.id.layout_analyze})
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
                ExpeDataTabFragment expeDataTabFragment = (ExpeDataTabFragment) fragments[1];
                expeDataTabFragment.reloadIfNeeded();
                /*if (tab != null) {
                    ExpeDataTabFragment expeDataTabFragment = (ExpeDataTabFragment)
                            mAdapter.getItem(TAB_INDEX_DATA);
                    *//*expeDataTabFragment.setExpe(tab.getExtra());*//*
                    //直接刷新就行
                    expeDataTabFragment.reload();
                    tab = null;
                }*/
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

    private boolean mOnSaveInstanceStateCalled;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mOnSaveInstanceStateCalled=true;
        super.onSaveInstanceState(outState);
        AnitoaLogUtil.writeFileLog("MainActivity onSaveInstanceState");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AnitoaLogUtil.writeFileLog("MainActivity onDestroy");
        //引起bug ExpeRunActivity 运行时MainActivity可能会被系统回收，导致usb断开连接。 ExpeRunActivity不更新的情况
      /*  if (!mOnSaveInstanceStateCalled) {
            AnitoaLogUtil.writeFileLog("MainActivity onDestroy unbindService");
            Anitoa.getInstance(getApplicationContext()).unbindService(getApplicationContext());
        }*/

      /*  CommunicationService communicationService=Anitoa.getInstance(getApplicationContext()).getCommunicationService();
        if (communicationService!=null) {
            communicationService.stopReadThread();
        }*/
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onBackPressed() {
        Exiter.exit2Click(getActivity());
    }

    private Tab tab;


    private long time;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        boolean lt=System.currentTimeMillis()-time<1000;
        if (lt){
            return;
        }
        time=System.currentTimeMillis();

        tab = Navigator.getParcelableExtra(this);
        AnitoaLogUtil.writeFileLog("MainActivity onNewIntent tab==null?"+(tab==null));
        if (tab == null) {
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


    public void CreateMenu(Menu menu) {

        menu.add(getString(R.string.op_del));
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        HistoryExperimentsFragment f = (HistoryExperimentsFragment) fragments[0];

        f.doItemLongClick();

        return super.onContextItemSelected(item);
    }
}
