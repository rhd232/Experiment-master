package com.wind.base;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.wind.base.utils.SystemUiUtil;
import com.wind.umengsharelib.UmengActivity;
import com.wind.view.TitleBar;


/**
 * Created by wind on 2017/11/28.
 */

public class BaseActivity extends UmengActivity {

    protected TitleBar mTitleBar;

    public int getStatusBarColor(){
        return Color.parseColor("#1F4E99");
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemUiUtil.setStatusBarColor(this,getStatusBarColor());
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View titleBar=$(R.id.title_bar);
        mTitleBar= (TitleBar) titleBar;
        if (mTitleBar!=null) {
//            mTitleBar.setLeftIcon(R.drawable.icon_back_s);
            mTitleBar.setTextColor(Color.parseColor("#1a1a1a"));
            mTitleBar.setLineColor(getResources().getColor(R.color.colordbdbdb));
        }
        setTitle();
    }


    public <T extends View> T $(int resId){
        return (T)super.findViewById(resId);
    }



    public boolean isFinish() {
        return this == null || isFinishing();
    }

    public Activity getActivity() {
        return this;
    }

    protected void setTitle() {
    }


    public void replaceFragment(Fragment fragment){
        if (fragment!=null){
            FragmentManager fm=getSupportFragmentManager();
            FragmentTransaction transaction=fm.beginTransaction();

            transaction.replace(R.id.fl_fragment_container,fragment);
            transaction.commitAllowingStateLoss();
        }

    }

}
