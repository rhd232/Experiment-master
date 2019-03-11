package com.jz.experiment.device;

public class SixteenWell extends Well {

    public SixteenWell() {
        super(16);
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
            case "A5":
                ksindex = 4;
                break;
            case "A6":
                ksindex = 5;
                break;
            case "A7":
                ksindex = 6;
                break;
            case "A8":
                ksindex = 7;
                break;

            case "B1":
                ksindex = 8;
                break;
            case "B2":
                ksindex = 9;
                break;
            case "B3":
                ksindex = 10;
                break;
            case "B4":
                ksindex = 11;
                break;
            case "B5":
                ksindex = 12;
                break;
            case "B6":
                ksindex = 13;
                break;
            case "B7":
                ksindex = 14;
                break;
            case "B8":
                ksindex = 15;
                break;
        }

        return ksindex;
    }
}
