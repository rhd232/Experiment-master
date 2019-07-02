package com.jz.experiment.module.data.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.jz.experiment.R;
import com.jz.experiment.module.data.bean.SampleRow;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;

public class TableAdapter extends QuickAdapter<SampleRow> implements View.OnFocusChangeListener,
View.OnTouchListener{

    private int itemColor;
    public TableAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
        itemColor=context.getResources().getColor(R.color.color333333);
    }

    @Override
    protected void convert(BaseAdapterHelper helper, SampleRow item) {
        String name;
        String type;
        String concentration;
        String ct;
        EditText et=helper.getView(R.id.et_concentration);
        et.setTag(helper.getPosition());
        et.setOnTouchListener(this); // 正确写法
        et.setOnFocusChangeListener(this);

        if (helper.getPosition() == 0) {
            helper.getView().setBackgroundResource(R.drawable.shape_solidf2bc00);
            helper.getView(R.id.et_concentration).setVisibility(View.GONE);
            helper.getView(R.id.tv_concentration).setVisibility(View.VISIBLE);
            name = context.getString(R.string.standard_sample);
            type = context.getString(R.string.standard_type);
            concentration = context.getString(R.string.standard_concentration);
            ct = context.getString(R.string.standard_ct);

            helper.setTextColor(R.id.tv_name, Color.WHITE);
            helper.setTextColor(R.id.tv_concentration, Color.WHITE);
            helper.setTextColor(R.id.tv_ct, Color.WHITE);
        } else {
            helper.getView(R.id.et_concentration).setVisibility(View.VISIBLE);
            helper.getView(R.id.tv_concentration).setVisibility(View.GONE);
            name = item.getName();
            type = item.getType();
            concentration = item.getConcentration();
            ct = item.getCtValue();

            helper.setTextColor(R.id.tv_name, itemColor);
            helper.setTextColor(R.id.tv_concentration, itemColor);
            helper.setTextColor(R.id.tv_ct, itemColor);
        }

        if (helper.getPosition()!=0){
            /*if (helper.getPosition()%2==0){
                helper.getView().setBackgroundResource(R.drawable.shape_solidf6f6f6);
            }else {
                helper.getView().setBackgroundResource(R.drawable.shape_solida5b8d6);
            }*/
            helper.getView().setBackgroundColor(Color.WHITE);
        }
        helper.setText(R.id.tv_name,name);
        helper.setText(R.id.tv_concentration,concentration);
        helper.setText(R.id.tv_ct,ct);



    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        EditText editText = (EditText) v;
        if (hasFocus) {
            editText.addTextChangedListener(mTextWatcher);
        } else {
            editText.removeTextChangedListener(mTextWatcher);
        }
    }

    private int selectedEditTextPosition;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (selectedEditTextPosition != -1) {
                SampleRow item = (SampleRow) getItem(selectedEditTextPosition);
                item.setConcentration(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            EditText editText = (EditText) v;
            selectedEditTextPosition = (int) editText.getTag();
        }
        return false;

    }
}
