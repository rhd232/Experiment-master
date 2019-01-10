package com.jz.experiment.module.data.adapter;

import android.content.Context;
import android.widget.TextView;

import com.jz.experiment.module.data.StringSelectable;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class StringSelectableAdapter extends QuickAdapter<StringSelectable> {
    public StringSelectableAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, StringSelectable item) {
        TextView tv= (TextView) helper.getView();
        tv.setText(item.getVal());

        tv.setActivated(item.isSelected());
    }



    public List<StringSelectable> getSelectedList(){
        List<StringSelectable> selectedList=new ArrayList<>();
        for (int i=0;i<getCount();i++){
            StringSelectable selectable=getItem(i);
            if (selectable .isSelected()){
                selectedList.add(selectable);
            }
        }
        return selectedList;
    }
}
