package com.jz.experiment.module.expe.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jz.experiment.R;
import com.wind.data.expe.bean.DeviceInfo;
import com.jz.experiment.module.expe.event.ConnectRequestEvent;
import com.wind.base.adapter.BaseRecyclerAdapter;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceAdapter extends BaseRecyclerAdapter<DeviceInfo,DeviceAdapter.ViewHolder> {


    public DeviceAdapter(Activity activity, int layoutRes) {
        super(activity, layoutRes);
    }

    @Override
    public ViewHolder onCreateViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DeviceInfo deviceInfo=getItem(position);
        holder.tv_dev_name.setText(deviceInfo.getName());
        holder.iv_connect_status.setActivated(deviceInfo.isConnected());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ConnectRequestEvent(deviceInfo));
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_dev_name)
        TextView tv_dev_name;

        @BindView(R.id.iv_connect_status)
        ImageView iv_connect_status;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }
    }
}
