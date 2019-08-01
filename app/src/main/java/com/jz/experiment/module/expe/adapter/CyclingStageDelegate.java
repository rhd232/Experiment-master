package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.event.AddCyclingStageEvent;
import com.jz.experiment.module.expe.event.DelCyclingStageEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.jz.experiment.widget.HorizontalContainer;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.PartStage;
import com.wind.base.dialog.AlertDialogUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class CyclingStageDelegate extends BaseAdapterDelegate<CyclingStageDelegate.ViewHolder> {

    private RecyclerView.Adapter mAdapter;

    public CyclingStageDelegate(Activity activity, RecyclerView.Adapter adapter, int layoutRes) {
        super(activity, layoutRes);
        this.mAdapter = adapter;

    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    protected boolean isForViewType(@NonNull List<DisplayItem> items, int position) {
        return items.get(position) instanceof CyclingStage;
    }

    private List<DisplayItem> items;

    @Override
    protected void onBindViewHolder(@NonNull List<DisplayItem> items, final int position,
                                    @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {

        this.items = items;
        final ViewHolder vh = (ViewHolder) holder;
       /* LinearLayoutManager manager=new LinearLayoutManager(mActivity){
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        vh.rvInner.setLayoutManager(manager);*/


        final CyclingStage stage = (CyclingStage) items.get(position);

        VernierAdapter adapter = new VernierAdapter(mActivity, R.layout.item_inner_vernier);
        if (stage.getPartStageList().isEmpty()) {
            stage.addChildStage(0, new PartStage());
        }


        adapter.setParentStage(stage);
        //  vh.rvInner.setAdapter(adapter);
        vh.horizontal_container.setAdapter(adapter);
        vh.iv_cycling_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EventBus.getDefault().post(new AddCyclingStageEvent(position));
                //buildLink(stage,(VernierAdapter) vh.rvInner.getAdapter());
            }
        });

        vh.iv_cycling_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new DelCyclingStageEvent(position));
                // buildLink( stage,(VernierAdapter)vh.rvInner.getAdapter());
            }
        });

        PartStage picStage = null;
        int i = 1;
        for (PartStage partStage : stage.getPartStageList()) {
            if (partStage.isTakePic()) {
                partStage.setStepName("step " + i);//step name没有报存数据库，所以设置一下
                picStage = partStage;

                break;
            }
            i++;
        }
        if (picStage != null) {
            vh.tv_pic_step.setText(picStage.getStepName());
            // stage.setPicStage(picStage);
        } else {

            vh.tv_pic_step.setText(mActivity.getString(R.string.setup_nopic));
        }

        vh.ll_pic_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDialogHelper.showStepSelectDialog(mActivity, stage.getPartStageList(), new AppDialogHelper.OnStepSelectListener() {
                    @Override
                    public void onStepSelected(PartStage step) {
                        vh.tv_pic_step.setText(step.getStepName());
                    }
                });
            }
        });
        vh.et_cycling_cnt.setTag(position);
        vh.et_cycling_cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(mActivity, stage);
            }
        });
     /*   if (selectedEditTextPosition != -1 && position == selectedEditTextPosition) { // 保证每个时刻只有一个EditText能获取到焦点
            vh.et_cycling_cnt.requestFocus();
        } else {
            vh.et_cycling_cnt.clearFocus();
        }*/


        int count = stage.getCyclingCount();
        vh.et_cycling_cnt.setText(count + "");
        // vh.et_cycling_cnt.setSelection(vh.et_cycling_cnt.length());
        //vh.et_cycling_cnt.setOnFocusChangeListener(this);


    }


  /*private int selectedEditTextPosition=-1;
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        EditText editText = (EditText) v;
        if (hasFocus) {
            selectedEditTextPosition=(int) editText.getTag();
            editText.addTextChangedListener(mTextWatcher);
        } else {
            editText.removeTextChangedListener(mTextWatcher);
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (selectedEditTextPosition != -1) {
                Log.w("MyEditAdapter", "onTextPosiotion " + selectedEditTextPosition);
               *//* ItemBean itemTest = (ItemBean) getItem(selectedEditTextPosition);
                itemTest.setText(s.toString());*//*
                CyclingStage stage= (CyclingStage) items.get(selectedEditTextPosition);
                String ss = s.toString().trim();
                if (TextUtils.isEmpty(ss)) {
                    ss = "1";
                }


                int v = Integer.parseInt(ss);
                stage.setCyclingCount(v);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };*/


    static class ViewHolder extends RecyclerView.ViewHolder {

        RecyclerView rvInner;
        View iv_cycling_add, iv_cycling_del;
        TextView et_cycling_cnt;
        TextView tv_pic_step;
        View stageItemView;
        View ll_pic_step;
        HorizontalContainer horizontal_container;

        public ViewHolder(View itemView) {
            super(itemView);
            stageItemView = itemView.findViewById(R.id.ll_stage_item);
            iv_cycling_add = itemView.findViewById(R.id.iv_cycling_add);
            iv_cycling_del = itemView.findViewById(R.id.iv_cycling_del);
            et_cycling_cnt = itemView.findViewById(R.id.et_cycling_cnt);
            tv_pic_step = itemView.findViewById(R.id.tv_pic_step);
            ll_pic_step = itemView.findViewById(R.id.ll_pic_step);
            // lv_inner=itemView.findViewById(R.id.lv_inner);
            horizontal_container = itemView.findViewById(R.id.horizontal_container);

        }
    }


    public void showInputDialog(Context context, final CyclingStage stage) {
        final AlertDialog alertDialog = AlertDialogUtil.showAlertDialog(context,
                com.wind.base.R.layout.dialog_input_temperature, true);
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        final EditText et_temp = alertDialog.findViewById(com.wind.base.R.id.et_temp);

        alertDialog.findViewById(com.wind.base.R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = et_temp.getText().toString().trim();
                if (!TextUtils.isEmpty(temp)) {
                    int cycleCount = Integer.parseInt(temp);
                    if (cycleCount > 0) {
                        alertDialog.dismiss();
                        stage.setCyclingCount(cycleCount);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }
}
