package com.wind.base.utils;

import android.app.Activity;

import com.wind.base.R;
import com.wind.toastlib.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;

public class Exiter {

    /**
     * 双击退出函数
     */
    private static boolean isExit = false;
    public static void exit2Click(Activity activity) {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            ToastUtil.showToast(activity, R.string.exit_app_confirm);
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            activity.finish();
        }
    }
}
