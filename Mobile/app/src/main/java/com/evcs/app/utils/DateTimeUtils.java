package com.evcs.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {
    
    public static final String DATE_FORMAT_API = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIME_FORMAT_DISPLAY = "HH:mm";
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATETIME_FORMAT_DISPLAY = "MMM dd, yyyy HH:mm";
    
    private static final SimpleDateFormat apiDateFormat = new SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault());
    private static final SimpleDateFormat apiDateTimeFormat = new SimpleDateFormat(DATETIME_FORMAT_API, Locale.getDefault());
    private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
    private static final SimpleDateFormat displayTimeFormat = new SimpleDateFormat(TIME_FORMAT_DISPLAY, Locale.getDefault());
    private static final SimpleDateFormat displayDateTimeFormat = new SimpleDateFormat(DATETIME_FORMAT_DISPLAY, Locale.getDefault());
    
    static {
        apiDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static String formatDateForApi(Date date) {
        return apiDateFormat.format(date);
    }
    
    public static String formatDateTimeForApi(Date date) {
        return apiDateTimeFormat.format(date);
    }
    
    public static String formatDateForDisplay(Date date) {
        return displayDateFormat.format(date);
    }
    
    public static String formatTimeForDisplay(Date date) {
        return displayTimeFormat.format(date);
    }
    
    public static String formatDateTimeForDisplay(Date date) {
        return displayDateTimeFormat.format(date);
    }
    
    public static Date parseDateFromApi(String dateString) {
        try {
            return apiDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Date parseDateTimeFromApi(String dateTimeString) {
        try {
            return apiDateTimeFormat.parse(dateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Date parseTimeString(String timeString) {
        try {
            return displayTimeFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Date getCurrentDate() {
        return new Date();
    }
    
    public static Date getTomorrowDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }
    
    public static Date addHours(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }
    
    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }
    
    public static boolean isDateInPast(Date date) {
        return date.before(new Date());
    }
    
    public static boolean isDateToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        cal2.setTime(new Date());
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    public static long getHoursDifference(Date date1, Date date2) {
        long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
        return diffInMillies / (60 * 60 * 1000);
    }
    
    public static boolean canCancelBooking(Date startTime) {
        // Can cancel if booking is at least 12 hours in the future
        Date now = new Date();
        long hoursUntilStart = (startTime.getTime() - now.getTime()) / (60 * 60 * 1000);
        return hoursUntilStart >= 12;
    }
    
    // UTC+5:30 (Sri Lanka Time) formatting methods
    public static String formatDateTimeForLocalDisplay(Date date) {
        // Manual timezone conversion: Add 5.5 hours (5 hours 30 minutes) to UTC time
        long utcTime = date.getTime();
        long sriLankaTimeOffset = (5 * 60 * 60 * 1000) + (30 * 60 * 1000); // 5.5 hours in milliseconds
        long localTime = utcTime + sriLankaTimeOffset;
        Date localDate = new Date(localTime);
        
        SimpleDateFormat localFormat = new SimpleDateFormat(DATETIME_FORMAT_DISPLAY, Locale.getDefault());
        return localFormat.format(localDate) + " (UTC+5:30)";
    }
    
    public static String formatDateTimeForLocalDisplaySimple(Date date) {
        // Manual timezone conversion: Add 5.5 hours (5 hours 30 minutes) to UTC time
        long utcTime = date.getTime();
        long sriLankaTimeOffset = (5 * 60 * 60 * 1000) + (30 * 60 * 1000); // 5.5 hours in milliseconds
        long localTime = utcTime + sriLankaTimeOffset;
        Date localDate = new Date(localTime);
        
        SimpleDateFormat localFormat = new SimpleDateFormat(DATETIME_FORMAT_DISPLAY, Locale.getDefault());
        return localFormat.format(localDate);
    }
    
    public static String formatDateTimeForUTCDisplay(Date date) {
        SimpleDateFormat utcFormat = new SimpleDateFormat(DATETIME_FORMAT_DISPLAY, Locale.getDefault());
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcFormat.format(date) + " UTC";
    }
}