package com.cuponation.android.service.notif;

import android.app.IntentService;
import android.content.Intent;

import com.batch.android.Batch;
import com.batch.android.BatchMessage;
import com.batch.android.BatchPushPayload;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.receiver.BatchPushReceiver;
import com.cuponation.android.service.local.NotificationPreferencesService;
import com.cuponation.android.service.local.NotificationService;
import com.cuponation.android.util.CountryUtil;
import com.cuponation.android.util.DeepLinkHandler;
import com.cuponation.android.util.NotificationsUtil;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.cuponation.android.util.ShortcutBadgerUtil;

/**
 * Created by goran on 9/21/16.
 */

public class BatchPushService extends IntentService {

    public BatchPushService() {
        super("MyBatchPushService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Code to display notification will go there
            try {
                String title = intent.getStringExtra(Batch.Push.TITLE_KEY);
                String message = intent.getStringExtra(Batch.Push.ALERT_KEY);
                long date = System.currentTimeMillis();
                BatchPushPayload pushData = BatchPushPayload.payloadFromReceiverIntent(intent);
                String iconURL = pushData.getCustomLargeIconURL(getApplicationContext());
                String deeplink = pushData.getDeeplink();
                String bigPictureURL = pushData.getBigPictureURL(getApplicationContext());
                BatchMessage batchMessage = pushData.getLandingMessage();
                boolean isAutoCampaign = Boolean.parseBoolean(pushData.getPushBundle().getString("isAutoCampaign"));
                String countryCode = pushData.getPushBundle().getString("countryCode");
                if(countryCode!=null && !CountryUtil.getCountrySelection().getCountryCode().equals(countryCode)){
                    return;
                }

                if(isAutoCampaign) {
                    DeepLinkHandler.DeepLinkType deepLinkType = DeepLinkHandler.getDeepLingType(deeplink);
                    if (deepLinkType == DeepLinkHandler.DeepLinkType.Voucher || deepLinkType == DeepLinkHandler.DeepLinkType.Retailer) {
                        String retailerId = DeepLinkHandler.getRetailerId(deeplink);
                        if (NotificationPreferencesService.getInstance().getLastRetailerNotification(retailerId)
                                + android.text.format.DateUtils.DAY_IN_MILLIS > System.currentTimeMillis()) {
                            return;
                        } else {
                            NotificationPreferencesService.getInstance().updateRetailerNotification(retailerId);
                        }
                    }
                }

                if(batchMessage == null) {
                    Notification notification = new Notification(title, message, deeplink, date, iconURL, isAutoCampaign);
                    NotificationService notificationService = NotificationService.getInstance();
                    notificationService.addNewNotification(notification);
                    SharedPreferencesUtil.getInstance().setNewNotificationAdded(true);
                    ShortcutBadgerUtil.updateAppBadge(notificationService);
                }

                NotificationsUtil.publishBatchNotification(
                        getApplicationContext(), deeplink, title, message, iconURL, bigPictureURL, date,
                        batchMessage, isAutoCampaign);

            } catch (BatchPushPayload.ParsingException a) {
                a.printStackTrace();
            }

        } finally {
            BatchPushReceiver.completeWakefulIntent(intent);
        }
    }
}