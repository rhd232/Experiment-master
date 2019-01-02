package com.wind.base.utils;

import com.orhanobut.logger.Logger;

/**
 * Created by shi on 2015/11/19.
 */
public class LogUtil {

    public static boolean isDebug=true;
    public static void e(String tag,String msg){
        if (isDebug){
            //Log.e(tag,msg);
            Logger.t(tag).e(msg);
        }
    }

    public static void e(String msg){
        if (isDebug){
            Logger.e(msg);
        }
    }

}
