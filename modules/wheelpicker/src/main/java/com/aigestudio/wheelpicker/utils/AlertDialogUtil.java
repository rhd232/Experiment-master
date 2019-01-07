package com.aigestudio.wheelpicker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by guodx on 2016/4/23.
 */
public class AlertDialogUtil {
    /**
     * 中规中矩的弹出框，显示在屏幕中间
     *
     * @param context
     * @param layoutRes
     */
    public static AlertDialog showAlertDialog(Context context, @LayoutRes int layoutRes) {
        return showAlertDialog(context, layoutRes, false);
    }

    public static AlertDialog showAlertDialog(Context context, @LayoutRes int layoutRes, boolean canceledOnTouchOutside) {
        return showAlertDialog(context, layoutRes, canceledOnTouchOutside, false);
    }

    public static AlertDialog showAlertDialog(Context context, @LayoutRes int layoutRes, boolean canceledOnTouchOutside, boolean isShowParentBottom) {
        return showAlertDialog(context, layoutRes, canceledOnTouchOutside, isShowParentBottom, false);
    }

    public static AlertDialog showAlertDialog(Context context, @LayoutRes int layoutRes, boolean canceledOnTouchOutside, boolean isShowParentBottom, boolean focusabled) {
        return showAlertDialog(context, layoutRes, canceledOnTouchOutside, isShowParentBottom, focusabled, 0);
    }

    /**
     * @param context
     * @param layoutRes              布局资源
     * @param canceledOnTouchOutside 窗口外部是否可以取消
     * @param isShowParentBottom     是否显示在屏幕底部
     * @param animResId              动画效果资源
     */
    public static AlertDialog showAlertDialog(Context context, @LayoutRes int layoutRes,
                                              boolean canceledOnTouchOutside, boolean isShowParentBottom, boolean focusabled, @StyleRes int animResId) {
        return showAlertDialog(context, layoutRes, canceledOnTouchOutside, isShowParentBottom, focusabled, animResId, false);
    }

    public static AlertDialog showAlertDialog(Context context, @LayoutRes int layoutRes,
                                              boolean canceledOnTouchOutside, boolean isShowParentBottom, boolean focusabled, @StyleRes int animResId,
                                              boolean isFullScreen) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (isShowParentBottom) {
            // 处理左右边缘空隙
            WindowManager.LayoutParams wl = alertDialog.getWindow().getAttributes();
            wl.x = 0;
            // 以下这两句是为了保证按钮可以水平满屏
            wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            wl.gravity = Gravity.BOTTOM;
            // 设置显示位置
            alertDialog.onWindowAttributesChanged(wl);
        } else if (isFullScreen) {
            // 为了处理状态栏
            Window window = alertDialog.getWindow();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 5.0以上
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                window.setStatusBarColor(Color.TRANSPARENT);
            }

            WindowManager.LayoutParams wl = window.getAttributes();
            wl.x = 0;
            // 以下这两句是为了保证按钮可以水平满屏
            wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
            wl.height = ViewGroup.LayoutParams.MATCH_PARENT;
            // 设置显示位置
            alertDialog.onWindowAttributesChanged(wl);
        }else {
            Window window = alertDialog.getWindow();
            WindowManager.LayoutParams wl = window.getAttributes();
            // 以下这两句是为了保证按钮可以水平满屏
            //wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager m = window.getWindowManager();
            m.getDefaultDisplay().getMetrics(dm);
            wl.width = (int) ( dm.widthPixels* 0.8); // 宽度设置为屏幕的0.8
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            wl.gravity = Gravity.CENTER;
            // 设置显示位置
            alertDialog.onWindowAttributesChanged(wl);
        }


        alertDialog.getWindow().setContentView(layoutRes);
        // 进入出去 动画效果
        if (animResId != 0) {
            alertDialog.getWindow().setWindowAnimations(animResId);
        }

        if (focusabled) {
            // 解决输入法聚焦问题
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return alertDialog;
    }
}
