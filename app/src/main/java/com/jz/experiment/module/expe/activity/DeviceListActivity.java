package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.mvp.impl.DeviceListFragment;
import com.wind.base.BaseActivity;
import com.wind.base.utils.Navigator;

public class DeviceListActivity extends BaseActivity {

    public static void start(Context context){
        Navigator.navigate(context,DeviceListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_with_toolbar);

        replaceFragment(new DeviceListFragment());

    }

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("连接设备");
    }
}
