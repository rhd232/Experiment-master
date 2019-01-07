package com.wind.base.bean;

import android.os.Parcel;

public class EndStage extends Stage {

    public EndStage(){
        setType(TYPE_END);
    }


    protected EndStage(Parcel in) {
        super(in);
    }

    public static final Creator<EndStage> CREATOR = new Creator<EndStage>() {
        @Override
        public EndStage createFromParcel(Parcel in) {
            return new EndStage(in);
        }

        @Override
        public EndStage[] newArray(int size) {
            return new EndStage[size];
        }
    };
}
