package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Mode implements Parcelable {
    private String name;
    private float ctThreshold=1;
    private int ctMin=13;

    public Mode(String name){
        this.name=name;
    }

    protected Mode(Parcel in) {
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Mode> CREATOR = new Creator<Mode>() {
        @Override
        public Mode createFromParcel(Parcel in) {
            return new Mode(in);
        }

        @Override
        public Mode[] newArray(int size) {
            return new Mode[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getCtThreshold() {
        return ctThreshold;
    }

    public void setCtThreshold(Float ctThreshold) {
        this.ctThreshold = ctThreshold;
    }

    public int getCtMin() {
        return ctMin;
    }

    public void setCtMin(int ctMin) {
        this.ctMin = ctMin;
    }
}
