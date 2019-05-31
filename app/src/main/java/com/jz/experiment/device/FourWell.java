package com.jz.experiment.device;

import java.util.ArrayList;
import java.util.List;

public class FourWell extends Well {

    public FourWell() {
        super(4);
    }

    @Override
    public int getWellIndex(String wellName) {
        int ksindex = -1;
        switch (wellName) {
            case "A1":
                ksindex = 0;
                break;
            case "A2":
                ksindex = 1;
                break;
            case "A3":
                ksindex = 2;
                break;
            case "A4":
                ksindex = 3;
                break;

        }

        return ksindex;
    }

    public List<String> getKsList() {
        List<String> KSList = new ArrayList<>();

        KSList.add("A1");
        KSList.add("A2");
        KSList.add("A3");
        KSList.add("A4");


        return KSList;
    }
}
