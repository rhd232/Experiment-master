package com.jz.experiment.module.expe.event;

public class DelCyclingStageEvent {

    private int position;
    public DelCyclingStageEvent(int position){
        this.position=position;
    }

    public int getPosition() {
        return position;
    }
}
