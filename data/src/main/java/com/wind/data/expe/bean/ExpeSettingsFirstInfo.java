package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ExpeSettingsFirstInfo implements Parcelable {

    private String name;
    private long expeStartMilliTime;//实验开始时间
    private List<Channel>  channels;//通道信息
    private List<Sample> samplesA;//样本a数据
    private List<Sample> samplesB;//样本b数据

    public ExpeSettingsFirstInfo(){}
    protected ExpeSettingsFirstInfo(Parcel in) {
        name = in.readString();
        expeStartMilliTime = in.readLong();
        channels=in.readArrayList(Channel.class.getClassLoader());
        samplesA=in.readArrayList(Sample.class.getClassLoader());
        samplesB=in.readArrayList(Sample.class.getClassLoader());
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(expeStartMilliTime);
        dest.writeList(channels);
        dest.writeList(samplesA);
        dest.writeList(samplesB);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExpeSettingsFirstInfo> CREATOR = new Creator<ExpeSettingsFirstInfo>() {
        @Override
        public ExpeSettingsFirstInfo createFromParcel(Parcel in) {
            return new ExpeSettingsFirstInfo(in);
        }

        @Override
        public ExpeSettingsFirstInfo[] newArray(int size) {
            return new ExpeSettingsFirstInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getExpeStartMilliTime() {
        return expeStartMilliTime;
    }

    public void setExpeStartMilliTime(long expeStartMilliTime) {
        this.expeStartMilliTime = expeStartMilliTime;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Sample> getSamplesA() {
        return samplesA;
    }

    public void setSamplesA(List<Sample> samplesA) {
        this.samplesA = samplesA;
    }

    public List<Sample> getSamplesB() {
        return samplesB;
    }

    public void setSamplesB(List<Sample> samplesB) {
        this.samplesB = samplesB;
    }
}
