package com.jz.experiment.module.data.adapter;

import android.content.Context;

import com.jz.experiment.R;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class SeqAdapter extends QuickAdapter<String> {

    public SeqAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, String item) {
        helper.setText(R.id.tv_seq,item);
    }
}
