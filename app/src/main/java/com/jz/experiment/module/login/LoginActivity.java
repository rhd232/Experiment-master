package com.jz.experiment.module.login;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jz.experiment.R;
import com.wind.base.BaseActivity;
import com.wind.base.utils.Navigator;

public class LoginActivity extends BaseActivity{
    public static void start(Context context){
        Navigator.navigate(context,LoginActivity.class);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        replaceFragment(new LoginFragment());
    }


    @Override
    public int getStatusBarColor() {
        return Color.WHITE;
    }
}
