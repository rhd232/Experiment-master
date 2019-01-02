package com.wind.toastlib;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by wind on 2018/2/27.
 * 原生Toast PK 自定义Toast
 * <p>
 * 不使用原生Toast的原因是某些国产机器如vivo，oppo默认关闭了通知栏权限，导致默认不显示Toast的尴尬
 * 不使用windowmanager的addView方法添加类型为TYPE_TOAST窗口的原因。在android7.0以上WMS的addWindow方法中做了更改，不允许添加。
 * 只能使用自定义Toast了。思想是在view tree中加入一个view实现toast效果，参考{@link android.support.design.widget.Snackbar}
 */
public class ToastUtil {

    public static void showToast(Activity activity, int resId) {
        showToast(activity, activity.getResources().getString(resId));
    }

    public static void showToast(Fragment fragment, int resId) {
        showToast(fragment, fragment.getResources().getString(resId));
    }

    public static void showToast(Fragment fragment, String msg) {

        if (fragment != null && fragment.getView() != null) {
            if (fragment.getView().findViewById(R.id.toast_container) != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    showFakeToast(fragment.getView().findViewById(R.id.toast_container)
                            , msg);
                } else {
                    boolean isNotificationEnabled = NotificationUtil.isNotificationEnabled(fragment.getActivity());
                    if (isNotificationEnabled) {
                        //Toast.makeText(fragment.getActivity(), msg, Toast.LENGTH_LONG).show();
                        showSystemToast(fragment.getActivity(),msg);
                    } else {
                        showFakeToast(fragment
                                        .getView()
                                        .findViewById(R.id.toast_container)
                                , msg);
                    }
                }
            } else {
                showToast(fragment.getActivity(), msg);
            }

        }

    }

    public static void showToast(Activity activity, String msg) {
        //判断是否有显示悬浮窗的权限
        //android5.0以下无法获取是否打开系统通知权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            showFakeToast(activity
                            .getWindow()
                            .getDecorView()
                            .findViewById(android.R.id.content)
                    , msg);

        } else  {
            boolean isNotificationEnabled = NotificationUtil.isNotificationEnabled(activity);
            if (isNotificationEnabled) {
               // Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                showSystemToast(activity,msg);
            } else {
                showFakeToast(activity
                                .getWindow()
                                .getDecorView()
                                .findViewById(android.R.id.content)
                        , msg);
            }
        }
    }

    private static void showFakeToast(View view, String msg) {
        TransientFrame
                .make(view,
                        msg, TransientFrame.LENGTH_SHORT)
                .show();
    }


    private static void showSystemToast(Context context,String msg){
        View toastView = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        TextView tv = (TextView) toastView.findViewById(R.id.tv);
        tv.setText(msg);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER,0,0);
        tv.setAlpha(0.9f);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastView);
        toast.show();
    }
}
