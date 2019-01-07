package com.wind.base.bean;

import android.os.Parcel;

public class StartStage extends Stage {



    public StartStage(){
        setType(TYPE_START);
    }

    protected StartStage(Parcel in) {
        super(in);
    }

    public static final Creator<StartStage> CREATOR = new Creator<StartStage>() {
        @Override
        public StartStage createFromParcel(Parcel in) {
            return new StartStage(in);
        }

        @Override
        public StartStage[] newArray(int size) {
            return new StartStage[size];
        }
    };
}
