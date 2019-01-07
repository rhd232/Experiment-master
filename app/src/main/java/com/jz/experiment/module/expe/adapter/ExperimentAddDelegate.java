package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.event.ToExpeSettingsEvent;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;
import com.wind.data.expe.bean.AddExperiment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ExperimentAddDelegate extends BaseAdapterDelegate<ExperimentAddDelegate.ViewHolder> {


    public ExperimentAddDelegate(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    protected boolean isForViewType(@NonNull List<DisplayItem> items, int position) {
        return items.get(position) instanceof AddExperiment;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<DisplayItem> items, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        ViewHolder vh= (ViewHolder) holder;
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ToExpeSettingsEvent(null));
                //UserSettingsStep1Activity.start(mActivity);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_add_expe;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_add_expe=itemView.findViewById(R.id.tv_add_expe);
        }
    }
}
