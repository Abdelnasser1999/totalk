package com.callberry.callingapp.util;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeUtil {
    public static long getTimestamp(){
        return new Date().getTime();
    }

    public static long getTimestamp(long increment){
        return new Date().getTime() + (increment);
    }

    public static String timeAgo(long timestamp){
        int seconds = getSeconds(timestamp);
        if (seconds == 0 )

            return "Now";

        if (seconds < 60 )
            return seconds + " seconds ago";

        int minutes = seconds / 60;
        if (minutes < 60)
            return minutes + " minutes ago";

        int hours = seconds / 3600;
        if (hours < 24 )
            return hours + " hours ago";

        if (hours > 24 && hours < 48)
            return "Yesterday";

        return getDate(timestamp);
    }

    public static String getDate(long timestamp) {
        // Timestamp: Sun May 26 05:19:10 GMT+05:00 2019
        Date date = new Date(timestamp);
        String strDate = String.valueOf(date);
        List<String> listDate = (Arrays.asList(strDate.split(" ")));
        String dateStr = listDate.get(0) + " "
                + listDate.get(1) + " "
                + listDate.get(2) + " "
                + listDate.get(5);
        return dateStr;
    }

    public static String convertIntoDate(long timestamp) {
        // Timestamp: Sun May 26 05:19:10 GMT+05:00 2019
        Date date = new Date(timestamp);
        return String.valueOf(date);
    }

    public static int getSeconds(long timestamp){
        Date from = new Date();
        Date to = new Date(timestamp);
        return (int) ((from.getTime() - to.getTime()) / 1000);
    }

    public static long getTimeDiff(long date1, long date2){
        if (date1 > date2)
            return (int) ((new Date(date1).getTime() - new Date(date2).getTime()));
        else
            return (int) ((new Date(date2).getTime() - new Date(date1).getTime()));
    }

    public static int intoSeconds(long timestamp){
        return (int) new Date(timestamp).getTime() / 1000 % 60;
    }

    public static int intoMinutes(long timestamp){
        return (int) new Date(timestamp).getTime() / (60 * 1000) % 60;
    }

    public static int intoHours(long timestamp){
        return (int) new Date(timestamp).getTime() / (60 * 60 * 1000) % 24;
    }

    public static int intoDays(long timestamp){
        return (int) new Date(timestamp).getTime() / (24 * 60 * 60 * 1000);
    }

    public static String dateOnly(Long timestamp) {
        Date date = new Date(timestamp);
        String strDate = String.valueOf(date);
        List<String> listDate = (Arrays.asList(strDate.split(" ")));
        return listDate.get(2) + " " + listDate.get(1);
    }

    public static String timeOnly(long timestamp) {
        Date date = new Date(timestamp);
        String strDate = String.valueOf(date);
        List<String> listDate = (Arrays.asList(strDate.split(" ")));
        return convert(listDate.get(3));
    }

    public static String time(long timestamp) {
        Date date = new Date(timestamp);
        String strDate = String.valueOf(date);
        List<String> listDate = (Arrays.asList(strDate.split(" ")));
        return listDate.get(3);
    }

    private static String convert(String time){
        String convertedTime = null;
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            final Date dateObj = sdf.parse(time);
            convertedTime = new SimpleDateFormat("hh:mm a").format(dateObj);
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        return convertedTime;
    }

    public static String getCurrentDate(){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(getTimestamp());
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }
}
