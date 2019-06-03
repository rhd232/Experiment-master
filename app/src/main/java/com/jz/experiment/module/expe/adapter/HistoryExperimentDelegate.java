package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.jz.experiment.MainActivity;
import com.jz.experiment.R;
import com.jz.experiment.module.expe.event.ToExpeSettingsEvent;
import com.wind.base.adapter.BaseAdapterDelegate;
import com.wind.base.adapter.BaseDelegateRecyclerAdapter;
import com.wind.base.adapter.DisplayItem;
import com.wind.base.utils.DateUtil;
import com.wind.data.expe.bean.HistoryExperiment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryExperimentDelegate extends BaseAdapterDelegate<HistoryExperimentDelegate.ViewHolder> {


    private BaseDelegateRecyclerAdapter mAdapter;
    public HistoryExperimentDelegate(Activity activity, int layoutRes, BaseDelegateRecyclerAdapter adapter) {
        super(activity, layoutRes);
        mAdapter=adapter;
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
    protected void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(viewHolder);
    }

    @Override
    protected void onBindViewHolder(@NonNull List<DisplayItem> items,final int position, @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
        final HistoryExperiment experiment= (HistoryExperiment) items.get(position);
        ViewHolder vh= (ViewHolder) holder;
        vh.tv_expe_name.setText(experiment.getName());
        vh.tv_expe_device.setText("-"+experiment.getDevice());
        String date=DateUtil.get(experiment.getMillitime(),"yy-MM-dd HH:mm:ss");
        vh.tv_expe_time.setText(date);
        vh.tv_expe_status.setText(experiment.getStatus().getDesc());

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ToExpeSettingsEvent(experiment));
               // UserSettingsStep1Activity.start(mActivity,experiment);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setContextMenuPosition(position);
                return false;
            }
        });

    }
    private int position;
    public int getContextMenuPosition() { return position; }
    public void setContextMenuPosition(int position) { this.position = position; }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{

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
            itemView.setOnCreateContextMenuListener(this);
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            HistoryExperiment item = (HistoryExperiment) mAdapter.getItem(getContextMenuPosition());
            Log.i("Adapter", "onCreateContextMenu: "+getContextMenuPosition());
           // menu.setHeaderTitle(mSelectModelUser.getUserName());
            ((MainActivity)mActivity).CreateMenu(menu);

        }
    }
}
