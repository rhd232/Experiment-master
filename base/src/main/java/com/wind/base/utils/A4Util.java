package com.wind.base.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.wind.base.C;

public class A4Util {


    public static int getA4Width(Context context){

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        int widthPxInA4= (int) (C.A4.WIDTH/C.A4.MM_PER_IN  *densityDpi);

        return widthPxInA4;
    }
    public static int getA4Height(Context context){

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        int heightPxInA4= (int) (C.A4.HEIGHT/C.A4.MM_PER_IN  *densityDpi);

        return heightPxInA4;
    }
}
