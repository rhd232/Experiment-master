package com.anitoa.well;

import java.util.ArrayList;
import java.util.List;

public class EightWell extends Well {

    public EightWell() {
        super(8);
    }

    @Override
    public int getWellIndex(String wellName) {
        int ksindex = -1;
        switch (wellName)
        {
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
            case "B1":
                ksindex = 4;
                break;
            case "B2":
                ksindex = 5;
                break;
            case "B3":
                ksindex = 6;
                break;
            case "B4":
                ksindex = 7;
                break;
        }

        return ksindex;
    }

    @Override
    public List<String> getKsList() {
        List<String> KSList=new ArrayList<>();
        KSList.add("A1");
        KSList.add("A2");
        KSList.add("A3");
        KSList.add("A4");

        KSList.add("B1");
        KSList.add("B2");
        KSList.add("B3");
        KSList.add("B4");

        return KSList;
    }
}
