package com.jz.experiment.module.expe.event;

public class DelStartStageEvent {

    private int position;
    public DelStartStageEvent(int position){
        this.position=position;
    }

    public int getPosition() {
        return position;
    }
}
