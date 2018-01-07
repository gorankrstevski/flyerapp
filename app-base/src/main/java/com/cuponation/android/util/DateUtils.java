package com.cuponation.android.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public enum DateUtilFormat {
        DayMonthYear, DayMonthYearSlash, YearMonthDay, TimeFormat, DateTime
    }

    // default format
    private static String DAY_MONTH_YEAR_FORMAT_PATTERN = "dd-MM-yyyy";
    private static String DAY_MONTH_YEAR_SLASH_FORMAT_PATTERN = "dd/MM/yyyy";
    private static String YEAR_MONTH_DAY_FORMAT_PATTERN = "yyyy-MM-dd";
    private static String TIME_FORMAT = "hh:mm:ss";
    private static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";
    private static DateUtils instance = null;
    private SimpleDateFormat mSimpleDateFormat;

    private DateUtils() {
        mSimpleDateFormat = new SimpleDateFormat(DAY_MONTH_YEAR_FORMAT_PATTERN, Locale.getDefault());
    }

    @NonNull
    public static DateUtils getInstance() {
        if (instance == null) {
            instance = new DateUtils();
        }
        return instance;
    }

    @Nullable
    public Date parseDate(@NonNull String dateString, @NonNull DateUtilFormat format) {
        setDateFormat(format);
        try {
            if (TextUtils.isEmpty(dateString)) {
                return null;
            }
            return mSimpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public String dateString(@NonNull Date date, @NonNull DateUtilFormat format) {
        setDateFormat(format);
        return mSimpleDateFormat.format(date);
    }

    public int currentYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.YEAR);
    }

    private void setDateFormat(DateUtilFormat format) {
        switch (format) {
            case DayMonthYear:
                mSimpleDateFormat = new SimpleDateFormat(DAY_MONTH_YEAR_FORMAT_PATTERN, Locale.getDefault());
                break;
            case YearMonthDay:
                mSimpleDateFormat = new SimpleDateFormat(YEAR_MONTH_DAY_FORMAT_PATTERN, Locale.getDefault());
                break;
            case DayMonthYearSlash:
                mSimpleDateFormat = new SimpleDateFormat(DAY_MONTH_YEAR_SLASH_FORMAT_PATTERN, Locale.getDefault());
                break;
            case TimeFormat:
                mSimpleDateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
                break;
            case DateTime:
                mSimpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
                break;
        }
    }
}
