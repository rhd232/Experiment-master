package com.jz.experiment.module.expe.adapter;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jz.experiment.R;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;
import com.wind.data.expe.bean.Sample;

import rx.functions.Action1;

public class SampleAdapter extends QuickAdapter<Sample> {
    public SampleAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
    }

    @Override
    protected void convert(BaseAdapterHelper helper,final Sample item) {
        helper.setText(R.id.tv_sample_pos,(helper.getPosition()+1)+"");
        helper.setText(R.id.tv_sample_name,item.getName());

        View tv_diseabled=helper.getView(R.id.tv_diseabled);
        View tv_sample_name=helper.getView(R.id.tv_sample_name);
        EditText et=helper.getView(R.id.tv_sample_name);
        RxTextView.textChanges(et).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                item.setName(charSequence.toString());
            }
        });
        if (item.isEnabled()){
            tv_diseabled.setVisibility(View.GONE);
            tv_sample_name.setVisibility(View.VISIBLE);
        }else {
            tv_diseabled.setVisibility(View.VISIBLE);
            tv_sample_name.setVisibility(View.GONE);
        }

        helper.getView().setEnabled(item.isEnabled());

    }
}
