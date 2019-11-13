package com.jz.experiment.module.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.anitoa.util.AnitoaLogUtil;
import com.jz.experiment.R;
import com.jz.experiment.module.data.FilterActivity;
import com.jz.experiment.module.login.LoginActivity;
import com.jz.experiment.module.settings.event.LogoutEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.mvp.view.BaseFragment;
import com.wind.base.utils.ActivityUtil;
import com.wind.view.TitleBar;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;
public class MineFragment extends BaseFragment {
    TitleBar mTitleBar;
    @Override
    protected int getLayoutRes() {
        return R.layout.activity_user_settings;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mTitleBar = view.findViewById(R.id.title_bar);
        initTitleBar();
        AnitoaLogUtil.writeFileLog("MineFragment onViewCreated");
    }

    private void initTitleBar() {
        mTitleBar.setTextColor(Color.BLACK);
        mTitleBar.setLeftVisibility(View.GONE);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.white));
        String title = getString(R.string.title_mine);
        mTitleBar.setTitle(title);
    }
    @OnClick({R.id.tv_logout,R.id.rl_edit_pwd})
    public void onViewClick(View v){
        switch (v.getId()){
            case R.id.rl_edit_pwd:
                EditPwdActivity.start(getActivity());
                break;
            case R.id.tv_logout:
                String msg=getString(R.string.user_settings_dialog_msg);
                AppDialogHelper.showNormalDialog(getActivity(),
                        msg, new AppDialogHelper.DialogOperCallback() {
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
