package com.jz.experiment.module.expe.adapter;

import android.content.Context;

import com.jz.experiment.R;
import com.wind.data.expe.bean.ChannelMaterial;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class ChannelMaterialAdapter extends QuickAdapter<ChannelMaterial> {
    public ChannelMaterialAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, ChannelMaterial item) {

        helper.setText(R.id.tv_channel_materia,item.getName());
        helper.getView().setActivated(item.isSelected());



    }
}
