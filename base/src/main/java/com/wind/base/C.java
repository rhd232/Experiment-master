package com.wind.base;

import android.os.Environment;

/**
 * Created by wind on 2017/3/1.
 */

public class C {
    public static class A4{
        public static final int WIDTH=210;//mm  需要根据屏幕密度转换成像素
        public static final int HEIGHT=297;//mm
        //每英寸25.4毫米
        public static final float MM_PER_IN=25.4f; //mm
    }

    public static class Char{
        public static final String NEW_LINE="\r\n";
    }
    public static class Config{
        public static final String DEFAULT_USERNAME="admin";
        public static final String DEFAULT_PWD="123456";
    }

    public static class FORMAT{
        public static final String GIF = ".gif";
    }
    public static class Version{
        public static final String API_VERSION="1.2";
        public static final String APP_VERSION = "1.1.0";
    }
    public static class Pack{
        public static String PACK_NAME_WEIBO="com.sina.weibo";
        public static String PACK_NAME_WEIXIN="com.tencent.mm";
        public static String PACK_NAME_QQ="com.tencent.mobileqq";

        public static final String PACK_NAME_MU="com.marryu";
    }
    public static class Key{
        public static final String UMENG_APPKEY="58cf4bef3eae252b79000fca";

        public static final String WEIXIN_APPKEY="wx3d2f8e6ff9127c85";
        public static final String WEIXIN_SECRET="67a02db20d5a9631eb821b749b7c26f9";

        public static final String WEIBO_APPKEY="3819362800";
        public static final String WEIBO_SECRET="2489fffdbdd75dce29f1e845ac378436";


        public static final int REQUESTCODE_VIPCENTER = 1000;


        public static final String SPLIT_CHAR = "#";
        //是否错误重试的layout
        public static final String SHOW_ERROR_RETRY_LAYOUT = "10";
    }
    public static class Value{
        public static final String APP_FOLDER= Environment.getExternalStorageDirectory().
                getAbsolutePath()+"/anitoa/";
        public static final String IMAGE_DATA=APP_FOLDER+"imageData/";
        public static final String REPORT_FOLDER=APP_FOLDER+"report/";
        public static final String TRIM_FOLDER=APP_FOLDER+"trim/";
        public static final String TEMP_FOLDER=APP_FOLDER+".temp/";
        public static final int MAX_PHOTO_COUNT = 9;

        public static final int REQUEST_CODE_GALLERY=100;
        public static final int REQUEST_CODE_CAMERA=101;

        //sp filename
        public static final String SP_FILENAME = "config";

        public static final String DATA_PREFIX="aa";
        public static final String DATA_SUFFIX="1717";
    }
    public static class PREF_KEY{

        /**
         * 地理位置相关
         */
        public static final String LOCATION_LATITUDE="latitude";
        public static final String LOCATION_LONGITUDE="longitude";
        public static final String LOCATION_PROVINCE="province";
        public static final String LOCATION_CITY="city";


        /**
         * 最后连接的蓝牙设备
         */
        public static final String DEVICE_NAME="device_name";
        public static final String DEVICE_ADDRESS="device_address";


        public static final String PREF_KEY_USB_DEVICE_NAME="pref_key_usb_device_name";


        /**
         * 报告中的字段
         */
        public static final String REPORT_COMPANY="report_company";
        public static final String REPORT_EXPE_NAME="report_expe_name";
    }
    public static class Api{
        /**
         * 测试地址
         */
        static String DEBUG_BASE_URL="";
        public static String IMAGE_SERVER_URL="";
        /**
         * 线上地址
         */
        static String RELEASE_BASE_URL="";

        public static String getBaseUrl(){
            return DEBUG_BASE_URL;
        }



    }



}
