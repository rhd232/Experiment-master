package com.jz.experiment.module.data.adapter;

import android.content.Context;
import android.graphics.Color;

import com.jz.experiment.R;
import com.jz.experiment.module.data.bean.SampleRow;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class TableUnknowAdapter extends QuickAdapter<SampleRow> {

    private int itemColor;
    public TableUnknowAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
        itemColor=context.getResources().getColor(R.color.color333333);
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

            helper.setTextColor(R.id.tv_name, Color.WHITE);
            helper.setTextColor(R.id.tv_concentration, Color.WHITE);
            helper.setTextColor(R.id.tv_ct, Color.WHITE);
        } else {

            name = item.getName();
            type = item.getType();
            concentration = item.getConcentration();
            ct = item.getCtValue();

            helper.setTextColor(R.id.tv_name, itemColor);
            helper.setTextColor(R.id.tv_concentration, itemColor);
            helper.setTextColor(R.id.tv_ct, itemColor);
        }

        if (helper.getPosition()!=0){
            if (helper.getPosition()%2==0){
                //helper.getView().setBackgroundResource(R.drawable.shape_solidf2bc00);
            }else {
                //helper.getView().setBackgroundResource(R.drawable.shape_solid1f4e99);
            }
            helper.getView().setBackgroundColor(Color.WHITE);
        }

        helper.setText(R.id.tv_name,name);
        helper.setText(R.id.tv_concentration,concentration);
        helper.setText(R.id.tv_ct,ct);



    }
}
