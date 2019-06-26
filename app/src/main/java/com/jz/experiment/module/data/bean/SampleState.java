package com.jz.experiment.module.data.bean;

public class SampleState {
    public static final int CODE_DEFAULT=0;
    public static final int CODE_STANDARD=1;
    public static final int CODE_UNKWON=2;
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
