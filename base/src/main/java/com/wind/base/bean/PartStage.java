package com.wind.base.bean;

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


   /* private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }*/

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
