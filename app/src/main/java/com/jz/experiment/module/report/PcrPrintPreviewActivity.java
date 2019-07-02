package com.jz.experiment.module.report;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jz.experiment.R;
import com.jz.experiment.module.report.bean.InputParams;
import com.wind.base.BaseActivity;
import com.wind.data.expe.bean.HistoryExperiment;

public class PcrPrintPreviewActivity extends BaseActivity {

    public static final String EXTRA_KEY_EXPE="extra_key_expe";
    public static final String EXTRA_KEY_PARAMS="extra_key_params";
    public static void start(Context context, HistoryExperiment experiment, InputParams params){

        Intent intent=new Intent(context,PcrPrintPreviewActivity.class);
        intent.putExtra(EXTRA_KEY_EXPE,experiment);
        intent.putExtra(EXTRA_KEY_PARAMS,params);
        context.startActivity(intent);
    }
    PcrPrintPreviewFragment f;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_with_toolbar);
        HistoryExperiment experiment=getIntent().getParcelableExtra(EXTRA_KEY_EXPE);
        InputParams params=getIntent().getParcelableExtra(EXTRA_KEY_PARAMS);
        f=PcrPrintPreviewFragment.newInstance(experiment,params);
        replaceFragment(f);
    }


    @Override
    protected void setTitle() {
        mTitleBar.setTitle(getString(R.string.print_preview));
        mTitleBar.setRightText(getString(R.string.print));
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f.print();
            }
        });
    }
}
