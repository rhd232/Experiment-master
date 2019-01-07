package com.aigestudio.wheelpicker.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by guodx on 16/4/14.
 */
public class DateUtil {
    /**
     * 2016-05-01
     */
    public static final String FORMATTYPE_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String FORMATTYPE_YYYYMMDD = "yyyyMMdd";

    /**
     * 2016-05-01 16:34:43
     */
    public static final String FORMATTYPE_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 05月01日 周日
     */
    public static final String FORMATTYPE_MM_DD_E = "MM月dd日 E";
    /**
     * 05月01日 周日 17:00
     */
    public static final String FORMATTYPE_MM_DD_E_HH_MM = "MM月dd日 E HH:mm";
    public static final String FORMATTYPE_MM_DD_E_HH_MM_2 = "MM月dd日 E HH时mm分";
    /**
     * 05-01 17:00
     */
    public static final String FORMATTYPE_MM_DD_HH_MM = "MM-dd HH:mm";

    /**
     * 根据时间获得年龄
     *
     * @param formate
     * @param birthday
     * @return
     * @throws Exception
     */
    public static int getAge(String formate, String birthday) throws Exception {
        Date date = new SimpleDateFormat(formate).parse(birthday);
        return getAge(date);
    }

    /**
     * 根据时间获得年龄
     *
     * @param birthDay
     * @return
     * @throws Exception
     */
    public static int getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(
                    "出生时间大于当前时间!");
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;//注意此处，如果不加1的话计算结果是错误的
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                //monthNow==monthBirth
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                } else {
                    //do nothing
                }
            } else {
                //monthNow>monthBirth
                age--;
            }
        } else {
            //monthNow<monthBirth
            //donothing
        }

        return age;
    }

    /**
     * 获得10位时间戳
     *
     * @param time
     * @return
     */
    public static String getYYYYMMDDHHMMSS10Stamp(String time) throws ParseException {
        return getYYYYMMDDHHMMSS10Stamp(pareYYYYMMDDHHMMSS(time));
    }

    /**
     * 获得10位时间戳
     *
     * @param date
     * @return
     */
    public static String getYYYYMMDDHHMMSS10Stamp(Date date) {
        String times = date.getTime() + "";
        if (times.length() > 10) {
            times = times.substring(0, 10);
        }
        return times;
    }

    /**
     * 获得10位时间戳
     *
     * @param date
     * @return
     */
    public static String getYYYYMMDDHHMMSS10Stamp(Calendar date) {
        String times = date.getTime().getTime() + "";
        if (times.length() > 10) {
            times = times.substring(0, 10);
        }
        return times;
    }

    /**
     * 根据10位时间戳解析Date
     *
     * @param times
     * @return
     */
    public static Date parse10Stamp(long times) {
        return new Date(times * 1000);
    }

    public static Date pareYYYYMMDD(String value) throws ParseException {
        return pareFromStr(value, FORMATTYPE_YYYY_MM_DD);
    }

    public static Date pareYYYYMMDDHHMMSS(String value) throws ParseException {
        return pareFromStr(value, FORMATTYPE_YYYY_MM_DD_HH_MM_SS);
    }

    public static Date pareFromStr(String value, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(value);
    }

    public static String formatYYYYMMDD(Date date) {
        return formatToStr(date, FORMATTYPE_YYYY_MM_DD);
    }

    public static String formatMMDDE(Date date) {
        return formatToStr(date, FORMATTYPE_MM_DD_E);
    }

    public static String formatMMDDHHmm(Date date) {
        return formatToStr(date, FORMATTYPE_MM_DD_HH_MM);
    }

    public static String formatToStr(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 解析时间 一天内 "HH:mm"； 一年内 "MM-dd"；一年外 "yyyy-MM-dd"
     *
     * @param date
     */
    public static String parseDate(String date) {
        // 解析时间
        Date createDate;
        Long time = Long.parseLong(date);
        if (date.length() == 10) createDate = new Date(1000 * time);
        else createDate = new Date(time);
        //LogUtil.d("HomeDiscoveryAdapter", "time is : " + time);
        Date nowDate = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        // 比较
        if (df.format(nowDate).equals(df.format(createDate))) {
            df = new SimpleDateFormat("HH:mm"); // 显示时分
        } else {
            df = new SimpleDateFormat("yyyy");
            if (df.format(nowDate).equals(df.format(createDate))) {
                df = new SimpleDateFormat("MM-dd");// 显示月日
            } else {
                df = new SimpleDateFormat("yyyy-MM-dd");// 显示年月日
            }
        }
        return df.format(createDate);
    }

    /**
     * 获取明天日期
     *
     * @return
     */
    public static Date getTomorrowDate() {
        Calendar theCa = Calendar.getInstance();
        theCa.add(theCa.DATE, 1);
        return theCa.getTime();
    }


    public static int distace(int year) {
        int todayYear = Calendar.getInstance().get(Calendar.YEAR);
        return todayYear - year;
    }


}
