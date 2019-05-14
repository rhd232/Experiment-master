package com.wind.data.expe.table;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.wind.data.expe.bean.ChannelInfoModel;

@AutoValue
public abstract class ChannelInfo implements ChannelInfoModel {

    public static final Factory<ChannelInfo> FACTORY = new Factory<>(new Creator<ChannelInfo>() {
        @Override
        public ChannelInfo create(long _id, @NonNull String name, @NonNull String value, @NonNull String remark, Long integration_time, long expe_id) {
            return new AutoValue_ChannelInfo(_id, name, value, remark, integration_time,expe_id);
        }
    });
}
