package com.wind.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wind on 16/6/8.
 */
public class AppUtil {

    /**
     * 获取app版本号
     * @param context
     * @return
     */
    public static int getAppVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static String getAppVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }



    public static String getAppName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String name = info.applicationInfo.loadLabel(manager).toString();
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * 获取app包名
     * @param context
     * @return
     */
    public static String getAppPackageName(Context context){
        return context.getPackageName();
    }


    /*
     * 唯一的设备ID：
     * GSM手机的 IMEI 和 CDMA手机的 MEID.
     * Return null if device ID is not available.
     */
    public static String getDeviceId(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId= tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)){
            deviceId=getMacAddress(context);
        }
        return deviceId;
    }
    /**
     * 获取设备mac地址
     *
     * @return
     */
    public static String getMacAddress(Context context) {
        if (context==null){
            return getLocalMacAddressFromIp();
        }
        //判断是否有wifi链接
        if (NetUtil.isWifiConnected(context)) {
            return getLocalMacAddressFromWifiInfo(context);
        } else {
            return getLocalMacAddressFromIp();
        }

    }


    //根据Wifi信息获取本地Mac
    public static String getLocalMacAddressFromWifiInfo(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac_s = info.getMacAddress();
        if (TextUtils.isEmpty(mac_s)) {
            mac_s = "asfag3235234fsd";
        }
        return mac_s;
    }

    //根据IP获取本地Mac
    public static String getLocalMacAddressFromIp() {
        String mac_s = "";
        try {
            byte[] mac;
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
        } catch (Exception e) {
            e.printStackTrace();

        }
        if (TextUtils.isEmpty(mac_s)) {
            mac_s = "asfag3235234fsd";
        }
        return mac_s;
    }

    public static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs = hs.append("0").append(stmp);
            else {
                hs = hs.append(stmp);
            }
        }
        return String.valueOf(hs);
    }

    /**
     * 获取主机ip地址
     *
     * @return
     */
    public static String getLocalIpAddress() {

        String ipaddress;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().toLowerCase().equals("eth0") || intf.getName().toLowerCase().equals("wlan0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            ipaddress = inetAddress.getHostAddress().toString();
                            if (!ipaddress.contains("::")) {// ipV6的地址
                                return ipaddress;
                            }
                        }
                    }
                } else {
                    continue;
                }
            }

        } catch (SocketException ex) {

        } catch (Exception e) {

        }
        return null;
    }
    /**
     * 获取屏幕像素信息
     * @param context
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context){
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);

      /*  int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        float density = metric.density;      // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240） */

        return metric;
    }


    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 检查包名为packageName的app是否已安装
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            packageInfo = null;
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 根据资源名称获取资源对于的id
     * @param packageName
     * @param className
     * @param idName
     * @return
     */
    public static int getComponentID(String packageName, String className,
                                   String idName) {
        int id = 0;
        try {
            Class<?> cls = Class.forName(packageName + ".R$" + className);
            id = cls.getField(idName).getInt(cls);
        } catch (Exception e) {
            //LogUtil.LogPrint(LogUtil.LOG_ERROR, "缺少" + idName + "文件!");
            e.printStackTrace();
        }
        return id;
    }


    /**
     * 获取程序签名
     * @param context
     * @return
     */
    public static String getSignInfo(Context context) {
        String signkey = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(getAppPackageName(context), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            //signkey = parseSignature(sign.toByteArray());
            signkey=Md5Util.bytesMd5Hex(sign.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signkey;
    }


    private static String parseSignature(byte[] signature) {
        String pubKey = "";
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            pubKey = cert.getPublicKey().toString();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return pubKey;
    }



    public static int getScreenWidth(Activity context){
        Display display=context.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics=new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }
    public static int getScreenHeight(Activity context){
        Display display=context.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics=new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static boolean isActivityFinish(FragmentActivity activity) {
        if (activity==null||activity.isFinishing()){
            return true;
        }

        return false;
    }


    /**
     * 获取手机品牌
     * @return
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }
    /**
     * 获取手机型号
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * android系统版本号
     * @return
     */
    public static String getOsVersion(){
        return android.os.Build.VERSION.RELEASE;
    }


}
