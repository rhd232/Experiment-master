package com.jz.experiment.module.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jz.experiment.R;
import com.wind.base.BaseActivity;
import com.wind.data.expe.bean.HistoryExperiment;

public class StandardCurveActivity extends BaseActivity {
    public static void start(Context context,HistoryExperiment experiment){
        Intent intent=new Intent(context,StandardCurveActivity.class);
        intent.putExtra(EXTRA_KEY_EXPE,experiment);
        context.startActivity(intent);
    }
    public static final String EXTRA_KEY_EXPE="extra_key_expe";

    private StandardCurveFragment mStandardCurveFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_with_toolbar);
        HistoryExperiment experiment=getIntent().getParcelableExtra(EXTRA_KEY_EXPE);
        replaceFragment(mStandardCurveFragment=StandardCurveFragment.newInstance(experiment));

    }

    @Override
    protected void setTitle() {
        mTitleBar.setTitle(getString(R.string.standard_curve));

        mTitleBar.setRightText(getString(R.string.print));

        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStandardCurveFragment.toPrintPreview();

            }
        });
    }
}
