package com.espressif.iot.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * both of input and output long values are for UTC time
 * 
 * @author afunx
 * 
 */
public class TimeUtil
{
    
    private static final String defaultPattern = "yyyy-MM-dd HH:mm:ss";
    
    public static final String ISO_8601_Pattern = "yyyy-MM-dd'T'HH:mm:ss";
    
    public static final String YEAR_MONTH_DAY_Pattern = "yyyy-MM-dd";
    
    private static final String defaultOriginTime = "2014-06-01T00:00:00";
    
    private static final String defaultLastTime = "2020-01-01T00:00:00";
    
    public static final long ONE_SECOND_LONG_VALUE = 1000l;
    
    public static final long ONE_MINUTE_LONG_VALUE = ONE_SECOND_LONG_VALUE * 60;
    
    public static final long ONE_HOUR_LONG_VALUE = ONE_MINUTE_LONG_VALUE * 60;
    
    public static final long ONE_DAY_LONG_VALUE = ONE_HOUR_LONG_VALUE * 24;
    
    /**
     * get the origin time of long, "2014-06-01 00:00:00 GMT" for the device's data can't be earlier than 2014-06-01
     * 
     * @return "2014-06-01 00:00:00 GMT"
     */
    public static String getOriginTime()
    {
        return defaultOriginTime + getTimeZoneOffsetStr();
    }
    
    /**
     * get the last time of long, "2020-01-01 00:00:00 GMT" as the device's last data
     * 
     * @return "2020-01-01 00:00:00 GMT"
     */
    public static String getLastTime()
    {
        return defaultLastTime + getTimeZoneOffsetStr();
    }
    
    /**
     * @return the time zone offset string like "+09:00", "+00:00", "-11:00" and etc.
     */
    private static String getTimeZoneOffsetStr()
    {
        long timeZone = TimeZone.getDefault().getRawOffset() / ONE_HOUR_LONG_VALUE;
        StringBuilder sb = new StringBuilder();
        // -11 ~ -10
        if (-11 <= timeZone && timeZone <= -10)
        {
            sb.append(timeZone);
        }
        // -9 ~ -1
        else if (-9 <= timeZone && timeZone <= -1)
        {
            sb.append("-0").append(-timeZone);
        }
        // 0 ~ 9
        else if (0 <= timeZone && timeZone <= 9)
        {
            sb.append("+0").append(timeZone);
        }
        // 10 ~ 13
        else if (10 <= timeZone && timeZone <= 13)
        {
            sb.append("+").append(timeZone);
        }
        sb.append(":00");
        return sb.toString();
    }
    
    /**
     * get the Date from dateStr and specified pattern
     * 
     * @param dateStr the String of date
     * @param pattern the pattern (e.g. "yyyy-MM-dd HH:mm:ss")
     * @return the date
     */
    private static Date getDate(String dateStr, String pattern)
    {
        if (pattern == null)
        {
            pattern = defaultPattern;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = null;
        try
        {
            date = sdf.parse(dateStr);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return date;
    }
    
    /**
     * get the Date from dateStr and specified pattern
     * 
     * @param dateStr the String of date
     * @param pattern the pattern (e.g. "yyyy-MM-dd HH:mm:ss")
     * @return the date long
     */
    public static long getLong(String dateStr, String pattern)
    {
        Date date = getDate(dateStr, pattern);
        long dateLong = date.getTime();
        return dateLong;
    }
    
    /**
     * get the Date String from dateLong
     * 
     * @param dateLong the number of milliseconds since Jan. 1, 1970 GMT.
     * @param pattern the pattern (e.g. "yyyy-MM-dd HH:mm:ss")
     * @return the date String
     */
    public static String getDateStr(long dateLong, String pattern)
    {
        if (pattern == null)
        {
            pattern = defaultPattern;
        }
        Date date = new Date(dateLong);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        String dateStr = sdf.format(date);
        if (pattern == ISO_8601_Pattern)
        {
            dateStr += getTimeZoneOffsetStr();
        }
        return dateStr;
    }
    
    /**
     * get the long from the year, monthOfyear and dayOfMonth
     * 
     * @param year the year
     * @param monthOfYear the month ( 0 means January, 1 means February and etc.)
     * @param dayOfMonth the day
     * @return the long
     */
    public static long getLong(int year, int monthOfYear, int dayOfMonth)
    {
        String dateStr = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        dateStr += " 00:00:00";
        return TimeUtil.getLong(dateStr, null);
    }
    
    /**
     * get the long at this day 00:00
     * 
     * @param dateLong the number of milliseconds since Jan. 1, 1970 GMT.
     * @return the long at this day 00:00
     */
    public static long getDayStartLong(long dateLong)
    {
        // dateLong -= getTimeZoneOffset();
        String yearStr = getDateStr(dateLong, "yyyy");
        String monthStr = getDateStr(dateLong, "MM");
        String dayStr = getDateStr(dateLong, "dd");
        int year = Integer.parseInt(yearStr);
        int month = Integer.parseInt(monthStr);
        int day = Integer.parseInt(dayStr);
        return getLong(year, month - 1, day);
    }
    
    /**
     * whether the time is the same date between dateLong1 and dateLong2
     * 
     * @param dateLong1 the number of milliseconds since Jan. 1, 1970 GMT.
     * @param dateLong2 the number of milliseconds since Jan. 1, 1970 GMT.
     * @return whether they are the same date
     */
    public static boolean isTheSameDate(long dateLong1, long dateLong2)
    {
        dateLong1 = getDayStartLong(dateLong1);
        dateLong2 = getDayStartLong(dateLong2);
        return dateLong1 == dateLong2;
    }
    
    /**
     * an adaptor of System.currentTimeMillis();
     * 
     * @return the current time long from Android System
     */
    public static long getSystemCurrentTimeLong()
    {
        return System.currentTimeMillis();
    }
    
    /**
     * get the round day of UTC time(floor)
     * 
     * @param dateLong the number of milliseconds since Jan. 1, 1970 GMT.
     * @return the floor of the dateLong to the UTC round day
     */
    public static long getUTCDayFloor(long dateLong)
    {
        return dateLong / ONE_DAY_LONG_VALUE * ONE_DAY_LONG_VALUE;
    }
    
    /**
     * get the round day of UTC time(ceiling)
     * 
     * @param dateLong the number of milliseconds since Jan. 1, 1970 GMT.
     * @return the ceiling of the dateLong to the UTC round day
     */
    public static long getUTCDayCeil(long dateLong)
    {
        return (dateLong + ONE_DAY_LONG_VALUE - 1) / ONE_DAY_LONG_VALUE * ONE_DAY_LONG_VALUE;
    }
    
    /**
     * check whether the time long is UTC day, UTC day means the time long is 00:00:00+00:00 for UTC time
     * 
     * @param dateLong the number of milliseconds since Jan. 1, 1970 GMT.
     * @return whether the dateLong is UTC day
     */
    public static boolean isUTCDay(long dateLong)
    {
        return dateLong == getUTCDayFloor(dateLong);
    }
    
}
