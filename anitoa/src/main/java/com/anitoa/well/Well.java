package com.anitoa.well;

import android.util.Log;

import com.anitoa.bean.FlashData;

import java.util.List;

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

    public abstract List<String>  getKsList();

    public static Well getWell() {
        Well well=null;
        int numWell =  FlashData.NUM_WELLS;
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
            Log.e("Well","UnsupportedDeviceException-->" + "Unsupported Device numWell :" + numWell);
            well = new SixteenWell();
            //throw new UnsupportedDeviceException("Unsupported Device numWell :"+numWell);
        }
        return well;
    }



}
