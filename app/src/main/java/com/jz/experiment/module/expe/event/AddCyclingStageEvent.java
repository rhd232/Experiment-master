package com.jz.experiment.module.expe.event;

public class AddCyclingStageEvent {

    private int position;
    public AddCyclingStageEvent(int position){
        this.position=position;
    }

    public int getPosition() {
        return position;
    }
}
