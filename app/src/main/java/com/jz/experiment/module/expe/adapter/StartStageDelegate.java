package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.event.AddStartStageEvent;
import com.jz.experiment.module.expe.event.DelStartStageEvent;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.bean.StartStage;
import com.wind.base.widget.VernierDragLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class StartStageDelegate extends BaseAdapterDelegate<StartStageDelegate.ViewHolder> {


    public StartStageDelegate(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    protected boolean isForViewType(@NonNull List<DisplayItem> items, int position) {

        return items.get(position) instanceof StartStage;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<DisplayItem> items,final int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        final ViewHolder vh= (ViewHolder) holder;
        ViewGroup.LayoutParams lp=vh.stageItemView.getLayoutParams();
        if (StageAdapter.STAGE_ITEM_WIDTH!= lp.width){
            lp.width=StageAdapter.STAGE_ITEM_WIDTH;
        }


        StartStage startStage= (StartStage) items.get(position);
        startStage.setStepName("step 1");
        final float startScale=startStage.getStartScale();
       /* if (position==0){
            startScale=-1;
        }*/
        final float curScale=startStage.getCurScale();
        System.out.println("startStage:curScale:"+curScale);
       // vh.vernier_drag_layout.setLink(null);
        vh.vernier_drag_layout.setLink(startStage);
        vh.vernier_drag_layout.setStartScale(startScale);
        vh.vernier_drag_layout.setCurScale(curScale);

        startStage.setLayout(vh.vernier_drag_layout);

        if (position==0){

            vh.vernier_drag_layout.post(new Runnable() {
                @Override
                public void run() {

                    vh.vernier_drag_layout.resetStartScale();


                    if (curScale==-1){
                        vh.vernier_drag_layout.resetCurScale();
                    }
                }
            });
        }

        vh.iv_start_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new AddStartStageEvent(position));
            }
        });
        vh.iv_start_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new DelStartStageEvent(position));
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        VernierDragLayout vernier_drag_layout;
        View stageItemView;
        View iv_start_add;
        View iv_start_del;
        public ViewHolder(View itemView) {
            super(itemView);
            stageItemView=itemView.findViewById(R.id.ll_stage_item);
            vernier_drag_layout=itemView.findViewById(R.id.vernier_drag_layout);
            iv_start_add=itemView.findViewById(R.id.iv_start_add);
            iv_start_del=itemView.findViewById(R.id.iv_start_del);
        }
    }
}
