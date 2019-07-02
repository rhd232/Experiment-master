package com.jz.experiment.module.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.jz.experiment.R;
import com.wind.base.BaseActivity;

public class ReportPreviewActivity extends BaseActivity {

    public static void start(Context context){
        Intent intent=new Intent(context,ReportPreviewActivity.class);
        context.startActivity(intent);

    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_with_toolbar);
        replaceFragment(new ReportPreviewFragment());
    }


    @Override
    protected void setTitle() {
        mTitleBar.setTitle("报告预览");
    }
}
