package com.jz.experiment.module.data.adapter;

import android.content.Context;
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
        int resId =R.drawable.shape_circle666666;
        switch (item.getStatus()) {
            case Sample.CODE_DEFAULT:
                resId =R.drawable.shape_circle666666;
                break;
            case Sample.CODE_STANDARD:
                resId =R.drawable.shape_circleblue;
                break;
            case Sample.CODE_UNKWON:
                resId =R.drawable.shape_circleyellow;
                break;
            default:
                resId =R.drawable.shape_circle666666;
                break;
        }
        tv.setBackgroundResource(resId);

        //tv.setActivated(item.isSelected());

    }


}
