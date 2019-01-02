package com.wind.data.expe.bean;

public class Sample {
    public static final int TYPE_A=0;
    public static final int TYPE_B=1;

    private String name;
    private int type;//样本类型，
    public String getName() {
        return name;
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
}
