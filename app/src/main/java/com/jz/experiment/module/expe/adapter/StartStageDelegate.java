package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jz.experiment.R;
import com.wind.base.bean.StartStage;
import com.wind.base.widget.VernierDragLayout;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;

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
    protected void onBindViewHolder(@NonNull List<DisplayItem> items, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        ViewHolder vh= (ViewHolder) holder;
        StartStage startStage= (StartStage) items.get(position);
        startStage.setStepName("step 1");
        float startScale=startStage.getStartScale();
        float curScale=startStage.getCurScale();
        vh.vernier_drag_layout.setLink(null);
        vh.vernier_drag_layout.setStartScale(startScale);
        vh.vernier_drag_layout.setCurScale(curScale);

        startStage.setLayout(vh.vernier_drag_layout);

        vh.vernier_drag_layout.setLink(startStage);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        VernierDragLayout vernier_drag_layout;
        public ViewHolder(View itemView) {
            super(itemView);
            vernier_drag_layout=itemView.findViewById(R.id.vernier_drag_layout);
        }
    }
}
