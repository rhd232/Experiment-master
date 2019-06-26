package com.wind.data.expe.bean;

import java.io.Serializable;

public class Sample implements Serializable {
    public static final int TYPE_A=0;
    public static final int TYPE_B=1;

    private long id=HistoryExperiment.ID_NONE;

    private String name;
    private int type;//样本类型，
    private boolean enabled;

    public static final int CODE_DEFAULT=0;
    public static final int CODE_STANDARD=1;
    public static final int CODE_UNKWON=2;
    private int status;

    private int seq;//序号
    public String getName() {
        return name;
    }

    public String getSeqName(){
        String type=getType()==Sample.TYPE_A?"A":"B";
        return type+seq;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
