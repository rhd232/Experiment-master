package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.mvp.impl.UserSettingsStep1Fragment;
import com.wind.base.BaseActivity;
import com.wind.base.utils.Navigator;
import com.wind.data.expe.bean.HistoryExperiment;

public class UserSettingsStep1Activity extends BaseActivity {

    public static void start(Context context) {
        Navigator.navigate(context, UserSettingsStep1Activity.class);
    }

    public static void start(Context context, HistoryExperiment experiment) {
        Navigator.navigate(context, UserSettingsStep1Activity.class, experiment);
    }


    @Override
    protected void setTitle() {
        String title=getString(R.string.title_setup_1);
        mTitleBar.setTitle(title);
        mTitleBar.setRightIcon(R.drawable.icon_history_data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting_step1);

        HistoryExperiment experiment = Navigator.getParcelableExtra(this);
        UserSettingsStep1Fragment f= UserSettingsStep1Fragment.newInstance(experiment);
        replaceFragment(f);
    }




}
