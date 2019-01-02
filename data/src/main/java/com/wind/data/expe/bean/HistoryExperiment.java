package com.wind.data.expe.bean;

import com.wind.base.adapter.DisplayItem;

public class HistoryExperiment implements DisplayItem {
    private long id;//实验id
    private String name;     //实验名称
    private String device;  //实验连接的设备
    private String timestamp; //实验时间
    private long millitime;//实验时间毫秒值
    private ExperimentStatus status;


    private ExpeSettingsFirstInfo settingsFirstInfo;
    private ExpeSettingSecondInfo settingSecondInfo;


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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
}
