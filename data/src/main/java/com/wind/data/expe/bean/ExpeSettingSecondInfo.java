package com.wind.data.expe.bean;

import com.wind.base.bean.Stage;

import java.util.List;

public class ExpeSettingSecondInfo {
    /**选择的模式*/
    private List<Mode> modes;

    /**变温扩增下的各个step数据*/
    private List<Stage>  steps;

    /**熔解曲线下的开始温度和结束温度*/
    private String startTemperature;
    private String endTemperature;


    public List<Mode> getModes() {
        return modes;
    }

    public void setModes(List<Mode> modes) {
        this.modes = modes;
    }

    public List<Stage> getSteps() {
        return steps;
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
