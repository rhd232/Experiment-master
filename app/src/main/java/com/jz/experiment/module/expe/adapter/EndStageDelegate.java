package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.jz.experiment.R;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.bean.EndStage;
import com.wind.base.widget.VernierDragLayout;

import java.util.List;

public class EndStageDelegate extends BaseAdapterDelegate<EndStageDelegate.ViewHolder> {


    public EndStageDelegate(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    protected boolean isForViewType(@NonNull List<DisplayItem> items, int position) {

        return items.get(position) instanceof EndStage;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<DisplayItem> items, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        ViewHolder vh= (ViewHolder) holder;

        if (StageAdapter.STAGE_ITEM_WIDTH!=0){
            ViewGroup.LayoutParams lp=vh.stageItemView.getLayoutParams();
            lp.width=StageAdapter.STAGE_ITEM_WIDTH;
        }

        EndStage endStage= (EndStage) items.get(position);
        endStage.setStepName("step 1");
        float startScale=endStage.getStartScale();
        float curScale=endStage.getCurScale();

        vh.vernier_drag_layout.setLink(null);
        vh.vernier_drag_layout.setStartScale(startScale);
        vh.vernier_drag_layout.setCurScale(curScale);

        endStage.setLayout(vh.vernier_drag_layout);

        vh.vernier_drag_layout.setLink(endStage);

    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        VernierDragLayout vernier_drag_layout;
        View stageItemView;
        public ViewHolder(View itemView) {
            super(itemView);
            stageItemView=itemView.findViewById(R.id.ll_stage_item);
            vernier_drag_layout=itemView.findViewById(R.id.vernier_drag_layout);
        }
    }
}
