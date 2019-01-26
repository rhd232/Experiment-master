package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.wind.base.adapter.DisplayItem;

public class HistoryExperiment implements DisplayItem,Parcelable {
    public static final long ID_NONE=-1;
    private long id=ID_NONE;//实验id
    private String name;     //实验名称
    private String device;  //实验连接的设备
    private long millitime;//实验时间毫秒值
    private ExperimentStatus status;//实验状态

    private long finishMilliTime;//实验结束时间
    private long during;//实验持续时间，秒为单位

    private ExpeSettingsFirstInfo settingsFirstInfo;
    private ExpeSettingSecondInfo settingSecondInfo;

    private ChartData dtChartData;//变温扩增曲线数据
    private ChartData meltaChartData;//溶解曲线数据

    public HistoryExperiment(){}


    protected HistoryExperiment(Parcel in) {
        id = in.readLong();
        name = in.readString();
        device = in.readString();
        millitime = in.readLong();
        status = in.readParcelable(ExperimentStatus.class.getClassLoader());
        finishMilliTime = in.readLong();
        during = in.readLong();
        settingsFirstInfo = in.readParcelable(ExpeSettingsFirstInfo.class.getClassLoader());
        settingSecondInfo = in.readParcelable(ExpeSettingSecondInfo.class.getClassLoader());
        dtChartData = in.readParcelable(ChartData.class.getClassLoader());
        meltaChartData = in.readParcelable(ChartData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(device);
        dest.writeLong(millitime);
        dest.writeParcelable(status, flags);
        dest.writeLong(finishMilliTime);
        dest.writeLong(during);
        dest.writeParcelable(settingsFirstInfo, flags);
        dest.writeParcelable(settingSecondInfo, flags);
        dest.writeParcelable(dtChartData, flags);
        dest.writeParcelable(meltaChartData, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HistoryExperiment> CREATOR = new Creator<HistoryExperiment>() {
        @Override
        public HistoryExperiment createFromParcel(Parcel in) {
            return new HistoryExperiment(in);
        }

        @Override
        public HistoryExperiment[] newArray(int size) {
            return new HistoryExperiment[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }




    public ExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(ExperimentStatus status) {
        this.status = status;
    }

    public long getMillitime() {
        return millitime;
    }

    public void setMillitime(long millitime) {
        this.millitime = millitime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExpeSettingsFirstInfo getSettingsFirstInfo() {
        return settingsFirstInfo;
    }

    public void setSettingsFirstInfo(ExpeSettingsFirstInfo settingsFirstInfo) {
        this.settingsFirstInfo = settingsFirstInfo;
    }

    public ExpeSettingSecondInfo getSettingSecondInfo() {
        return settingSecondInfo;
    }

    public void setSettingSecondInfo(ExpeSettingSecondInfo settingSecondInfo) {
        this.settingSecondInfo = settingSecondInfo;
    }

    public ChartData getDtChartData() {
        return dtChartData;
    }

    public void setDtChartData(ChartData dtChartData) {
        this.dtChartData = dtChartData;
    }

    public ChartData getMeltaChartData() {
        return meltaChartData;
    }

    public void setMeltaChartData(ChartData meltaChartData) {
        this.meltaChartData = meltaChartData;
    }

    public long getFinishMilliTime() {
        return finishMilliTime;
    }

    public void setFinishMilliTime(long finishMilliTime) {
        this.finishMilliTime = finishMilliTime;
    }

    public long getDuring() {
        return during;
    }

    public void setDuring(long during) {
        this.during = during;
    }
}
