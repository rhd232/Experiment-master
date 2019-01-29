package com.jz.experiment.module.expe.event;

import java.util.List;

public class FilterEvent {

    private List<String> ChanList;
    private List<String> KSList;
    public FilterEvent(List<String> ChanList, List<String> KSList){
        this.ChanList=ChanList;
        this.KSList=KSList;
    }

    public List<String> getChanList() {
        return ChanList;
    }

    public void setChanList(List<String> chanList) {
        ChanList = chanList;
    }

    public List<String> getKSList() {
        return KSList;
    }

    public void setKSList(List<String> KSList) {
        this.KSList = KSList;
    }
}
