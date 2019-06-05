package com.wind.base.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;

/**
 * Created by wind on 2018/3/12.
 */

public class ActivityUtil {

    public static boolean isFinish(Activity activity){
        return activity==null?true:activity.isFinishing();
    }

    public static void finish(Activity activity){
        if (activity!=null)
            activity.finish();
    }



    /**
     * 判断指定的activity是否是横屏
     * @param activity
     * @return
     */
    public static boolean isLandscape(Activity activity){
        return activity.getRequestedOrientation()== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }
}
