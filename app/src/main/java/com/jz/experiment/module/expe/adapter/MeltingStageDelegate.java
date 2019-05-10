package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jz.experiment.R;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.bean.MeltingStage;
import com.wind.base.widget.VernierDragLayout;

import java.util.List;

public class MeltingStageDelegate extends BaseAdapterDelegate<MeltingStageDelegate.ViewHolder> {


    public MeltingStageDelegate(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    protected boolean isForViewType(@NonNull List<DisplayItem> items, int position) {

        return items.get(position) instanceof MeltingStage;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<DisplayItem> items,final int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        final ViewHolder vh= (ViewHolder) holder;
        MeltingStage startStage= (MeltingStage) items.get(position);
        startStage.setStepName("step 1");
        final float startScale=startStage.getStartScale();
       /* if (position==0){
            startScale=-1;
        }*/
        final float curScale=startStage.getCurScale();
       // System.out.println("curScale:"+curScale);
       // vh.vernier_drag_layout.setLink(null);
        vh.vernier_drag_layout.setLink(startStage);
        vh.vernier_drag_layout.setStartScale(startScale);
        vh.vernier_drag_layout.setCurScale(curScale);
        vh.vernier_drag_layout.setTimeLayoutVisibility(View.GONE);
        startStage.setLayout(vh.vernier_drag_layout);

       /* if (position==0){

            vh.vernier_drag_layout.post(new Runnable() {
                @Override
                public void run() {

                    vh.vernier_drag_layout.resetStartScale();


                    if (curScale==-1){
                        vh.vernier_drag_layout.resetCurScale();
                    }
                }
            });
        }*/

    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        VernierDragLayout vernier_drag_layout;


        public ViewHolder(View itemView) {
            super(itemView);
            vernier_drag_layout=itemView.findViewById(R.id.vernier_drag_layout);

        }
    }
}
