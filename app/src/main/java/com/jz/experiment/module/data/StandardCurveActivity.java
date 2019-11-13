package com.jz.experiment.module.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jz.experiment.R;
import com.jz.experiment.module.report.bean.InputParams;
import com.wind.base.BaseActivity;
import com.wind.data.expe.bean.HistoryExperiment;


public class StandardCurveActivity extends BaseActivity {

    public static void start(Context context,HistoryExperiment experiment,InputParams inputParams){
        Intent intent=new Intent(context,StandardCurveActivity.class);
        intent.putExtra(EXTRA_KEY_EXPE,experiment);
        intent.putExtra(EXTRA_KEY_INPUT_PARAMS,inputParams);
        context.startActivity(intent);
    }
    public static final String EXTRA_KEY_EXPE="extra_key_expe";
    public static final String EXTRA_KEY_INPUT_PARAMS="extra_key_input_params";

    private StandardCurveFragment mStandardCurveFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_with_toolbar);
        HistoryExperiment experiment=getIntent().getParcelableExtra(EXTRA_KEY_EXPE);
        InputParams inputParams=getIntent().getParcelableExtra(EXTRA_KEY_INPUT_PARAMS);
        replaceFragment(mStandardCurveFragment=StandardCurveFragment.newInstance(experiment,inputParams));
    }

    @Override
    protected void setTitle() {
        mTitleBar.setTitle(getString(R.string.standard_curve));
//        mTitleBar.setRightText(getString(R.string.print));
        mTitleBar.setRightIcon(R.drawable.btn_selector_print);
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStandardCurveFragment.toPrintPreview();

            }
        });
    }

}
