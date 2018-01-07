package com.cuponation.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.cuponation.android.app.CouponingApplication;

/**
 * Created by Goran Krstevski (goran.krstevski@cuponation.com) on 5.11.15.
 */
public class SharedPreferencesUtil {

    private static final String USER_PREFERENCES = "USER_PREF";
    private static final String MY_RETAILERS_NOTIFICATION_KEY = "MY_RETAILER_KEY";
    private static final String PROMO_NOTIFICATION_KEY = "PROMO_NOTIFICATION_KEY";
    private static final String BOOKMARK_NOTIFICATION_KEY = "BOOKMARK_NOTIF_KEY";
    private static final String NEW_NOTIFICATION_KEY = "NEW_NOTIFICATION_KEY";
    private static final String NEW_BOOKMARK_NOTIFICATION_KEY = "NEW_BM_NOTIF_KEY";
    private static final String RETAILERS_DATA_TIMESTAMP_KEY = "RETAILERS_TIMESTAMP";
    private static final String COPY_CODE_FIRST_SHOW = "COPY_CODE_FIRST_SHOW";
    private static final String UPDATE_NEEDED_KEY = "UPDATE_NEEDED_KEY";
    private static final String APP_RATED_KEY = "APP_RATED_KEY";
    private static final String APP_RATED_TIMESTAMP_KEY = "APP_RATED_TIMESTAMP_KEY";
    private static final String IS_BOOKMARK_TOOLTIP_SHOWN = "IS_BOOKMARK_TOOLTIP_SHOWN";
    private static final String IS_OPEN_VOUCHER_TOOLTIP_SHOWN = "IS_OPEN_VOUCHER_TOOLTIP_SHOWN";
    private static final String AT_LEAST_ONE_VOUCHER_OPENED = "AT_LEAST_ONE_VOUCHER_OPENED";
    private static final String COUNTRY_SELECTION_KEY = "COUNTRY_SELECTION_KEY";
    private static final String LEXPRESS_SWITCH_BATCH_ACCOUNT = "LEXPRESS_SWITCH_BATCH_ACCOUNT";

    private static SharedPreferencesUtil me;

    private SharedPreferencesUtil(){
        super();
    }

    public static SharedPreferencesUtil getInstance(){
        if(me == null){
            me = new SharedPreferencesUtil();
        }
        return me;
    }

    public boolean getMyRetailersNotification() {
        return getValue(MY_RETAILERS_NOTIFICATION_KEY, true);
    }

    public void setMyRetailersNotification(boolean value) {
        setValue(MY_RETAILERS_NOTIFICATION_KEY, value);
    }

    public boolean getPromoNotification() {
        return getValue(PROMO_NOTIFICATION_KEY, true);
    }

    public void setPromoNotification(boolean value) {
        setValue(PROMO_NOTIFICATION_KEY, value);
    }

    public boolean isBokmarkNotificationEnabled() {
        return getValue(BOOKMARK_NOTIFICATION_KEY, true);
    }

    public void setBokmarkNotificationEnabled(boolean value) {
        setValue(BOOKMARK_NOTIFICATION_KEY, value);
    }

    public boolean isNewBookmarkNotificationAdded() {
        return getValue(NEW_BOOKMARK_NOTIFICATION_KEY, false);
    }

    public void setNewBookmarkNotificationAdded(boolean value) {
        setValue(NEW_BOOKMARK_NOTIFICATION_KEY, value);
    }

    public boolean isNewNotificationAdded() {
        return getValue(NEW_NOTIFICATION_KEY, false);
    }

    public void setNewNotificationAdded(boolean value) {
        setValue(NEW_NOTIFICATION_KEY, value);
    }

    public boolean isUpdateNeededOnHome() {
        return getValue(UPDATE_NEEDED_KEY, false);
    }

    public void setUpdateNeededOnHome(boolean value) {
        setValue(UPDATE_NEEDED_KEY, value);
    }

    public long getRetailersDataTimestamp() {
        return getValue(RETAILERS_DATA_TIMESTAMP_KEY, 0);
    }

    public void setRetailersDataTimestamp(long value) {
        setValue(RETAILERS_DATA_TIMESTAMP_KEY, value);
    }

    public void setCopyCodeFirstShow(boolean value){
        setValue(COPY_CODE_FIRST_SHOW, value);
    }

    public boolean getCopyCodeFirstShow(){
        return getValue(COPY_CODE_FIRST_SHOW, false);
    }

    public void setAppRate(boolean value){
        setValue(APP_RATED_KEY, value);
    }

    public boolean getAppRate(){
        return getValue(APP_RATED_KEY, false);
    }

    public void setAppRateRemindTimestamp(long timestamp){
        setValue(APP_RATED_TIMESTAMP_KEY, timestamp);
    }

    public long getAppRateRemindTimestamp(){
        return getValue(APP_RATED_TIMESTAMP_KEY, -1);
    }

    public void setBookmarkTooltipShown(boolean value){
        setValue(IS_BOOKMARK_TOOLTIP_SHOWN, value);
    }

    public boolean isBookmarkTooltipShown(){
        return getValue(IS_BOOKMARK_TOOLTIP_SHOWN, false);
    }

    public void setOpenVoucherTooltipShown(boolean value){
        setValue(IS_OPEN_VOUCHER_TOOLTIP_SHOWN, value);
    }

    public boolean isOpenVoucherTooltipShown(){
        return getValue(IS_OPEN_VOUCHER_TOOLTIP_SHOWN, false);
    }
    public void setFirstVoucherOpened(boolean value){
        setValue(AT_LEAST_ONE_VOUCHER_OPENED, value);
    }

    public boolean isFirstVoucherOpened(){
        return getValue(AT_LEAST_ONE_VOUCHER_OPENED, false);
    }

    public void setCountrySelection(String value){
        setValue(COUNTRY_SELECTION_KEY, value);
    }

    public String getCountrySelection(){
        return getValue(COUNTRY_SELECTION_KEY, null);
    }

    public void setLexressBatchAccountTransferDone(boolean value){
        setValue(LEXPRESS_SWITCH_BATCH_ACCOUNT, value);
    }

    public boolean isLexressBatchAccountTransferDone(){
        return getValue(LEXPRESS_SWITCH_BATCH_ACCOUNT, false);
    }
    // ============== Private methods ======================

    private static void clearSharedPreferences(Context context){
        getUserSharedPref(context).edit().clear().apply();
    }

    private static SharedPreferences getUserSharedPref(Context context) {
        return context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void setValue(String key, String value) {
        SharedPreferences userPref = SharedPreferencesUtil.getUserSharedPref(CouponingApplication.getContext());
        userPref.edit().putString(key, value).apply();
    }

    private String getValue(String key, String defaultValue) {
        SharedPreferences userPref = SharedPreferencesUtil.getUserSharedPref(CouponingApplication.getContext());
        return userPref.getString(key, defaultValue);
    }

    private void setValue(String key, long value) {
        SharedPreferences userPref = SharedPreferencesUtil.getUserSharedPref(CouponingApplication.getContext());
        userPref.edit().putLong(key, value).apply();
    }

    private long getValue(String key, long defaultValue) {
        SharedPreferences userPref = SharedPreferencesUtil.getUserSharedPref(CouponingApplication.getContext());
        return userPref.getLong(key, defaultValue);
    }

    private void setValue(String key, boolean value) {
        SharedPreferences userPref = SharedPreferencesUtil.getUserSharedPref(CouponingApplication.getContext());
        userPref.edit().putBoolean(key, value).apply();
    }

    private boolean getValue(String key, boolean defaultValue) {
        SharedPreferences userPref = SharedPreferencesUtil.getUserSharedPref(CouponingApplication.getContext());
        return userPref.getBoolean(key, defaultValue);
    }



}
