package com.wind.base.bean;

import java.util.ArrayList;
import java.util.List;

public class CyclingStage extends Stage {

    private int serialNumber;//CyclingStage编号
    private List<PartStage> partStageList;
    private int cyclingCount;//循环次数
    private PartStage picStage;//拍照阶段
    public CyclingStage() {
        setType(TYPE_CYCLING);
        partStageList = new ArrayList<>();
    }


    public List<PartStage> getPartStageList() {
        return partStageList;
    }


    public void addChildStage(int position, PartStage partStage) {
        partStageList.add(position, partStage);
    }

    public void removeChildStage(int position) {
        partStageList.remove(position);
    }

   /* public void setVernierAdapter(VernierAdapter adapter) {
        this.adapter = adapter;
    }*/

   /* public VernierAdapter getAdapter() {
        return adapter;
    }*/

    public List<PartStage> getChildStages() {
        List<PartStage> list = new ArrayList<>();

        for (int i = 0; i < partStageList.size(); i++) {
            PartStage partStage = partStageList.get(i);
            partStage.setSerialNumber(i);
            if (i !=  partStageList.size() - 1) {
                partStage.setNext(partStageList.get(i + 1));
            }
            list.add(partStage);
        }
        return list;
    }

    public int getCyclingCount() {
        return cyclingCount;
    }

    public void setCyclingCount(int cyclingCount) {
        this.cyclingCount = cyclingCount;
    }

    public PartStage getPicStage() {
        return picStage;
    }

    public void setPicStage(PartStage picStage) {
        this.picStage = picStage;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
}
