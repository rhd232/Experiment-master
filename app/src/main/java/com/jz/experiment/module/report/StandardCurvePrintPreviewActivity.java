package com.jz.experiment.module.report;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jz.experiment.R;
import com.jz.experiment.module.report.bean.StdLineData;
import com.wind.base.BaseActivity;

public class StandardCurvePrintPreviewActivity extends BaseActivity {
    public static final String EXTRA_KEY_DATA="extra_key_data";
    public static void start(Context context, StdLineData data){
        Intent intent=new Intent(context,StandardCurvePrintPreviewActivity.class);
        intent.putExtra(EXTRA_KEY_DATA,data);
        context.startActivity(intent);
    }
    StdCurvePrintPreviewFragment mStdCurvePrintPreviewFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_with_toolbar);
        StdLineData data= (StdLineData) getIntent().getSerializableExtra(EXTRA_KEY_DATA);

        replaceFragment(mStdCurvePrintPreviewFragment=StdCurvePrintPreviewFragment.newInstance(data));

    }


    @Override
    protected void setTitle() {

        mTitleBar.setTitle(getString(R.string.print_preview));
        mTitleBar.setRightText(getString(R.string.print));
        mTitleBar.getRightView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStdCurvePrintPreviewFragment.print();
            }
        });
    }
}
