package com.wind.data.expe.bean;

import java.util.List;

public class ExpeSettingsFirstInfo {

    private String name;
    private long expeStartMilliTime;//实验开始时间
    private List<Channel>  channels;//通道信息
    private List<Sample> samplesA;//样本a数据
    private List<Sample> samplesB;//样本b数据

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
