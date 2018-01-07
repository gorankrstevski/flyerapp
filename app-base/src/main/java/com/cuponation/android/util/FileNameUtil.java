package com.cuponation.android.util;

import android.content.Context;

import java.io.File;

/**
 * Created by goran on 7/15/16.
 */

public class FileNameUtil {

    private static final String SEARCH_HISTORY_PATH = "search_history.json";
    private static final String RETAILERS_PATH = "retailers.json";
    private static final String SAVED_VOUCHERS_PATH = "saved_vouchers.json";
    private static final String NOTIFICATIONS_PATH = "notifications.json";
    private static final String VISITED_RETAILERS_PATH = "visited_retaielrs.json";
    private static final String PUSH_ENABLED_RETAILERS_PATH = "push_enabled_retaielrs.json";


    public static String getLikedRetailersStorageFile(Context context){
        return getAbsoluteFileLocation(context, SEARCH_HISTORY_PATH);
    }

    public static String getVisitedRetailersStorageFile(Context context){
        return getAbsoluteFileLocation(context, VISITED_RETAILERS_PATH);
    }

    public static String getRetailersStorageFile(Context context){
        return getAbsoluteFileLocation(context, RETAILERS_PATH);
    }

    public static String getNotificationsStorageFile(Context context){
        return getAbsoluteFileLocation(context, NOTIFICATIONS_PATH);
    }

    public static String getSavedVouchersStorageFile(Context context){
        return getAbsoluteFileLocation(context, SAVED_VOUCHERS_PATH);
    }

    public static String getPushEnabledRetailersStorageFile(Context context){
        return getAbsoluteFileLocation(context, PUSH_ENABLED_RETAILERS_PATH);
    }

    private static String getAbsoluteFileLocation(Context context, String path){
        return new File(context.getFilesDir(), getFilePrefix() + path).getAbsolutePath();
    }

    private static String getFilePrefix(){
        return CountryUtil.getCountrySelection().getDbFilePrefix();
    }
}
