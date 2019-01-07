package com.jz.experiment.module.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.wind.data.expe.bean.HistoryExperiment;

public class Tab implements Parcelable{
    private int index;

    private HistoryExperiment extra;

    public Tab(){}


    protected Tab(Parcel in) {
        index = in.readInt();
        extra = in.readParcelable(HistoryExperiment.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
        dest.writeParcelable(extra, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Tab> CREATOR = new Creator<Tab>() {
        @Override
        public Tab createFromParcel(Parcel in) {
            return new Tab(in);
        }

        @Override
        public Tab[] newArray(int size) {
            return new Tab[size];
        }
    };

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public HistoryExperiment getExtra() {
        return extra;
    }

    public void setExtra(HistoryExperiment extra) {
        this.extra = extra;
    }
}
