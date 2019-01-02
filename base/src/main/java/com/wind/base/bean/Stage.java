package com.wind.base.bean;

//import com.jz.experiment.widget.VernierDragLayout;

import com.wind.base.adapter.DisplayItem;
import com.wind.base.widget.VernierDragLayout;

public class Stage implements DisplayItem {
    public static final int TYPE_START=0;
    public static final int TYPE_CYCLING=1;
    public static final int TYPE_PART=2;
    public static final int TYPE_END=3;

    private int type;//stage类型

    private float startScale;
    private float curScale;
    public Stage(){
        startScale=-1;
        curScale=-1;
    }

    private int id;
    private String stepName;
    /**上游stage
    private Stage prev;
     */
    /**下游stage*/
    private Stage next;

    private VernierDragLayout layout;
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
}
