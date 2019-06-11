package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.R;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.PartStage;
import com.jz.experiment.module.expe.event.RefreshStageAdapterEvent;
import com.wind.base.widget.VernierDragLayout;
import com.wind.base.adapter.BaseRecyclerAdapter;
import com.wind.toastlib.ToastUtil;

import org.greenrobot.eventbus.EventBus;

public class VernierAdapter extends BaseRecyclerAdapter<PartStage,VernierAdapter.ViewHolder> {


    public VernierAdapter(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    @Override
    public ViewHolder onCreateViewHolder(View v) {
        return new ViewHolder(v);
    }
    private CyclingStage mParentStage;
    public void setParentStage(CyclingStage parentStage){
        this.mParentStage=parentStage;
        replace(mParentStage.getPartStageList());
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        PartStage partStage= getItem(position);
        partStage.setStepName("step "+(position+1));
      //  partStage.setPosition(position);

        String step=mActivity.getString(R.string.step,(position+1)+"");
        holder.tv_step_name.setText(step);
        float startScale=partStage.getStartScale();
        float curScale=partStage.getCurScale();
     //   holder.vernier_drag_layout.setLink(null);
        holder.vernier_drag_layout.setLink(partStage);
        holder.vernier_drag_layout.setStartScale(startScale);
        holder.vernier_drag_layout.setCurScale(curScale);

        partStage.setLayout(holder.vernier_drag_layout);



        holder.iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PartStage partStage=new PartStage();
                mParentStage.addChildStage(position,partStage);
                add(position,partStage);
                EventBus.getDefault().post(new RefreshStageAdapterEvent());

            }
        });
        holder.iv_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItemCount()==1){
                    ToastUtil.showToast(mActivity,"至少含有一个循环步骤");
                }else {
                    mParentStage.removeChildStage(position);
                    remove(position);
                    EventBus.getDefault().post(new RefreshStageAdapterEvent());

                }
            }
        });

    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View iv_add,iv_del;
        VernierDragLayout vernier_drag_layout;
        TextView tv_step_name;
        public ViewHolder(View itemView) {
            super(itemView);
            iv_add=itemView.findViewById(R.id.iv_add);
            iv_del=itemView.findViewById(R.id.iv_del);
            tv_step_name=itemView.findViewById(R.id.tv_step_name);
            vernier_drag_layout=itemView.findViewById(R.id.vernier_drag_layout);
        }
    }

}
