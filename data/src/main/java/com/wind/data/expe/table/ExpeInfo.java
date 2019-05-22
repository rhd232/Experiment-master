package com.wind.data.expe.table;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.wind.data.expe.bean.ExpeInfoModel;

@AutoValue
public abstract class ExpeInfo implements ExpeInfoModel {
    public static final Factory<ExpeInfo> FACTORY = new Factory<>(
            new Creator<ExpeInfo>() {

                @Override
                public ExpeInfo create(long _id, @NonNull String name,
                                       @Nullable String device, long millitime,
                                       long status, @NonNull String status_desc,
                                       @Nullable Long finish_millitime, @Nullable Long during,
                                       @NonNull String mode, @Nullable String startTemperature,
                                       @Nullable String endTemperature,  @Nullable Long autoIntTime) {
                    return new AutoValue_ExpeInfo(_id, name,
                            device, millitime,
                            status, status_desc,finish_millitime,during,mode,
                            startTemperature, endTemperature,autoIntTime);
                }
            }
    );
    /*public static final Factory<ExpeInfo> FACTORY = new Factory<>(
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
    );*/


}
