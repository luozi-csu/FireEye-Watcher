package com.luozi.fireeyewatcher.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String timeNow() {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static String convert(int timestamp) {
        long longTimestamp = (long)timestamp * 1000;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(longTimestamp);
        return formatter.format(date);
    }

    public static String convertToDateNormal(int timestamp) {
        long longTimeStamp = (long) timestamp * 1000;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(longTimeStamp);
        return formatter.format(date);
    }

    public static String convertToDate(int timestamp) {
        long longTimeStamp = (long) timestamp * 1000;
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd");
        Date date = new Date(longTimeStamp);
        return formatter.format(date);
    }

    public static String convertToDate(int year, int month, int day) throws ParseException {
        String dateStr = String.format("%d-%d-%d", year, month, day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(dateStr);
        return simpleDateFormat.format(date);
    }

    public static long convertToTimeStamp(String s) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = formatter.parse(s);
        return date.getTime();
    }
}
