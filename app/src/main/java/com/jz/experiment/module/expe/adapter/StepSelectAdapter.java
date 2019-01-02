package com.jz.experiment.module.expe.adapter;

import android.content.Context;

import com.jz.experiment.R;
import com.wind.base.bean.PartStage;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class StepSelectAdapter extends QuickAdapter<PartStage> {
    public StepSelectAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, PartStage item) {

        helper.setText(R.id.tv_channel_materia,item.getStepName());
        helper.getView().setActivated(item.isTakePic());



    }
}
