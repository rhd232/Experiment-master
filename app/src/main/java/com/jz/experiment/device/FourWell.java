package com.jz.experiment.device;

public class FourWell extends Well {

    public FourWell() {
        super(4);
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

            case "B1":
                ksindex = 2;
                break;
            case "B2":
                ksindex = 3;
                break;

        }

        return ksindex;
    }
}
