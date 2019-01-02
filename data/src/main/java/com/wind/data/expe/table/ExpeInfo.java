package com.wind.data.expe.table;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.wind.data.expe.bean.ExpeInfoModel;

@AutoValue
public abstract class ExpeInfo implements ExpeInfoModel {

    public static final Factory<ExpeInfo> FACTORY = new Factory<>(
            new Creator<ExpeInfo>() {
                @Override
                public ExpeInfo create(long _id, @NonNull String name,
                                       @NonNull String device, long millitime,
                                       long status, @NonNull String status_desc,
                                       @NonNull String mode, @NonNull String startTemperature,
                                       @NonNull String endTemperature) {
                    return new AutoValue_ExpeInfo(_id, name,
                            device, millitime,
                            status, status_desc, mode,
                            startTemperature, endTemperature);
                }

            }
    );


}
