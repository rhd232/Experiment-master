package com.jz.experiment.module.expe.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

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
        mTitleBar.setTitle("用户设置2");
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


    }


}
