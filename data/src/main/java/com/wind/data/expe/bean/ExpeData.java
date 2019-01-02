package com.wind.data.expe.bean;

public class ExpeData {

    private float xValue;
    private float yValue;

    public ExpeData(float xValue,float yValue){
        this.xValue=xValue;
        this.yValue=yValue;
    }

    public float getxValue() {
        return xValue;
    }

    public void setxValue(float xValue) {
        this.xValue = xValue;
    }

    public float getyValue() {
        return yValue;
    }

    public void setyValue(float yValue) {
        this.yValue = yValue;
    }
}
