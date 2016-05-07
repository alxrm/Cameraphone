package com.rm.cameraphone.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by alex
 */
public class TimeUtils {

    public static boolean isMillis(long time) {
        return time > 1_000_000_000_000L;
    }

    public static long toSeconds(long time) {
        return isMillis(time) ? (time / 1000) : time;
    }

    public static long toMillis(long time) {
        return isMillis(time) ? time : (time * 1000);
    }

    public static long unixTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long unixTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static int getMinutes() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public static int getHours() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static String getDay(long unix) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM, EEEE", new Locale("ru", "RU"));
        Date d = new Date();
        String resDate;

        d.setTime(unix);
        dateFormat.applyPattern("d MMMM, EEEE");

        resDate = dateFormat.format(d);

        Log.d("TimeUtil", "getDay - resDate: "
                + resDate);

        return resDate;
    }

    public static String getTime(long unix) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm", new Locale("ru", "RU"));
        Date d = new Date();
        String resDate;

        d.setTime(unix);
        dateFormat.applyPattern("h:mm");

        resDate = dateFormat.format(d);

        Log.d("TimeUtil", "getTime - resDate: " + resDate);

        return resDate;
    }

    public static boolean compareMillis(long lMillis, long rMillis) {
        return (lMillis/1000) == (rMillis/1000);
    }

    public static String formatTime(int minutes, int seconds) {
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String formatTime(long millis) {
        int seconds = (int) (millis / 1000);

        return formatTime(seconds / 60, seconds % 60);
    }
}
