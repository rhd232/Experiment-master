package com.jz.experiment.module.data.adapter;

import android.content.Context;

import com.jz.experiment.R;
import com.jz.experiment.module.data.bean.SampleRow;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class TableUnknowAdapter extends QuickAdapter<SampleRow> {

    public TableUnknowAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, SampleRow item) {
        String name;
        String type;
        String concentration;
        String ct;
        if (helper.getPosition() == 0) {
            helper.getView().setBackgroundResource(R.drawable.shape_solid1f4e99);
            name = context.getString(R.string.standard_sample);
            type = context.getString(R.string.standard_type);
            concentration = context.getString(R.string.standard_concentration);
            ct = context.getString(R.string.standard_ct);
        } else {
            name = item.getName();
            type = item.getType();
            concentration = item.getConcentration();
            ct = item.getCtValue();
        }

        if (helper.getPosition()!=0){
            if (helper.getPosition()%2==0){
                helper.getView().setBackgroundResource(R.drawable.shape_solidf6f6f6);
            }else {
                helper.getView().setBackgroundResource(R.drawable.shape_solida5b8d6);
            }
        }
        helper.setText(R.id.tv_name,name);
        helper.setText(R.id.tv_concentration,concentration);
        helper.setText(R.id.tv_ct,ct);



    }
}
