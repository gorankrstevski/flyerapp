package com.cuponation.android.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.service.local.NotificationService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.MainActivity;

import java.util.Collections;
import java.util.List;

/**
 * Created by goran on 7/7/16.
 */

public class NotificationsUtil {

    public static final String ACTION_BOOKMARK = "bookmark";

    public static final int DEFAULT_NOTIFICATION_ID = 001;
    private static final String NOTIFICATION_CHANNEL = "notiff_channel";

    public static void publishNotification(Context context, Retailer retailer, String url, String uniqueCode) {

        List<Voucher> codes = Utils.getCodeVouchers(retailer);

        if (codes.size() == 0) {
            return;
        }

        Voucher voucherForCopyCode;

        if (codes.size() == 1) {
            voucherForCopyCode = codes.get(0);
        } else {
            voucherForCopyCode = getAppExlusiveVoucher(codes);
        }

        if (voucherForCopyCode == null) {
            voucherForCopyCode = getVerifiedVoucher(codes);
        }

        Intent seeMoreIntent = new Intent(context, MainActivity.class);
        seeMoreIntent.putExtra(Constants.EXTRAS_RETAILER_ID, retailer.getId());
        seeMoreIntent.putExtra(Constants.EXTRAS_RETAILER_VOUCHERS, codes.size());
        seeMoreIntent.putExtra(Constants.EXTRAS_SOURCE_NOTIFICATION, com.cuponation.android.model.notifications.Notification.NotificationType.APP_NOTIF.ordinal());
        seeMoreIntent.putExtra(Constants.EXTRAS_URL_KEY, url);
        // Creates the PendingIntent
        PendingIntent seeMorePendingIntent =
                PendingIntent.getActivity(context, 0, seeMoreIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = getNotificationBuilder(context);

        if (voucherForCopyCode != null) {

            long notificationDate = System.currentTimeMillis();

            builder.addAction(R.drawable.ic_stat_bookmark, context.getString(R.string.cn_app_bookmark),
                    getBookmarkActionPendingIntent(context, retailer.getId(), voucherForCopyCode.getVoucherId(), notificationDate,
                            com.cuponation.android.model.notifications.Notification.NotificationType.APP_NOTIF));

            voucherForCopyCode.setRetailerId(retailer.getId());
            // Creates an Intent for the Activity
            Intent copyIntent = new Intent(context, MainActivity.class);
            // Sets the Activity to start in a new, empty task
            copyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            copyIntent.setAction(Constants.EXTRAS_COPY_ACTION);
            copyIntent.putExtra(Constants.EXTRAS_URL_KEY, url);
            copyIntent.putExtra(Constants.EXTRAS_CODE_KEY, uniqueCode != null ? uniqueCode : voucherForCopyCode.getCode());
            copyIntent.putExtra(Constants.EXTRAS_VOUCHER, voucherForCopyCode);
            // Creates the PendingIntent
            PendingIntent copyPendingIntent =
                    PendingIntent.getActivity(context, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addAction(R.drawable.ic_stat_copy, context.getString(R.string.cn_app_copy_code), copyPendingIntent); // #0
            builder.setContentTitle(voucherForCopyCode.getRetailerName());
            builder.setContentText(voucherForCopyCode.getShortTitle());
            builder.setContentIntent(copyPendingIntent);

            // add Notification in storage
            NotificationService.getInstance().addNewNotification(new com.cuponation.android.model.notifications.Notification(voucherForCopyCode, notificationDate));

        } else {
            builder.setContentTitle(retailer.getName());
            String label = String.format(context.getString(R.string.cn_app_notification_codes), "" + codes.size(), retailer.getName());
            builder.setContentText(label);
            builder.setContentIntent(seeMorePendingIntent);

            Retailer retailerForNotification = new Retailer(retailer);
            retailer.setVouchers(null);
            NotificationService.getInstance().addNewNotification(new com.cuponation.android.model.notifications.Notification(retailerForNotification, label, System.currentTimeMillis()));

            builder.addAction(R.drawable.ic_stat_see_all, context.getString(R.string.cn_app_seemore), seeMorePendingIntent);  // #1
        }

        ShortcutBadgerUtil.updateAppBadge(NotificationService.getInstance());
        // set flag to show that new notification was added
        SharedPreferencesUtil.getInstance().setNewNotificationAdded(true);
        publishNotification(context, builder, DEFAULT_NOTIFICATION_ID);

    }

    public static void publishBatchNotification(Context context, String deeplink, String title, String message, String largeIcon, String bigPictureUrl, long localNotificationId,
                                                Object batchMessage, boolean isAutoCampaign) {

        NotificationCompat.Builder builder = getNotificationBuilder(context);

        builder.setContentTitle(title);
        builder.setContentText(message);
        Bitmap largeIconBitmap;
        if (largeIcon != null) {
            largeIconBitmap = BitmapUtil.getBitmap(largeIcon);
        } else {
            largeIconBitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).getBitmap();
        }
        builder.setLargeIcon(largeIconBitmap);

        List<com.cuponation.android.model.notifications.Notification> unreadNotifications
                = NotificationService.getInstance().getUnreadNotifications();
        // if more then 1 unread notification, use InboxStyle
        if (unreadNotifications.size() > 1) {
            Collections.reverse(unreadNotifications);
            NotificationCompat.InboxStyle notificationCompat = new NotificationCompat.InboxStyle();
            int i = 0;
            for (com.cuponation.android.model.notifications.Notification notification : unreadNotifications) {
                notificationCompat.addLine(notification.getTitle() + " : " + notification.getMessage());
                i++;
                if (i == 5) {
                    break;
                }
            }
            notificationCompat.setSummaryText("+ " + unreadNotifications.size() + context.getString(R.string.cn_app_notification_new_coupons));
            notificationCompat.setBigContentTitle(unreadNotifications.size() + context.getString(R.string.cn_app_notification_new_coupons));
            builder.setStyle(notificationCompat);

            // Creates the PendingIntent for Action when click normal-view(not expanded) of notification
            Intent seeMoreIntent = new Intent(context, MainActivity.class);
            Uri dataSeeMore = Uri.parse(context.getString(R.string.app_scheme) + DeepLinkHandler.NOTIFICATIONS_DL_IDENTIFIER);
            seeMoreIntent.setData(dataSeeMore);
            seeMoreIntent.putExtra(Constants.EXTRAS_LOCAL_NOTIFICATION_ID, localNotificationId);
            seeMoreIntent.setAction(Intent.ACTION_VIEW);
            setBatchNotificationType(seeMoreIntent, isAutoCampaign);

            PendingIntent seeMorePendingIntent =
                    PendingIntent.getActivity(context, 0, seeMoreIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(seeMorePendingIntent);

        } else {
            // If big picture present show Custom view
            if (bigPictureUrl != null) {

                RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_big_image);
                contentView.setImageViewBitmap(R.id.notif_icon, largeIconBitmap);
                contentView.setTextViewText(R.id.notif_title, title);
                contentView.setTextViewText(R.id.notif_text, message);
                contentView.setImageViewBitmap(R.id.notif_image, BitmapUtil.getBitmap(bigPictureUrl));
                contentView.setTextColor(R.id.notif_title, Color.BLACK);
                contentView.setTextColor(R.id.notif_text, Color.BLACK);

                builder.setCustomBigContentView(contentView);
            }
            // build notification
            Intent seeMoreIntent = new Intent(context, MainActivity.class);
            if (deeplink != null) {
                seeMoreIntent.setData(Uri.parse(deeplink));
            }
            seeMoreIntent.setAction(Intent.ACTION_VIEW);
            seeMoreIntent.putExtra(Constants.EXTRAS_LOCAL_NOTIFICATION_ID, localNotificationId);
            setBatchNotificationType(seeMoreIntent, isAutoCampaign);

            if(batchMessage!=null) {
                CouponingApplication.getBatchService().writeToIntent(batchMessage, seeMoreIntent);
            }

            // Creates the PendingIntent
            PendingIntent seeMorePendingIntent =
                    PendingIntent.getActivity(context, 0, seeMoreIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(seeMorePendingIntent);

            if(DeepLinkHandler.DeepLinkType.Voucher == DeepLinkHandler.getDeepLingType(deeplink)){
                String retailerId = DeepLinkHandler.getRetailerId(deeplink);
                final String voucherId = DeepLinkHandler.getVoucherId(deeplink);
                builder.addAction(R.drawable.ic_stat_bookmark,
                        context.getString(R.string.cn_app_bookmark),
                        getBookmarkActionPendingIntent(context, retailerId, voucherId, localNotificationId,
                                isAutoCampaign ?
                com.cuponation.android.model.notifications.Notification.NotificationType.BATCH_AUTO_NOTIF :
                com.cuponation.android.model.notifications.Notification.NotificationType.BATCH_MANUAL_NOTIF
                ));
            }
        }

        // Options Action
        Intent optionIntent = new Intent(context, MainActivity.class);
        Uri data = Uri.parse(context.getString(R.string.app_scheme) + DeepLinkHandler.SETTINGS_DL_IDENTIFIER);
        optionIntent.setData(data);
        setBatchNotificationType(optionIntent, isAutoCampaign);
        optionIntent.setAction(Intent.ACTION_VIEW);

        PendingIntent optionsPendingIntent =
                PendingIntent.getActivity(context, 0, optionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         builder.addAction(R.drawable.ic_stat_settings, context.getString(R.string.cn_app_options_btn), optionsPendingIntent);

        // Publish notification
        publishNotification(context, builder, DEFAULT_NOTIFICATION_ID);
    }

    public static void publishExpireVoucherNotification(Context context, Voucher voucher) {
        GATracker.getInstance(context).trackExpireVoucherNotification(voucher);
        NotificationCompat.Builder builder = getNotificationBuilder(context);

        String message = context.getString(voucher.isCode() ? R.string.cn_app_expire_code_notiffication : R.string.cn_app_expire_deal_notiffication);
        message = String.format(message, voucher.getRetailerName());
        message = message + TimeUtil.getExpireTimeInHours(voucher.getEndDate(), context);
        builder.setContentText(message);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message));

        String title = voucher.getRetailerName();
        builder.setContentTitle(title);


        Bitmap largeIconBitmap = BitmapUtil.getBitmap(voucher.getRetailerLogoUrl());
        if (largeIconBitmap != null) {
            builder.setLargeIcon(BitmapUtil.overlay(largeIconBitmap, (int) context.getResources().getDimension(R.dimen.notificaiton_icon_size)));
        }

        Intent seeMoreIntent = new Intent(context, MainActivity.class);
        seeMoreIntent.setAction(Intent.ACTION_VIEW);
        String logoUrl = voucher.getRetailerLogoUrl();
        if(logoUrl!=null) {
            seeMoreIntent.setData(Uri.parse(logoUrl));
        }
        seeMoreIntent.putExtra(Constants.EXTRAS_SOURCE_NOTIFICATION, com.cuponation.android.model.notifications.Notification.NotificationType.EXPIRED_NOTIF.ordinal());
        seeMoreIntent.putExtra(Constants.EXTRAS_VOUCHER, voucher);

        // Creates the PendingIntent
        PendingIntent seeMorePendingIntent =
                PendingIntent.getActivity(context, 0, seeMoreIntent, 0);
        builder.setContentIntent(seeMorePendingIntent);

        publishNotification(context, builder, (int) (System.currentTimeMillis() % 1000));

        com.cuponation.android.model.notifications.Notification notification =
                new com.cuponation.android.model.notifications.Notification(title, message, System.currentTimeMillis(), voucher);

        // set flag to show that new notification was added
        SharedPreferencesUtil.getInstance().setNewBookmarkNotificationAdded(true);
        NotificationService.getInstance().addNewNotification(notification);
    }

    private static NotificationCompat.Builder getNotificationBuilder(Context context) {

        BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notif_cn)
                        .setLargeIcon(bitmapDrawable.getBitmap())
                        .setPriority(Notification.PRIORITY_MAX)
                        .setVibrate(new long[]{0});

        builder.setChannelId(NOTIFICATION_CHANNEL);
        return builder;
    }

    public static com.cuponation.android.model.notifications.Notification.NotificationType getNotificationType(Bundle bundle) {
        if (bundle != null) {
            int notificationTypeOrdinal = bundle.getInt(Constants.EXTRAS_SOURCE_NOTIFICATION, -1);
            if (notificationTypeOrdinal > -1) {
                return com.cuponation.android.model.notifications.Notification.NotificationType.values()[notificationTypeOrdinal];
            }
        }

        return null;
    }

    private static PendingIntent getBookmarkActionPendingIntent(
            Context context, String retailerId, String voucherId, long localNotificationId,
            com.cuponation.android.model.notifications.Notification.NotificationType notificationType){
        // Bookmark Action
        Intent bookmarkIntent = new Intent(context, MainActivity.class);
        bookmarkIntent.putExtra(Constants.EXTRAS_LOCAL_NOTIFICATION_ID, localNotificationId);
        bookmarkIntent.putExtra(Constants.EXTRAS_RETAILER_ID, retailerId);
        bookmarkIntent.putExtra(Constants.EXTRAS_VOUCHER_ID, voucherId);
        bookmarkIntent.setAction(ACTION_BOOKMARK);
        bookmarkIntent.putExtra(Constants.EXTRAS_SOURCE_NOTIFICATION, notificationType.ordinal());

        return PendingIntent.getActivity(context, 0, bookmarkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancelNotification(Context context, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    private static void publishNotification(Context context, NotificationCompat.Builder builder, int notificationId) {
        // Sets an ID for the notification
        if (notificationId == -1) {
            notificationId = DEFAULT_NOTIFICATION_ID;
        }

        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        builder.setAutoCancel(true);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notifyManager.notify(notificationId, notification);
    }

    private static Voucher getAppExlusiveVoucher(List<Voucher> vouchers) {
        Voucher result = null;
        int count = 0;
        for (Voucher voucher : vouchers) {
            if (voucher.getAppExclusive() == 1) {
                result = voucher;
                count++;
            }
        }

        if (count == 1) {
            return result;
        } else {
            return null;
        }
    }

    private static Voucher getVerifiedVoucher(List<Voucher> vouchers) {
        Voucher result = null;
        int count = 0;
        for (Voucher voucher : vouchers) {
            if (voucher.getVerified() != null) {
                result = voucher;
                count++;
            }
        }

        if (count == 1) {
            return result;
        } else {
            return null;
        }
    }

    private static void setBatchNotificationType(Intent intent, boolean isAutoCampaign){
        intent.putExtra(Constants.EXTRAS_SOURCE_NOTIFICATION,
                isAutoCampaign ?
                        com.cuponation.android.model.notifications.Notification.NotificationType.BATCH_AUTO_NOTIF.ordinal() :
                        com.cuponation.android.model.notifications.Notification.NotificationType.BATCH_MANUAL_NOTIF.ordinal());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = NOTIFICATION_CHANNEL;
        // The user-visible name of the channel.
        CharSequence name = context.getString(R.string.channel_name);
        // The user-visible description of the channel.
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mNotificationManager.createNotificationChannel(mChannel);
    }

}
