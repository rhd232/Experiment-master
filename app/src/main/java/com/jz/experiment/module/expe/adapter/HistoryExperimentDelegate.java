package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.R;
import com.jz.experiment.module.expe.activity.UserSettingsStep1Activity;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.utils.DateUtil;
import com.wind.data.expe.bean.HistoryExperiment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryExperimentDelegate extends BaseAdapterDelegate<HistoryExperimentDelegate.ViewHolder> {


    public HistoryExperimentDelegate(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    protected boolean isForViewType(@NonNull List<DisplayItem> items, int position) {
        return items.get(position) instanceof HistoryExperiment;
    }

    @Override
    protected void onBindViewHolder(@NonNull List<DisplayItem> items, int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        final HistoryExperiment experiment= (HistoryExperiment) items.get(position);
        ViewHolder vh= (ViewHolder) holder;
        vh.tv_expe_name.setText(experiment.getName());
        vh.tv_expe_device.setText(experiment.getDevice());
        String date=DateUtil.get(experiment.getMillitime(),"yy-MM-dd HH:mm:ss");
        vh.tv_expe_time.setText(date);
        vh.tv_expe_status.setText(experiment.getStatus().getDesc());

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSettingsStep1Activity.start(mActivity,experiment);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_expe_name)
        TextView tv_expe_name;
        @BindView(R.id.tv_expe_device)
        TextView tv_expe_device;
        @BindView(R.id.tv_expe_time)
        TextView tv_expe_time;
        @BindView(R.id.tv_expe_status)
        TextView tv_expe_status;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
