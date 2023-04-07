package com.jimi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间工具类
 */
public class DateUtils {
    public final static String TIME_STR_T_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public final static String TIME_STR = "yyyy-MM-dd HH:mm:ss";

    /**
     * LocalDate 格式转成Date
     * @param localDate
     * @return
     */
    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate 格式转成Date
     * @param localDate
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime localDate) {
        return Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转成String，GMT标准时间
     * eg： 2023-04-04 13:00:00 --> 2023-04-04 05:00:00
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateToStringGMT(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    /**
     * Date 转成String，GMT+8 北京时间
     * eg：2023-04-04 13:00:00 --> 2023-04-04 13:00:00
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateToStringGMT8(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sdf.format(date);
    }

    /**
     * String 转成Date
     * eg: 2023-04-04T23:59:59Z 转成 Wed Apr 05 07:59:59 CST 2023
     *
     * @param time
     * @param format
     * @return
     */
    public static Date timeStrToDateGMT(String time, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = sdf.parse(time);
        return date;
    }

    /**
     * String 转成Date
     * eg:2023-04-04T23:59:59Z 转成 Tue Apr 04 23:59:59 CST 2023
     *
     * @param time
     * @param format
     * @return
     */
    public static Date timeStrToDateGMT8(String time, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Date date = sdf.parse(time);
        return date;
    }

    /**
     * 获取某一天的开始时间格式
     * eg：2023-03-29 得到 2023-03-29T00:00:00Z
     *
     * @param date yyyy-MM-dd
     * @return 2023-03-29T00:00:00Z
     */
    public static String getStartOfDay(String date) {
        LocalDate localDate = LocalDate.parse(date);
        Date toDate = localDateToDate(localDate);
        return dateToStringGMT8(toDate, TIME_STR_T_Z);
    }

    /**
     * 获取某一天的结束时间格式
     * eg：2023-03-29 得到 2023-03-29T23:59:59Z
     *
     * @param date yyyy-MM-dd
     * @return 2023-03-29T23:59:59Z
     */
    public static String getEndOfDay(String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime endOfDay = localDate.atTime(23, 59, 59);
        Date toDate = localDateTimeToDate(endOfDay);
        return dateToStringGMT8(toDate, TIME_STR_T_Z);
    }

    /**
     * 获取现在的标准时间格式GMT
     * @return @return 2023-03-29T16:59:59Z
     */
    public static String getNowFormat() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return dateToStringGMT(date, TIME_STR_T_Z);
    }

    /**
     * 获取今天凌晨标准时间格式GMT
     * eg：2023-04-04T23:59:59Z
     *
     * @return
     */
    public static String getTodayFormat() {
        LocalDate today = LocalDate.now();
        Date date = localDateToDate(today);
        return dateToStringGMT(date, TIME_STR_T_Z);
    }

    /**
     * 格式转换 2023-03-30T05:53:05Z 转成 yyyy-MM-dd HH:mm:ss
     * eg: 2023-04-04T23:59:59Z 转成 2023-04-04 23:59:59
     *
     * @param timeStamp
     * @return
     */
    public static String transUTCToStrGMT(String timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_STR);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = Date.from(Instant.parse(timeStamp));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /**
     * 格式转换 2023-03-30T05:53:05Z 转成 yyyy-MM-dd HH:mm:ss 并且进行GMT转成GMT+8
     * eg: 2023-04-04T23:59:59Z 转成 2023-04-05 07:59:59
     *
     * @param timeStamp
     * @return
     */
    public static String transUTCToStrGMT8(String timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_STR);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Date date = Date.from(Instant.parse(timeStamp));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /**
     *
     * eg:  2023-04-04 23:59:59 转成  2023-04-04T23:59:59Z
     *
     * @param timeStr
     * @return
     */
    public static String transStrToUTCGMT8(String timeStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_STR);
        SimpleDateFormat sdf1 = new SimpleDateFormat(TIME_STR_T_Z);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Date date = sdf.parse(timeStr);
        String formattedDate = sdf1.format(date);
        return formattedDate;
    }

    /**
     *
     * eg:  2023-04-04 23:59:59 转成  2023-04-04T15:59:59Z
     *
     * @param timeStr
     * @return
     */
    public static String transStrToUTCGMT(String timeStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_STR);
        Date date = sdf.parse(timeStr);
        return dateToStringGMT(date, TIME_STR_T_Z);
    }

    public static void main(String[] args) throws ParseException {
        String startOfDay = getStartOfDay("2023-03-29");
        String endOfDay = getEndOfDay("2023-03-29");
        String now = getNowFormat();
        System.out.println(startOfDay);
        System.out.println(endOfDay);
        System.out.println(now);
        System.out.println(getTodayFormat());
        String timeStamp = "2023-04-04T23:59:59Z";
        String timeStamp2 = "2023-04-04 23:59:59";
        System.out.println("timeStamp" + transUTCToStrGMT(timeStamp));
        System.out.println("timeStamp8" + transUTCToStrGMT8(timeStamp));
        System.out.println("timeStamp8-date" + timeStrToDateGMT8(timeStamp, TIME_STR_T_Z));
        System.out.println("timeStamp-date" + timeStrToDateGMT(timeStamp, TIME_STR_T_Z));
        System.out.println("GMT:" + dateToStringGMT(new Date(), TIME_STR));
        System.out.println("GMT8:" + dateToStringGMT8(new Date(), TIME_STR));
        System.out.println("timeStamp8-date" + timeStrToDateGMT8(timeStamp2, TIME_STR));
        System.out.println("timeStamp-date" + timeStrToDateGMT(timeStamp2, TIME_STR));
        System.out.println("timeStamp-transStrToUTCGMT8" + transStrToUTCGMT8(timeStamp2));
        System.out.println("timeStamp-transStrToUTCGMT" + transStrToUTCGMT(timeStamp2));
    }

}
