package com.wind.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;


import java.io.Serializable;

/**
 * Created by wind on 2017/11/30.
 */

public class Navigator {
    public static final String PARCELABLE_EXTRA_KEY="parcelable_extra_key";
    public static <T> T getSerializableExtra(Activity activity) {
        Serializable serializable = activity.getIntent().getSerializableExtra(Navigator.PARCELABLE_EXTRA_KEY);
        activity = null;
        return (T)serializable;
    }
    public static void putSerializableExtra(Intent intent, Serializable serializable) {
        if (serializable == null) return;
        intent.putExtra(PARCELABLE_EXTRA_KEY, serializable);
    }
    public static void navigate(Context context, Class clazz,Serializable serializable){
        Intent intent=new Intent(context,clazz);
        putSerializableExtra(intent,serializable);
        context.startActivity(intent);
    }
    public static void navigate(Context context, Class clazz){
        navigate(context,clazz,null);
    }



    public static void navigateToSettingDetail(Context context) {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }
            context.startActivity(localIntent);
    }

    public static void navigateForResult(Fragment fragment, Class clazz, int requestCode, Serializable serializable) {
        Intent intent=new Intent(fragment.getActivity(),clazz);
        putSerializableExtra(intent,serializable);
        fragment.startActivityForResult(intent,requestCode);
    }
    public static void navigateForResult(Activity activity, Class clazz,int requestCode,Serializable serializable) {
        Intent intent=new Intent(activity,clazz);
        putSerializableExtra(intent,serializable);
        activity.startActivityForResult(intent,requestCode);
    }
}
