package com.jz.experiment.module.expe.adapter;

import android.app.Activity;

import com.jz.experiment.R;
import com.wind.base.adapter.BaseDelegateRecyclerAdapter;
import com.wind.data.expe.bean.HistoryExperiment;

public class HistoryExperimentAdapter extends BaseDelegateRecyclerAdapter {
    public HistoryExperimentAdapter(Activity activity) {
        super(activity);
    }
    private HistoryExperimentDelegate mHistoryExperimentDelegate;
    @Override
    protected void addDelegate() {
        manager
                .addDelegate(new ExperimentAddDelegate(mActivity,R.layout.item_expe_add))
                .addDelegate(mHistoryExperimentDelegate=new HistoryExperimentDelegate(mActivity,R.layout.item_history_expe,this));
    }




    public HistoryExperiment getLongClingItemData() {

       return (HistoryExperiment) getItem(mHistoryExperimentDelegate.getContextMenuPosition());
    }
}
