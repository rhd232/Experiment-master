package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class ExperimentStatus implements Parcelable {
    public static final int STATUS_NOT_START=0;
    public static final int STATUS_COMPLETED=1;

    private int status;
    private String desc;

    public ExperimentStatus(){

    }
    protected ExperimentStatus(Parcel in) {
        status = in.readInt();
        desc = in.readString();
    }

    public static final Creator<ExperimentStatus> CREATOR = new Creator<ExperimentStatus>() {
        @Override
        public ExperimentStatus createFromParcel(Parcel in) {
            return new ExperimentStatus(in);
        }

        @Override
        public ExperimentStatus[] newArray(int size) {
            return new ExperimentStatus[size];
        }
    };

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeString(desc);
    }
}
