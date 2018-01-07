package com.cuponation.android.util;

import android.content.Context;

import com.cuponation.android.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Goran Krstevski (goran.krstevski@cuponation.com) on 28.12.15.
 */
public class TimeUtil {

    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getListExpireTime(String endDate, Context context) {
        long diff = getDiff(endDate);
        if (diff < DAY_MILLIS) {
            return context.getString(R.string.cn_app_expires_tomorrow);
        } else if (diff < 2 * DAY_MILLIS) {
            return context.getString(R.string.cn_app_expires_today);
        } else if (diff < 3 * DAY_MILLIS && diff > 2 * DAY_MILLIS) {
            int daysLeft = (int) (diff / DAY_MILLIS);
            return String.format(context.getString(R.string.cn_app_expire_in_days), "" + daysLeft);
        } else {
            return "";
        }
    }

    public static String getExpireTime(String endDate, Context context) {

        long diff = getDiff(endDate);

        if (diff < DAY_MILLIS) {
            return context.getString(R.string.cn_app_expires_today);
        } else if (diff < 2 * DAY_MILLIS) {
            return context.getString(R.string.cn_app_expires_tomorrow);
        } else if (diff < 25 * DAY_MILLIS && diff > 2 * DAY_MILLIS) {
            int daysLeft = (int) (diff / DAY_MILLIS);
            return String.format(context.getString(R.string.cn_app_expire_in_days), "" + daysLeft);
        } else if (diff > 25 * DAY_MILLIS && diff < 2 * 45 * DAY_MILLIS) {
            return context.getString(R.string.cn_app_expire_in_month);
        } else if (diff > 2 * 30 * DAY_MILLIS && diff < 3 * 30 * DAY_MILLIS) {
            return String.format(context.getString(R.string.cn_app_expire_in_months), "" + 2);
        } else {
            return String.format(context.getString(R.string.cn_app_expire_in_months), "" + 3);
        }
    }

    public static String getExpireTimeInHours(String endDate, Context context){
        long diff = getDiff(endDate);

        long hours = diff / HOUR_MILLIS;
        return String.format(context.getString(R.string.cn_app_expire_hours), ""+hours);
    }

    public static long parseTime(String endDate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.parse(endDate).getTime();
    }

    public static long getDiff(String endDate) {
        long time;
        try {
            time = parseTime(endDate);
        } catch (ParseException e) {
            return -1;
        }

        long now = System.currentTimeMillis();
        if (time < now || time <= 0) {
            return -1;
        }

        return time - now;
    }
}
