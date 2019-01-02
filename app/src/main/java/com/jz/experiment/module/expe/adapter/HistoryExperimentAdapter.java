package com.jz.experiment.module.expe.adapter;

import android.app.Activity;

import com.jz.experiment.R;
import com.wind.base.adapter.BaseDelegateRecyclerAdapter;

public class HistoryExperimentAdapter extends BaseDelegateRecyclerAdapter {
    public HistoryExperimentAdapter(Activity activity) {
        super(activity);
    }

    @Override
    protected void addDelegate() {
        manager
                .addDelegate(new ExperimentAddDelegate(mActivity,R.layout.item_expe_add))
                .addDelegate(new HistoryExperimentDelegate(mActivity,R.layout.item_history_expe));
    }
}
