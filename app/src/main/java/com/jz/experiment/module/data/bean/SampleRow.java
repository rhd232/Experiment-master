package com.jz.experiment.module.data.bean;

import java.io.Serializable;

public class SampleRow implements Serializable {
    private String name;
    private String type;

    /**浓度*/
    private String concentration;

    private String ctValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public String getCtValue() {
        return ctValue;
    }

    public void setCtValue(String ctValue) {
        this.ctValue = ctValue;
    }
}
