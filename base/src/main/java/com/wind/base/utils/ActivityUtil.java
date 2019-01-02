package com.wind.base.utils;

import android.app.Activity;

/**
 * Created by wind on 2018/3/12.
 */

public class ActivityUtil {

    public static boolean isFinish(Activity activity){
        return activity==null?true:activity.isFinishing();
    }

    public static void finish(Activity activity){
        activity.finish();
    }

}
