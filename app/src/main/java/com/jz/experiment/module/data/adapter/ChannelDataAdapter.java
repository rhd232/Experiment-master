package com.jz.experiment.module.data.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.jz.experiment.R;
import com.wind.data.expe.bean.ChannelData;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class ChannelDataAdapter extends QuickAdapter<ChannelData> {

    public ChannelDataAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, ChannelData item) {

        TextView tv= helper.getView(R.id.tv);
        int position=helper.getPosition();
        int lineNumber=position/9;
        boolean first=position % 9 == 0;
        if (lineNumber==0){
            tv.setBackgroundColor(context.getResources().getColor(R.color.colorA5B8D6));
        }else {
            tv.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
        if (first){
            tv.setText(item.getChannelName());
        }
        if (lineNumber==0 && !first){
           //int pos=item.getSampleIndex();
            String alias=item.getSampleIndexAlias();
            if (TextUtils.isEmpty(alias)){
                alias=item.getSampleIndex()+"";
            }
            tv.setText(alias);
        }
        if (lineNumber>0 && !first){
            tv.setText(item.getSampleVal());
        }
    }
}
