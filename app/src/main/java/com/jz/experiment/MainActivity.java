package com.jz.experiment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.jz.experiment.module.bluetooth.BluetoothService;
import com.jz.experiment.module.data.ExpeDataTabFragment;
import com.jz.experiment.module.expe.HistoryExperimentsFragment;
import com.jz.experiment.module.expe.bean.Tab;
import com.jz.experiment.module.settings.event.LogoutEvent;
import com.wind.base.BaseActivity;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Navigator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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

    public static void start(Context context){
        Navigator.navigate(context,MainActivity.class);
    }
    public static void start(Context context, Tab tab){

        Navigator.navigate(context,MainActivity.class,tab);


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
    }

    Intent mServiceIntent;
    private void startBluetoothService() {
        mServiceIntent=new Intent(this,BluetoothService.class);
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
                if (tab!=null){
                    ExpeDataTabFragment expeDataTabFragment= (ExpeDataTabFragment) mAdapter.getItem(TAB_INDEX_DATA);
                    expeDataTabFragment.setExpe(tab.getExtra());
                    tab=null;
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
    public void onLogoutEvent(LogoutEvent event){
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
        tab=Navigator.getParcelableExtra(this);
        if (tab.getIndex()==TAB_INDEX_DATA){
            onViewClick(layout_data);
        }
    }



}
