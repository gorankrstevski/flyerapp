package com.cuponation.android.util;

import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;

import com.cuponation.android.BuildConfig;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.network.URLBuilder;
import com.cuponation.android.tracking.BatchUtil;
import com.cuponation.android.ui.activity.BaseActivity;
import com.cuponation.android.util.customtabs.CustomTabActivityHelper;
import com.cuponation.android.util.customtabs.WebviewFallback;

/**
 * Created by goran on 7/21/16.
 */

public class VoucherClickoutUtil {

    public static void performClickout(final Voucher voucher, final BaseActivity activity, CustomTabsSession session){
        BatchUtil.trackUsedCodeEvent(voucher.getRetailerName(), voucher.getCategory());

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);

        CustomTabsIntent customTabsIntent = builder.build();
        //customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        String url = getClickoutUrl(voucher);

        CustomTabActivityHelper.openCustomTab(
                activity, customTabsIntent, Uri.parse(url), new WebviewFallback());

        CouponingApplication.isCustomTabOpened = true;
    }

    public static void openCustomTab(String url, final BaseActivity activity){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        //customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        CustomTabActivityHelper.openCustomTab(
                activity, customTabsIntent, Uri.parse(url), new WebviewFallback());
    }

    private static String getClickoutUrl(Voucher voucher) {
        String clickoutUrl = BuildConfig.CLICKOUT_URL + CountryUtil.getCountryDomain() + URLBuilder.URLPath.URL_PATH_CLICKOUT + voucher.getVoucherId();
        clickoutUrl = URLBuilder.appendParameters(clickoutUrl,  URLBuilder.HTTPConstants.KEY_CLIENT_ID, CountryUtil.getClientId());
        clickoutUrl = URLBuilder.appendParameters(clickoutUrl,  URLBuilder.HTTPConstants.KEY_DEVICE_SOURCE, CountryUtil.getClickoutDeviceValue());
        return clickoutUrl;
    }

}
