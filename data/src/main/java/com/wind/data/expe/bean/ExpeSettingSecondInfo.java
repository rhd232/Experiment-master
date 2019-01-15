package com.wind.data.expe.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.wind.base.bean.Stage;

import java.util.ArrayList;
import java.util.List;

public class ExpeSettingSecondInfo implements Parcelable {
    /**选择的模式*/
    private List<Mode> modes;

    /**变温扩增下的各个step数据*/
    private List<Stage>  steps;

    /**熔解曲线下的开始温度和结束温度*/
    private String startTemperature;
    private String endTemperature;

    public ExpeSettingSecondInfo(){
     /*   modes=new ArrayList<>();
        steps=new ArrayList<>();*/
    }
    protected ExpeSettingSecondInfo(Parcel in) {

        modes=in.readArrayList(Mode.class.getClassLoader());
        steps=in.readArrayList(Stage.class.getClassLoader());
        startTemperature = in.readString();
        endTemperature = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(modes);
        dest.writeList(steps);
        dest.writeString(startTemperature);
        dest.writeString(endTemperature);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExpeSettingSecondInfo> CREATOR = new Creator<ExpeSettingSecondInfo>() {
        @Override
        public ExpeSettingSecondInfo createFromParcel(Parcel in) {
            return new ExpeSettingSecondInfo(in);
        }

        @Override
        public ExpeSettingSecondInfo[] newArray(int size) {
            return new ExpeSettingSecondInfo[size];
        }
    };

    public List<Mode> getModes() {
        return modes;
    }

    public void setModes(List<Mode> modes) {
        this.modes = modes;
    }

    public List<Stage> getSteps() {
        return steps;
    }


    public List<Stage> getCyclingSteps(){
        List<Stage> cyclingStageList=new ArrayList<>();
        cyclingStageList.addAll(steps);
        cyclingStageList.remove(0);
        cyclingStageList.remove(cyclingStageList.size()-1);
        return cyclingStageList;

    }

    public void setSteps(List<Stage> steps) {
        this.steps = steps;
    }

    public String getStartTemperature() {
        return startTemperature;
    }

    public void setStartTemperature(String startTemperature) {
        this.startTemperature = startTemperature;
    }

    public String getEndTemperature() {
        return endTemperature;
    }

    public void setEndTemperature(String endTemperature) {
        this.endTemperature = endTemperature;
    }
}
