package com.luozi.fireeyewatcher.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String timeNow() {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
