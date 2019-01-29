package com.wind.base.bean;

//import com.jz.experiment.widget.VernierDragLayout;

import android.os.Parcel;
import android.os.Parcelable;

import com.wind.base.adapter.DisplayItem;
import com.wind.base.widget.VernierDragLayout;

public class Stage implements DisplayItem,Parcelable {
    public static final int TYPE_START=0;
    public static final int TYPE_CYCLING=1;
    public static final int TYPE_PART=2;
    public static final int TYPE_END=3;

    private int type;//stage类型

    private float startScale;
    private float curScale;

    /**温度*/
    private float temp;
    /**持续时间*/
    private short during;

    public Stage(){
        startScale=-1;
        curScale=-1;
        during=10;//默认10s
    }

    private int id;
    private String stepName;
    /**上游stage
    private Stage prev;
     */
    /**下游stage*/
    private Stage next;

    private transient VernierDragLayout layout;


    protected Stage(Parcel in) {
        type = in.readInt();
        startScale = in.readFloat();
        curScale = in.readFloat();
        temp = in.readFloat();
        during = (short) in.readInt();
        id = in.readInt();
        stepName = in.readString();
        next = in.readParcelable(Stage.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeFloat(startScale);
        dest.writeFloat(curScale);
        dest.writeFloat(temp);
        dest.writeInt((int) during);
        dest.writeInt(id);
        dest.writeString(stepName);
        dest.writeParcelable(next, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Stage> CREATOR = new Creator<Stage>() {
        @Override
        public Stage createFromParcel(Parcel in) {
            return new Stage(in);
        }

        @Override
        public Stage[] newArray(int size) {
            return new Stage[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

   /* public Stage getPrev() {
        return prev;
    }*/

    /*public void setPrev(Stage prev) {
        this.prev = prev;
    }*/

    public Stage getNext() {
        return next;
    }

    public void setNext(Stage next) {
        this.next = next;
    }

    public VernierDragLayout getLayout() {
        return layout;
    }

    public void setLayout(VernierDragLayout layout) {
        this.layout = layout;
    }

    public float getStartScale() {
        return startScale;
    }

    public void setStartScale(float startScale) {
        this.startScale = startScale;
    }

    public float getCurScale() {
        return curScale;
    }

    public void setCurScale(float curScale) {
        this.curScale = curScale;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public short getDuring() {
        return during;
    }

    public void setDuring(short during) {
        this.during = during;
    }
}
