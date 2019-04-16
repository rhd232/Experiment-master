package com.wind.data.expe.table;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.wind.data.expe.bean.StageInfoModel;

@AutoValue
public abstract class StageInfo  implements StageInfoModel {

    public static final Factory<StageInfo> FACTORY=new Factory<>(new Creator<StageInfo>() {
        @Override
        public StageInfo create(long _id, @Nullable Long type,
                                @Nullable Double startScale,
                                @Nullable Double curScale, @Nullable String stepName,
                                @Nullable Long serialNumber, @Nullable Long cycling_count,
                                @Nullable Long part_takepic, @Nullable Long cycling_id, Long during,long expe_id) {
            return new AutoValue_StageInfo(_id,type,startScale,curScale,stepName,serialNumber,cycling_count,part_takepic,cycling_id,during,expe_id);
        }


    });
}
