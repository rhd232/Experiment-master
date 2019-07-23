package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.anitoa.util.AnitoaLogUtil;
import com.jz.experiment.R;
import com.jz.experiment.module.expe.mvp.impl.UserSettingsStep2Fragment;
import com.wind.base.BaseActivity;
import com.wind.base.utils.Navigator;
import com.wind.data.expe.bean.HistoryExperiment;

public class UserSettingsStep2Activity extends BaseActivity {

    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, UserSettingsStep2Activity.class, experiment);
    }


    @Override
    protected void setTitle() {
        String title=getString(R.string.title_setup_2);
        mTitleBar.setTitle(title);
        mTitleBar.setRightIcon(R.drawable.icon_program_save);

        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.saveExpe();
            }
        });
    }
    private UserSettingsStep2Fragment mFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting_step2);
        HistoryExperiment historyExperiment = Navigator.getParcelableExtra(getActivity());
        mFragment=UserSettingsStep2Fragment.newInstance(historyExperiment);
        replaceFragment(mFragment);

        AnitoaLogUtil.writeFileLog("UserSettingsStep2Activity onCreate");
    }


    @Override
    public void onPause() {
        super.onPause();
        AnitoaLogUtil.writeFileLog("UserSettingsStep2Activity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        AnitoaLogUtil.writeFileLog("UserSettingsStep2Activity onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        AnitoaLogUtil.writeFileLog("UserSettingsStep2Activity onResume");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AnitoaLogUtil.writeFileLog("UserSettingsStep2Activity onConfigurationChanged");
    }
}
