package com.jz.experiment.module.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jz.experiment.R;
import com.jz.experiment.module.login.LoginActivity;
import com.jz.experiment.module.settings.event.LogoutEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.BaseActivity;
import com.wind.base.utils.ActivityUtil;
import com.wind.base.utils.Navigator;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserSettingsActivity extends BaseActivity {

    public static void start(Context context){
        Navigator.navigate(context,UserSettingsActivity.class);
    }

    @Override
    protected void setTitle() {
        mTitleBar.setTitle("用户设置");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        ButterKnife.bind(this);


    }


    @OnClick({R.id.tv_logout,R.id.rl_edit_pwd})
    public void onViewClick(View v){
        switch (v.getId()){
            case R.id.rl_edit_pwd:
                EditPwdActivity.start(getActivity());
                break;
            case R.id.tv_logout:
                AppDialogHelper.showNormalDialog(getActivity(),
                        "确定要退出吗？", new AppDialogHelper.DialogOperCallback() {
                            @Override
                            public void onDialogConfirmClick() {
                                EventBus.getDefault().post(new LogoutEvent());

                                LoginActivity.start(getActivity());
                                ActivityUtil.finish(getActivity());
                            }
                        });
                break;
        }
    }
}
