package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.event.RefreshStageAdapterEvent;
import com.wind.base.adapter.BaseAdapterHelper;
import com.wind.base.adapter.QuickAdapter;
import com.wind.base.bean.CyclingStage;
import com.wind.base.bean.PartStage;
import com.wind.base.widget.VernierDragLayout;
import com.wind.toastlib.ToastUtil;

import org.greenrobot.eventbus.EventBus;

public class VernierListAdapter extends QuickAdapter<PartStage> {


    public VernierListAdapter(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    private CyclingStage mParentStage;
    public void setParentStage(CyclingStage parentStage){
        this.mParentStage=parentStage;
        replace(mParentStage.getPartStageList());
    }
    @Override
    protected void convert(BaseAdapterHelper helper, PartStage item) {
        final int position=helper.getPosition();
        PartStage partStage= getItem(position);
        partStage.setStepName("step "+(position+1));
        //  partStage.setPosition(position);

        String step=context.getString(R.string.step,(position+1)+"");
        helper.setText(R.id.tv_step_name,step);
        float startScale=partStage.getStartScale();
        float curScale=partStage.getCurScale();
        //   holder.vernier_drag_layout.setLink(null);
        VernierDragLayout layout=helper.getView(R.id.vernier_drag_layout);
        layout.setLink(partStage);
        layout.setStartScale(startScale);
        layout.setCurScale(curScale);

        partStage.setLayout(layout);



        helper.getView(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PartStage partStage=new PartStage();
                mParentStage.addChildStage(position,partStage);
                add(position,partStage);
                EventBus.getDefault().post(new RefreshStageAdapterEvent());

            }
        });
        helper.getView(R.id.iv_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCount()==1){
                    ToastUtil.showToast((Activity) context,"最后一个不能删除");
                }else {
                    mParentStage.removeChildStage(position);
                    remove(position);
                    EventBus.getDefault().post(new RefreshStageAdapterEvent());

                }
            }
        });
    }

    static class ViewHolder{

        View iv_add,iv_del;
        VernierDragLayout vernier_drag_layout;
        TextView tv_step_name;
        public ViewHolder(View itemView) {
            iv_add=itemView.findViewById(R.id.iv_add);
            iv_del=itemView.findViewById(R.id.iv_del);
            tv_step_name=itemView.findViewById(R.id.tv_step_name);
            vernier_drag_layout=itemView.findViewById(R.id.vernier_drag_layout);
        }
    }

}
