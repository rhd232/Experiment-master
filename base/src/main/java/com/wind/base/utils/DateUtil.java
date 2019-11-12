package com.wind.base.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by wind on 2016/11/4.
 */

public class DateUtil {

    public static String get(){
        return get(new Date().getTime());
    }

    public static String get(long milliTime){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy.MM.dd");
        String date=dateFormat.format(new Date(milliTime));
        return date;
    }
    public static String get(long milliTime,String pattern){
        SimpleDateFormat dateFormat=new SimpleDateFormat(pattern);
        String date=dateFormat.format(new Date(milliTime));
        return date;
    }
    public static String getDateTime(long milliTime){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String date=dateFormat.format(new Date(milliTime));
        return date;
    }
    public static String getYearAndMonth(long milliTime){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy.MM.dd");
        String date=dateFormat.format(new Date(milliTime));
        return date;
    }

    public static String getTodayTime(long milliTime) {
        SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
        String date=dateFormat.format(new Date(milliTime));
        return date;
    }

    public static String getMonthDay(long milliTime) {
        SimpleDateFormat dateFormat=new SimpleDateFormat("MM月dd日");
        String date=dateFormat.format(new Date(milliTime));
        return date;
    }

    public static String getHowLong(long milliTimeOne,long milliTimeOther) {
        String retVal="";
        long delta=milliTimeOne-milliTimeOther;
        long deltaDay=delta / (1000 * 60 * 60 * 24);
        if (deltaDay>0&&deltaDay<3){
            retVal=deltaDay+"天前";
        }else if(deltaDay>=3){
            retVal="3天前";
        }else {
            long hours=delta / (1000 * 60 * 60);
            if (hours==0){
                long min=delta / (1000 * 60);
                if (min==0){
                    retVal="片刻之前";
                }else {
                    retVal=min+"分钟之前";
                }

            }else {
                retVal=hours+"小时之前";
            }

        }

        return retVal;
    }

    public static String getHowLongOrDate(long milliTimeOne,long milliTimeOther) {
        String retVal="";
        long delta=milliTimeOne-milliTimeOther;
        long deltaDay=delta / (1000 * 60 * 60 * 24);
        if (deltaDay>0&&deltaDay<2){
            retVal=deltaDay+"天之前";
        }else if(deltaDay>=2){
            return getYearAndMonth(milliTimeOther);
        }else {
            long hours=delta / (1000 * 60 * 60);
            if (hours==0){
                long min=delta / (1000 * 60);
                if (min==0){
                    retVal="刚刚";
                }else {
                    retVal=min+"分钟之前";
                }

            }else {
                retVal=hours+"小时之前";
            }

        }

        return retVal;
    }

    private static boolean isSameDay(long inputTime) {

        TimeInfo tStartAndEndTime = getTodayStartAndEndTime();
        if(inputTime>tStartAndEndTime.getStartTime()&&inputTime<tStartAndEndTime.getEndTime())
            return true;
        return false;
    }

    private static boolean isYesterday(long inputTime) {
        TimeInfo yStartAndEndTime = getYesterdayStartAndEndTime();
        if(inputTime>yStartAndEndTime.getStartTime()&&inputTime<yStartAndEndTime.getEndTime())
            return true;
        return false;
    }


    /**
     * 是否是同一天
     * @param date1
     * @param date2
     * @return
     */
    private static boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2
                .get(Calendar.DAY_OF_MONTH);

        return isSameDate;
    }

    private static boolean isSameYear(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        return isSameYear;
    }

    private static class TimeInfo {
        private long startTime;
        private long endTime;

        public TimeInfo() {
        }

        public long getStartTime() {
            return this.startTime;
        }

        public void setStartTime(long var1) {
            this.startTime = var1;
        }

        public long getEndTime() {
            return this.endTime;
        }

        public void setEndTime(long var1) {
            this.endTime = var1;
        }
    }

    public static TimeInfo getTodayStartAndEndTime() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar1.getTime();
        long startTime = startDate.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar2.getTime();
        long endTime = endDate.getTime();
        TimeInfo info = new TimeInfo();
        info.setStartTime(startTime);
        info.setEndTime(endTime);
        return info;
    }

    public static TimeInfo getYesterdayStartAndEndTime() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DATE, -1);
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);

        Date startDate = calendar1.getTime();
        long startTime = startDate.getTime();

        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(Calendar.DATE, -1);
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar2.getTime();
        long endTime = endDate.getTime();
        TimeInfo info = new TimeInfo();
        info.setStartTime(startTime);
        info.setEndTime(endTime);
        return info;
    }

    public static long getToadyStartTime(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getToadyEndime(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return  cal.getTimeInMillis();
    }
}
