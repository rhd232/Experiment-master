package com.wind.data.expe.bean;

import com.github.mikephil.charting.data.Entry;

public class ColorfulEntry {
    private Entry entry;

    private int color;
    private String wellName;
    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getWellName() {
        return wellName;
    }

    public void setWellName(String wellName) {
        this.wellName = wellName;
    }
}
