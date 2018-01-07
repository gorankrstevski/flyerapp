package com.cuponation.android.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.tracking.GATracker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by goran on 2/2/17.
 */

public class ShareUtility {

    public static final String MAIL_CLIENT_TYPE = "message/rfc822";
    public static final String WHATS_APP_PACKAGE = "com.whatsapp";
    public static final String MESSENGER_APP_PACKAGE = "com.facebook.orca";


    public static String prepareShareContent(Context context, Voucher voucher, String appName) {

        String refererPart = "utm_source=share&utm_medium=";
        try {
            refererPart = refererPart + appName.replace(" ", "");
            refererPart = URLEncoder.encode(refererPart, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return context.getString(R.string.cn_app_share_text) + voucher.getRetailerName()
                + ": " + voucher.getTitle()
                + context.getString(R.string.share_link_to_play_store)
                ;
    }

    public static Intent getShareIntent(Intent intent, String text) {
        if (intent == null) {
            intent = new Intent(Intent.ACTION_SEND);
        } else {
            intent.setAction(Intent.ACTION_SEND);
        }
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return intent;
    }

    public static Intent getShareIntent(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        return getShareIntent(intent, text);
    }

    public static boolean isPackageInstalled(String packagename) {

        try {
            PackageManager pm = CouponingApplication.getContext().getPackageManager();

            pm.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getApplicationName(String packageName) {

        try {
            PackageManager pm = CouponingApplication.getContext().getPackageManager();
            return pm.getApplicationLabel(pm.getPackageInfo(packageName, 0).applicationInfo).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    public static List<ResolveInfo> getShareOptions(Intent intent) {
        PackageManager pm = CouponingApplication.getContext().getPackageManager();
        return pm.queryIntentActivities(intent, 0);
    }

    public static String getShareMethod(String type, String appname) {
        if (MAIL_CLIENT_TYPE.equals(type)) {
            return GATracker.SHARE_METHOD_MAIL;
        } else if (MESSENGER_APP_PACKAGE.equals(type)) {
            return GATracker.SHARE_METHOD_FB;
        } else if (WHATS_APP_PACKAGE.equals(type)) {
            return GATracker.SHARE_METHOD_WHATSAPP;
        } else {
            return GATracker.SHARE_METHOD_OTHER + appname;
        }
    }

}
