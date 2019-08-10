package com.wind.base.bean;

import android.os.Parcel;
import java.util.ArrayList;
import java.util.List;

public class CyclingStage extends Stage {

    private int serialNumber;//CyclingStage编号
    private int cyclingCount;//循环次数
   // private PartStage picStage;//拍照阶段
    private List<PartStage> partStageList;
    public CyclingStage() {
        setType(TYPE_CYCLING);
        partStageList = new ArrayList<>();
        cyclingCount=1;
    }

    public CyclingStage(Parcel in){
        super(in);
        serialNumber=in.readInt();
        cyclingCount=in.readInt();
        partStageList=in.readArrayList(PartStage.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(serialNumber);
        dest.writeInt(cyclingCount);
        dest.writeList(partStageList);
    }

    public static final Creator<Stage> CREATOR = new Creator<Stage>() {
        @Override
        public Stage createFromParcel(Parcel in) {
            return new CyclingStage(in);
        }

        @Override
        public Stage[] newArray(int size) {
            return new CyclingStage[size];
        }
    };

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



    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
}
