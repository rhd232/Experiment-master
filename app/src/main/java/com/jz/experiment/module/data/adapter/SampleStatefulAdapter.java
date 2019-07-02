package com.jz.experiment.module.data.adapter;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.jz.experiment.R;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;
import com.wind.data.expe.bean.Sample;

public class SampleStatefulAdapter extends QuickAdapter<Sample> {
    public SampleStatefulAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, Sample item) {
        TextView tv = (TextView) helper.getView(R.id.tv_sample);
        TextView tv_seq = (TextView) helper.getView(R.id.tv_seq);
        tv_seq.setText((helper.getPosition()+1)+"");
        int resId;
        tv_seq.setTextColor(context.getResources().getColor(R.color.color686868));
        switch (item.getStatus()) {
            case Sample.CODE_DEFAULT:
                resId =R.drawable.shape_circlef8f8f8;
                break;
            case Sample.CODE_STANDARD:
                resId =R.drawable.shape_circlef2bc00;
                tv_seq.setTextColor(Color.WHITE);
                break;
            case Sample.CODE_UNKWON:
                resId =R.drawable.shape_circleblue;
                tv_seq.setTextColor(Color.WHITE);
                break;
            default:
                resId =R.drawable.shape_circlef8f8f8;
                break;
        }
        tv.setBackgroundResource(resId);



        //tv.setActivated(item.isSelected());

    }


}
