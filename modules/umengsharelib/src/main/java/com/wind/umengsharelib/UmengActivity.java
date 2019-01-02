package com.wind.umengsharelib;

import android.support.v4.app.FragmentActivity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by wind on 2018/3/29.
 * 非常重要：必须调用 MobclickAgent.onResume() 和MobclickAgent.onPause()方法，
 * 才能够保证获取正确的新增用户、活跃用户、启动次数、使用时长等基本数据。
 */
public class UmengActivity extends FragmentActivity {

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
