package com.jz.experiment.module.expe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.jz.experiment.R;
import com.wind.data.expe.bean.Channel;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class ChannelAdapter extends QuickAdapter<Channel> {
    private int mItemHeight;
    public ChannelAdapter(Context context, int layoutResId,int itemHeight) {
        super(context, layoutResId);
        mItemHeight=itemHeight;
    }

    @Override
    protected void convert(BaseAdapterHelper helper, Channel item) {

        ViewGroup.LayoutParams params=helper.getView().getLayoutParams();
        params.height=mItemHeight;

        helper.setText(R.id.tv_channel_name,item.getName());
        StringBuilder sBuilder=new StringBuilder();
        if (!TextUtils.isEmpty(item.getValue())){
            sBuilder.append(item.getValue());
        }

        if (!TextUtils.isEmpty(item.getRemark())){
            sBuilder.append("-").append(item.getRemark());
        }
        helper.setText(R.id.tv_channel_value,sBuilder.toString());

        helper.getView().setEnabled(item.isEnabled());
    }
}
