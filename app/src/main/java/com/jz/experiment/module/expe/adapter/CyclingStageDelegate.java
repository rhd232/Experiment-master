package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.event.AddCyclingStageEvent;
import com.jz.experiment.module.expe.event.DelCyclingStageEvent;
import com.jz.experiment.util.AppDialogHelper;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.PartStage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class CyclingStageDelegate extends BaseAdapterDelegate<CyclingStageDelegate.ViewHolder> implements
View.OnFocusChangeListener{


    public CyclingStageDelegate(Activity activity, int layoutRes) {
        super(activity, layoutRes);

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

        this.items=items;
        final ViewHolder vh = (ViewHolder) holder;
        LinearLayoutManager manager=new LinearLayoutManager(mActivity){
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
        vh.rvInner.setLayoutManager(manager);


        final CyclingStage stage = (CyclingStage) items.get(position);

        VernierAdapter adapter = new VernierAdapter(mActivity, R.layout.item_inner_vernier);
        if (stage.getPartStageList().isEmpty()) {
            stage.addChildStage(0, new PartStage());
            //stage.addChildStage(1, new PartStage());
        }
        adapter.setParentStage(stage);
        vh.rvInner.setAdapter(adapter);
        //   stage.setVernierAdapter(adapter);
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
        int i=1;
        for (PartStage partStage : stage.getPartStageList()) {
            if (partStage.isTakePic()) {
                partStage.setStepName("step "+i);//step name没有报存数据库，所以设置一下
                picStage = partStage;

                break;
            }
            i++;
        }
        if (picStage != null) {
            vh.tv_pic_step.setText(picStage.getStepName());
           // stage.setPicStage(picStage);
        } else {
            vh.tv_pic_step.setText("无");
        }

        vh.tv_pic_step.setOnClickListener(new View.OnClickListener() {
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
        if (selectedEditTextPosition != -1 && position == selectedEditTextPosition) { // 保证每个时刻只有一个EditText能获取到焦点
            vh.et_cycling_cnt.requestFocus();
        } else {
            vh.et_cycling_cnt.clearFocus();
        }


        int count=stage.getCyclingCount();

        vh.et_cycling_cnt.setText(count+ "");
        vh.et_cycling_cnt.setSelection(vh.et_cycling_cnt.length());
      //  vh.et_cycling_cnt.setOnTouchListener(this); // 正确写法
        vh.et_cycling_cnt.setOnFocusChangeListener(this);


       /* RxTextView.textChanges(vh.et_cycling_cnt).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                String s = charSequence.toString().trim();
                if (TextUtils.isEmpty(s)) {
                    s = "1";
                }
                try {
                    vh.et_cycling_cnt.setSelection(s.length());
                }catch (Exception e){
                    e.printStackTrace();
                }

                int count = Integer.parseInt(s);
                stage.setCyclingCount(count);
            }
        });*/

    }

  /*  @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            EditText editText = (EditText) v;
            selectedEditTextPosition = (int) editText.getTag();
        }
        return false;
    }
*/
  private int selectedEditTextPosition=-1;
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
               /* ItemBean itemTest = (ItemBean) getItem(selectedEditTextPosition);
                itemTest.setText(s.toString());*/
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
    };


    static class ViewHolder extends RecyclerView.ViewHolder {

        RecyclerView rvInner;
        View iv_cycling_add, iv_cycling_del;
        EditText et_cycling_cnt;
        TextView tv_pic_step;


        public ViewHolder(View itemView) {
            super(itemView);
            iv_cycling_add = itemView.findViewById(R.id.iv_cycling_add);
            iv_cycling_del = itemView.findViewById(R.id.iv_cycling_del);
            et_cycling_cnt = itemView.findViewById(R.id.et_cycling_cnt);
            tv_pic_step = itemView.findViewById(R.id.tv_pic_step);

            rvInner = itemView.findViewById(R.id.rv_inner);
            LinearLayoutManager manager = new LinearLayoutManager(itemView.getContext()) {
                @Override
                public boolean canScrollHorizontally() {
                    return false;
                }

                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };

            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rvInner.setLayoutManager(manager);
        }
    }
}
