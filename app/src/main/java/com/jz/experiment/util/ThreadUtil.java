package com.jz.experiment.util;

public class ThreadUtil {

    public static void sleep(long milliTime){
        try {
            Thread.sleep(milliTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
