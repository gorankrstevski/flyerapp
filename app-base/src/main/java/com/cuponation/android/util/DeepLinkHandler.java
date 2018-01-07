package com.cuponation.android.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.remote.VoucherService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.CategoryVouchersActivity;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.activity.VoucherDetailsActivity;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by goran on 11/9/16.
 */

public class DeepLinkHandler {

    public static final String RETAILER_DL_IDENTIFIER = "://retailer";
    public static final String VOUCHER_DL_IDENTIFIER = "://retailer/voucher";
    public static final String CATEGORY_DL_IDENTIFIER = "://category";
    public static final String NOTIFICATIONS_DL_IDENTIFIER = "://notifications";
    public static final String SETTINGS_DL_IDENTIFIER = "://settings";

    public enum DeepLinkType {
        Category, Retailer, Http, Notifications, Settings, Voucher
    }

    public static boolean handelDeepLink(final Activity activity, String data,
                                         final Bundle intentExtras,
                                         final Notification.NotificationType notificationType,
                                         final long notificationId) {

        DeepLinkHandler.DeepLinkType deepLinkType = DeepLinkHandler.getDeepLingType(data);

        if (deepLinkType != null) {
            switch (deepLinkType) {
                case Category:
                    String categoryId = DeepLinkHandler.getCategoryId(data);
                    if (!TextUtils.isEmpty(categoryId)) {
                        Intent activityIntent = new Intent(activity.getApplicationContext(), CategoryVouchersActivity.class);
                        activityIntent.putExtra(Constants.EXTRAS_CATEGORY_ID, categoryId);
                        if (intentExtras != null) {
                            activityIntent.putExtras(intentExtras);
                        }
                        activity.startActivity(activityIntent);
                        trackOpenApp(GATracker.getOpenType(notificationType), null, null);
                        return true;
                    }
                    break;
                case Retailer:
                    String retailerId = DeepLinkHandler.getRetailerId(data);
                    if (!TextUtils.isEmpty(retailerId)) {
                        Intent activityIntent = new Intent(activity.getApplicationContext(), RetailerVouchersActivity.class);
                        activityIntent.putExtra(Constants.EXTRAS_RETAILER_ID, retailerId);
                        if (intentExtras != null) {
                            activityIntent.putExtras(intentExtras);
                        }
                        activity.startActivity(activityIntent);
                        trackOpenApp(GATracker.getOpenType(notificationType), null, null);
                        return true;
                    }
                    break;
                case Voucher:
                    final String retailerIdd = DeepLinkHandler.getRetailerId(data);
                    final String voucherId = DeepLinkHandler.getVoucherId(data);

                    Retailer retailer = RetailerService.getInstance().getRetailerById(retailerIdd);
                    if(retailer!=null) {
                        trackOpenApp(GATracker.getOpenType(notificationType), retailer.getName(), voucherId);
                    }

                    VoucherService.getInstance().getVoucherFull(voucherId)
                            .compose(((RxAppCompatActivity) activity).<VoucherFull>bindToLifecycle())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<VoucherFull>() {
                                @Override
                                public void accept(VoucherFull voucher) throws Exception {
                                    if (voucher != VoucherService.NOT_FOUND) {
                                        Intent intent = new Intent(CouponingApplication.getContext(), VoucherDetailsActivity.class);
                                        intent.putExtra(Constants.EXTRAS_NOTIFICATION_ID, notificationId);
                                        intent.putExtra(Constants.EXTRAS_VOUCHER, voucher);
                                        intent.putExtra(Constants.EXTRAS_SCREEN_NAME, "deeplink");
                                        activity.startActivity(intent);

                                    } else {
                                        Intent activityIntent = new Intent(activity.getApplicationContext(), RetailerVouchersActivity.class);
                                        activityIntent.putExtra(Constants.EXTRAS_RETAILER_ID, retailerIdd);
                                        if (intentExtras != null) {
                                            activityIntent.putExtras(intentExtras);
                                        }
                                        activity.startActivity(activityIntent);
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    throwable.printStackTrace();
                                    Toast.makeText(activity, R.string.cn_app_check_your_connection, Toast.LENGTH_SHORT).show();
                                }
                            });

                    break;
                case Http:
                    trackOpenApp(GATracker.getOpenType(notificationType), null, null);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(data));
                    activity.startActivity(i);

                    return true;
                case Notifications:
                case Settings:
                    return false;
            }
        }
        return false;
    }

    private static void trackOpenApp(String type, String retailerName, String voucherId){
        if(type!=null) {
            CouponingApplication.getGATracker().trackOpenApp(type, retailerName, voucherId);
        }
    }

    public static DeepLinkType getDeepLingType(String data) {

        if (data != null) {
            if (data.contains(VOUCHER_DL_IDENTIFIER)) {
                return DeepLinkType.Voucher;
            } else if (data.contains(RETAILER_DL_IDENTIFIER)) {
                return DeepLinkType.Retailer;
            } else if (data.contains(CATEGORY_DL_IDENTIFIER)) {
                return DeepLinkType.Category;
            } else if (data.startsWith("http://") || data.startsWith("https://")) {
                return DeepLinkType.Http;
            } else if (data.contains(NOTIFICATIONS_DL_IDENTIFIER)) {
                return DeepLinkType.Notifications;
            } else if (data.contains(SETTINGS_DL_IDENTIFIER)) {
                return DeepLinkType.Settings;
            }
        }

        return null;
    }

    // handle deeplinks like "cuponation://category/5"
    public static String getCategoryId(String data) {
        return data.substring(data.lastIndexOf("/") + 1);
    }

    // handle deeplinks like "cuponation://retailer/501"
    public static String getRetailerId(String data) {
        return data.substring(data.lastIndexOf("/") + 1);
    }

    // handle deeplinks like "cuponation://retailer/voucher/12212/501"
    public static String getVoucherId(String data) {
        String[] pathSegments = data.split("/");
        return pathSegments[pathSegments.length - 2];
    }
}
