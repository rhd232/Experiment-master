package com.jz.experiment.module.expe.event;

public class AddStartStageEvent {

    private int position;
    public AddStartStageEvent(int position){
        this.position=position;
    }

    public int getPosition() {
        return position;
    }
}
