package com.wind.data.expe.table;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.wind.data.expe.bean.SampleInfoModel;

@AutoValue
public abstract class SampleInfo implements SampleInfoModel {
    public static final Factory<SampleInfo> FACTORY=new Factory<>(new Creator<SampleInfo>() {
        @Override
        public SampleInfo create(long _id, @NonNull String name, long type, long expe_id) {
            return new AutoValue_SampleInfo(_id,name,type,expe_id);
        }
    });
}
