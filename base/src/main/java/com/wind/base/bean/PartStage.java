package com.wind.base.bean;

import android.os.Parcel;

public class PartStage extends Stage {

    public PartStage(){
        setType(TYPE_PART);
    }
    private int cyclingId;
    private int serialNumber;//PartStage编号
    /**
     * 该阶段是否需要拍照
     */
    private boolean takePic;

    public PartStage(Parcel in){
        super(in);
        cyclingId=in.readInt();
        serialNumber=in.readInt();
        takePic=in.readInt()==1?true:false;

    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(cyclingId);
        dest.writeInt(serialNumber);
        int pic=takePic?1:0;
        dest.writeInt(pic);

    }
    public static final Creator<Stage> CREATOR = new Creator<Stage>() {
        @Override
        public Stage createFromParcel(Parcel in) {
            return new PartStage(in);
        }

        @Override
        public Stage[] newArray(int size) {
            return new PartStage[size];
        }
    };
    public boolean isTakePic() {
        return takePic;
    }

    public void setTakePic(boolean takePic) {
        this.takePic = takePic;
    }


    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getCyclingId() {
        return cyclingId;
    }

    public void setCyclingId(int cyclingId) {
        this.cyclingId = cyclingId;
    }
}
