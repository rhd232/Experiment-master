package com.wind.base.bean;

import android.os.Parcel;

public class MeltingStage extends Stage {

    public MeltingStage(){
        setType(TYPE_MELTING);
    }

    protected MeltingStage(Parcel in) {
        super(in);
    }

    public static final Creator<MeltingStage> CREATOR = new Creator<MeltingStage>() {
        @Override
        public MeltingStage createFromParcel(Parcel in) {
            return new MeltingStage(in);
        }

        @Override
        public MeltingStage[] newArray(int size) {
            return new MeltingStage[size];
        }
    };
}
