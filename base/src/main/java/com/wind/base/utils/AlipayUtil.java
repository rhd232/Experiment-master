package com.wind.base.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.wind.toastlib.ToastUtil;

/**
 * Created by wind on 2017/8/4.
 */

public class AlipayUtil {

    public static void withhold(Context context,String url){
        boolean isInstalled=AppUtil.isPackageInstalled(context,"com.eg.android.AlipayGphone");
        if (isInstalled){
            String alipayUrl="alipays://platformapi/startapp?appId=20000067&url=";
            alipayUrl=alipayUrl+ url;
           // alipayUrl=alipayUrl+ URLEncoder.encode(url);
            Uri uri = Uri.parse(alipayUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);

        }else {
            ToastUtil.showToast((Activity) context,"请先安装支付宝");
        }
    }
    //判断是否安装支付宝app
    public static boolean checkAliPayInstalled(Context context) {
        Uri uri = Uri.parse("alipays://platformapi/startApp");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        return componentName != null;
    }
}
