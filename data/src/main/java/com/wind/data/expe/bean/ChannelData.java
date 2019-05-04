package com.wind.data.expe.bean;

public class ChannelData {
    //所属通道
    private String channelName;
    //对应样本位置
    private int sampleIndex;
    //对应样本位置别名
    private String sampleIndexAlias;

    //对应样本值
    private String sampleVal;


    public ChannelData(String channelName, int sampleIndex,String sampleIndexAlias, String sampleVal) {
        this.channelName = channelName;
        this.sampleIndex = sampleIndex;
        this.sampleIndexAlias=sampleIndexAlias;
        this.sampleVal = sampleVal;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public int getSampleIndex() {
        return sampleIndex;
    }

    public void setSampleIndex(int sampleIndex) {
        this.sampleIndex = sampleIndex;
    }

    public String getSampleVal() {
        return sampleVal;
    }

    public void setSampleVal(String sampleVal) {
        this.sampleVal = sampleVal;
    }

    public String getSampleIndexAlias() {
        return sampleIndexAlias;
    }
}
