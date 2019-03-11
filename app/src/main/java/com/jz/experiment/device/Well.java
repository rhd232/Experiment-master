package com.jz.experiment.device;

import com.jz.experiment.chart.CommData;

public abstract class Well {

    /**
     * 反应井数
     */
    private int numWell;

    public Well(int numWell) {
        this.numWell = numWell;
    }

    /**
     * 获取反应井index
     *
     * @return
     */
    public abstract int getWellIndex(String wellName);


    public static Well getWell() {
        Well well=null;
        int numWell = CommData.KsIndex;
        switch (numWell) {
            case 4:
                well = new FourWell();
                break;
            case 8:
                well = new EightWell();
                break;
            case 16:
                well = new SixteenWell();
                break;
        }
        if (well==null){
            throw new UnsupportedDeviceException("Unsupported Device numWell :"+numWell);
        }
        return well;
    }

}
