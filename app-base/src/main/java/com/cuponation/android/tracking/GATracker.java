package com.cuponation.android.tracking;

import android.content.Context;
import android.content.res.Configuration;

import com.cuponation.android.R;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.notifications.Notification;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by goran on 9/20/16.
 */

public class GATracker {

    private static Tracker tracker;
    private static GATracker me;

    private GATracker(Context context) {
        super();
        tracker = GoogleAnalytics.getInstance(context).newTracker(context.getString(R.string.ga_user_id));
        tracker.enableAdvertisingIdCollection(true);
    }

    public static GATracker getInstance(Context context) {
        if (me == null) {
            me = new GATracker(context);
        }

        return me;
    }


    public void trackClickout(Voucher voucher, String clickoutMethod, boolean isBookmarked) {

        String action = ACTION_CLICKOUT + clickoutMethod;
        String label = String.format(LABEL_CLICKOUT, voucher.getType(), voucher.getRetailerName(), voucher.getVoucherId()) + (isBookmarked ? "true" : "false");
        trackEvent(action, CATEGORY_CLICKOUT, label, 0);
        AdjustTracker.trackClickout(voucher.getVoucherId());
    }

    public void trackSeeMore(String retailerName, String seeMoreMethod) {

        String action = ACTION_SEE_MORE + seeMoreMethod;
        String label = LABEL_SEE_MORE + retailerName;
        trackEvent(action, CATEGORY_SEE_MORE, label, 0);
    }

    public void trackOnboarding(String actionType, long time, boolean isFilterUser, String filterNames) {
        if(actionType.equals(ONBOARDING_ACTION_SUCCESS)) {
            trackEvent(ACTION_ONBOARDING + actionType, CATEGORY_ONBOARDING,
                    String.format(LABEL_ONBOARDING, "" + isFilterUser, filterNames), time);
        }else{
            trackEvent(ACTION_ONBOARDING + actionType, CATEGORY_ONBOARDING, "", time);
        }
    }

    public void trackOpenApp(String openType, String retailerName, String voucherId) {
        String label;
        if(retailerName!=null && voucherId!=null){
            label = String.format(LABEL_OPEN_APP_BATCH, retailerName, voucherId);
        }else{
            label = "";
        }
        trackEvent(ACTION_OPEN_APP + openType, CATEGORY_OPEN_APP, label, 0);
    }

    public void trackSearch(String searchType, String searchTerm, boolean isSuccesfull, String suggestionType) {
        String label = String.format(LABEL_SEARCH, "" + isSuccesfull, searchTerm, suggestionType);
        trackEvent(ACTION_SEARCH + searchType, CATEGORY_SEARCH, label, 0);
    }

    public void trackVote(Voucher voucher, boolean isPositiveVote) {
        String action = ACTION_VOTE + (isPositiveVote ? VOTE_ACTION_YES : VOTE_ACTION_NO);
        String label = String.format(LABEL_VOTE, voucher.getType(), voucher.getRetailerName(), voucher.getVoucherId());
        trackEvent(action, CATEGORY_FEEDBACK, label, 0);
    }

    public void trackShare(Voucher voucher, String shareMethod) {
        String action = String.format(ACTION_SHARE, shareMethod, voucher.getType());
        String label = String.format(LABEL_SHARE, voucher.getRetailerName(), voucher.getVoucherId());
        trackEvent(action, CATEGORY_SHARE, label, 0);
    }

    public void trackSubscription(boolean isEnabled) {
        trackEvent(ACTION_SUBSCRIPTION + (isEnabled ? SUBSCRIPTION_ON : SUBSCRIPTION_OFF), CATEGORY_SUBSCRIPTION, "", 0);
    }

    public void trackCategoryWidget(String categoryName) {
        trackEvent(ACTION_WIDGET_CATEGORY, CATEGORY_HS_WIDGETS, LABEL_CATEGORY_NAME + categoryName, 0);
    }

    public void trackRetailerWidget(String retailerName) {
        trackEvent(ACTION_WIDGET_RETAILER, CATEGORY_HS_WIDGETS, LABEL_RETAILER_NAME + retailerName, 0);
    }

    public void trackBookmark(Voucher voucher, boolean isAdded, String screenName) {

        String action = isAdded ? ACTION_BOOKMARK_ADDED : ACTION_BOOKMARK_REMOVED;
        String label = String.format(LABEL_BOOKMARK, voucher.getType(), voucher.getRetailerName(), voucher.getVoucherId(), screenName);

        trackEvent(action, CATEGORY_BOOKMARK, label, 0);
    }

    public void trackBannerWidget(String action) {
        trackEvent(ACTION_WIDGET_BANNER, CATEGORY_HS_WIDGETS, LABEL_USER_ACTION + action, 0);
    }

    public void trackFavoriteRetailer(Retailer retailer, boolean isAdded, String screenName) {
        String action = isAdded ? ACTION_FAVORITE_ADDED : ACTION_FAVORITE_REMOVED;
        String label = String.format(LABEL_FAVORITE_RETAILER, retailer.getName(), screenName);
        trackEvent(action, CATEGORY_FAVORITE, label, 0);
    }

    public void trackExpireVoucherNotification(Voucher voucher) {
        String action = ACTION_EXPIRE_VOUCHER_RECEIVED;
        String label = String.format(LABEL_EXPIRE_VOUCHER, voucher.getVoucherId(), voucher.getRetailerName(), voucher.getType());
        trackEvent(action, CATEGORY_EXPIRE_VOUCHER, label, 0);
    }

    public void trackExpireVoucherNotificationClick(Voucher voucher) {
        String action = ACTION_EXPIRE_VOUCHER_TAPPED;
        String label = String.format(LABEL_EXPIRE_VOUCHER, voucher.getVoucherId(), voucher.getRetailerName(), voucher.getType());
        trackEvent(action, CATEGORY_EXPIRE_VOUCHER, label, 0);
    }

    public void trackPostNotificationAction(Notification.NotificationType notificationType, int codesCount, String action) {

        String label = LABEL_NOTIFICATION_TYPE;

        switch (notificationType) {
            case APP_NOTIF:
                label += (codesCount == 1 ? "CN" : "CNMultiple");
                break;
            case BATCH_AUTO_NOTIF:
            case BATCH_MANUAL_NOTIF:
                label += (codesCount == 1 ? "Batch" : "BatchMultiple");
                break;
        }

        trackEvent(action, CATEGORY_POST_NOTIFICATION, label, 0);
    }

    public void trackAddMoreRetailersWidget(){
        trackEvent(ACTION_WIDGET_ADD_RETAILERS, CATEGORY_HS_WIDGETS, "na", 0);
    }

    public void trackOrientationChange(String orientation, String screenName){
        trackEvent(ACTION_ORIENTATION + orientation, CATEGORY_DEVICE_ORIENTATION, LABEL_ORIENTATION + screenName, 0);
    }

    public void trackCategoryBanner(String retailerName, String categoryName) {
        String label = String.format(LABEL_CATEGORY_BANNER, retailerName, categoryName);
        trackEvent(ACTION_CATEGORY_BANNER, CATEGORY_RLP_WIDGET, label, 0);
    }

    public void trackLanguageSwitch(String language){
        trackEvent(ACTION_LANGUAGE_SWITCH, CATEGORY_LANGUAGE, language, 0);
    }

    public void trackEvent(String action, String category, String label, long value) {

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    public void trackScreen(String screenName, String extras) {
        trackScreen(screenName, extras, -1, "");
    }

    public void trackScreen(String screenName, String extras, int customDimension, String customDimensionValue) {
        if (extras != null) {
            tracker.setScreenName(screenName + extras);
        } else {
            tracker.setScreenName(screenName);
        }

        HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder();

        if (customDimension != -1) {
            builder.setCustomDimension(customDimension, customDimensionValue);
        }
        tracker.send(builder.build());
    }

    private String getOrientation(int orientation){
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            return ORIENTATION_LANDSCAPE;
        }else if(orientation == Configuration.ORIENTATION_PORTRAIT){
            return ORIENTATION_PORTRAIT;
        }else {
            return ORIENTATION_SQUARE;
        }
    }

    public static String getOpenType(Notification.NotificationType notificationType) {
        if (notificationType == null) {
            return OPEN_APP_METHOD_NOTIF_APP;
        }
        switch (notificationType) {
            case BATCH_AUTO_NOTIF:
                return OPEN_APP_METHOD_NOTIF_BATCH_AUTO;
            case BATCH_MANUAL_NOTIF:
                return OPEN_APP_METHOD_NOTIF_BATCH_MANUAL;
            case APP_NOTIF:
            default:
                return OPEN_APP_METHOD_NOTIF_APP;
        }
    }

    public static final int RETAILER_NUMBER_CUSTOM_DIMENSION = 12;

    public static final String CATEGORY_CLICKOUT = "clickout";
    public static final String CATEGORY_SEE_MORE = "See More";
    public static final String CATEGORY_ONBOARDING = "Onboarding";
    public static final String CATEGORY_OPEN_APP = "Open App";
    public static final String CATEGORY_FEEDBACK = "voucherFeedbackVote";
    public static final String CATEGORY_SUBSCRIPTION = "Notification Subscription";
    public static final String CATEGORY_SEARCH = "Search";
    public static final String CATEGORY_SHARE = "Voucher Share";
    public static final String CATEGORY_HS_WIDGETS = "HS Widgets";
    public static final String CATEGORY_BOOKMARK = "Voucher Bookmark";
    public static final String CATEGORY_FAVORITE = "Retailer Favorite";
    public static final String CATEGORY_NOTIFICATION_SETTINGS = "Notification Settings Navigation";
    public static final String CATEGORY_EXPIRE_VOUCHER = "Expiring Voucher Notification";
    public static final String CATEGORY_POST_NOTIFICATION = "Post Notification Actions";
    public static final String CATEGORY_DEVICE_ORIENTATION = "Device Orientation";
    public static final String CATEGORY_RLP_WIDGET = "RLP Widgets";
    public static final String CATEGORY_LANGUAGE = "Language settings";

    public static final String ACTION_CLICKOUT = "clickoutMethod:";
    public static final String ACTION_SEE_MORE = "seeMoreMethod:";
    public static final String ACTION_ONBOARDING = "onboardingAction:";
    public static final String ACTION_OPEN_APP = "openAppMethod:";
    public static final String ACTION_VOTE = "didOfferWork:";
    public static final String ACTION_SUBSCRIPTION = "allNotifications:";
    public static final String ACTION_SEARCH = "searchMethod:";
    public static final String ACTION_SHARE = "shareMethod:%1$s - voucherType:%2$s";
    public static final String ACTION_BOOKMARK_ADDED = "userAction:bookmarkAdded";
    public static final String ACTION_BOOKMARK_REMOVED = "userAction:bookmarkRemoved";
    public static final String ACTION_WIDGET_CATEGORY = "widgetName:Categories";
    public static final String ACTION_WIDGET_RETAILER = "widgetName:Retailers";
    public static final String ACTION_WIDGET_BANNER = "widgetName:TutorialBanner";
    public static final String ACTION_FAVORITE_ADDED = "userAction:favoriteAdded";
    public static final String ACTION_FAVORITE_REMOVED = "userAction:favoriteRemoved";
    public static final String ACTION_OPTIONS_BUTTON = "navigationMethod:NotificationOptionsButton";
    public static final String ACTION_EXPIRE_VOUCHER_RECEIVED = "userAction:Received";
    public static final String ACTION_EXPIRE_VOUCHER_TAPPED = "userAction:Tapped";
    public static final String ACTION_BACK_BUTTON = "userAction:Back Button Tap";
    public static final String ACTION_LEAVE_APP = "userAction:Leave App";
    public static final String ACTION_WIDGET_ADD_RETAILERS = "widgetName:AddRetailers";
    public static final String ACTION_ORIENTATION = "orientation:";
    public static final String ACTION_CATEGORY_BANNER = "widgetName:Category Banner";
    public static final String ACTION_LANGUAGE_SWITCH = "Language switch";

    public static final String LABEL_CLICKOUT = "voucherType:%1$s - retailerName:%2$s - voucherId:%3$s - isBookmarked:";
    public static final String LABEL_SEE_MORE = "retailerName:";
    public static final String LABEL_VOTE = "voucherType:%1$s - retailerName:%2$s - voucherId:%3$s";
    public static final String LABEL_SEARCH = "isSuccessful:%1$s - searchKeyword:%2$s - suggestionType:%3$s";
    public static final String LABEL_SHARE = "retailerName:%1$s - voucherId:%2$s";
    public static final String LABEL_CATEGORY_NAME = "categoryName:";
    public static final String LABEL_RETAILER_NAME = "retailerName:";
    public static final String LABEL_BOOKMARK = "voucherType:%1$s - retailerName:%2$s - voucherId:%3$s - location:%4$s";
    public static final String LABEL_USER_ACTION = "userAction:";
    public static final String LABEL_FAVORITE_RETAILER = "retailerName:%1$s - location:%2$s";
    public static final String LABEL_EXPIRE_VOUCHER = "voucherId:%1$s - retailerName:%2$s - voucherType:%3$s";
    public static final String LABEL_NOTIFICATION_TYPE = "notificationType:";
    public static final String LABEL_ORIENTATION = "location:";
    public static final String LABEL_CATEGORY_BANNER = "retailerName:%1$s - categoryName:%2$s";
    public static final String LABEL_OPEN_APP_BATCH = "retailerName:%1$s - voucherId:%2$s";
    public static final String LABEL_ONBOARDING = "isFilterUsed:%1$s - filterNames:%2$s";

    public static final String CLICKOUT_METHOD_NOTIFICATION_CN = "notificationCN";
    public static final String CLICKOUT_METHOD_NOTIFICATION_BATCH = "notificationBatch";
    public static final String CLICKOUT_METHOD_IN_APP_CODE = "inAppCode";
    public static final String CLICKOUT_METHOD_IN_APP_DEAL = "inAppDeal";
    public static final String CLICKOUT_METHOD_SHARE_CODE = "inAppCodeBrowserShare";
    public static final String CLICKOUT_METHOD_SHARE_DEAL = "inAppDealBrowserShare";
    public static final String CLICKOUT_METHOD_BATCH_MANUAL = "notificationBatchManualcampaign";
    public static final String CLICKOUT_METHOD_BATCH_AUTO = "notificationBatchAutocampaign";

    public static final String SEEMORE_METHOD_NOTIFICATION_CN = "notificationCN";
    public static final String SEEMORE_METHOD_NOTIFICATION_CN_MULTIPLE = "notificationCNMultiple";
    public static final String SEEMORE_METHOD_NOTIFICATION_BATCH = "notificationBatch";
    public static final String SEEMORE_METHOD_NOTIFICATION_BATCH_MULTIPLE = "notificationBatchMultiple";

    public static final String ONBOARDING_ACTION_START = "Start";
    public static final String ONBOARDING_ACTION_SKIP = "Skip";
    public static final String ONBOARDING_ACTION_SUCCESS = "Success";

    public static final String OPEN_APP_METHOD_NOTIF_APP = "notificationCN";
    public static final String OPEN_APP_METHOD_NOTIF_BATCH_AUTO = "notificationBatchAutocampaign";
    public static final String OPEN_APP_METHOD_NOTIF_BATCH_MANUAL = "notificationBatchManualcampaign";

    public static final String OPEN_APP_METHOD_SHARE = "browserShare";
    public static final String OPEN_APP_METHOD_MANUAL = "manual";

    public static final String SEARCH_METHOD_MANUAL = "Manual";
    public static final String SEARCH_METHOD_SUGGESTION = "Suggestion";

    public static final String SEARCH_SUGG_TYPE_RETAILER = "RETAILER";
    public static final String SEARCH_SUGG_TYPE_HISTORIC = "HISTORIC";
    public static final String SEARCH_SUGG_TYPE_NA = "n/a";

    public static final String SCREEN_NAME_RETAILER = "retailerScreen_";
    public static final String SCREEN_NAME_RETAILERS = "retailerScreen";
    public static final String SCREEN_NAME_VOUCHER = "voucherScreen";
    public static final String SCREEN_NAME_HOME = "homeScreen";
    public static final String SCREEN_NAME_NOTIFICATIONS = "notificationsScreen";
    public static final String SCREEN_NAME_FAVOURITE = "favoritesScreen";
    public static final String SCREEN_NAME_SETTINGS = "settingsScreen";
    public static final String SCREEN_NAME_BOOKMARKS = "bookmarksScreen";
    public static final String SCREEN_NAME_CATEGORY = "categoryScreen";
    public static final String SCREEN_NAME_ONBOARDING = "onboardingScreen";
    public static final String SCREEN_APPEND_OVERLAY = "_DetailsOverlay";
    public static final String LOCATION_NOTIFICATION = "Notification";


    public static final String VOTE_ACTION_YES = "yes";
    public static final String VOTE_ACTION_NO = "no";

    public static final String SUBSCRIPTION_ON = "on";
    public static final String SUBSCRIPTION_OFF = "off";

    public static final String SHARE_METHOD_FB = "Facebook Messenger";
    public static final String SHARE_METHOD_MAIL = "Email";
    public static final String SHARE_METHOD_WHATSAPP = "Whatsapp";
    public static final String SHARE_METHOD_OTHER = "Other:";

    public static final String WIDGET_ACTION_TAP = "Tap";
    public static final String WIDGET_ACTION_SWIPE = "Swipe";

    public static final String ORIENTATION_TO_PORTRAIT = "toPortrait";
    public static final String ORIENTATION_TO_LANDSCAPE = "toLandscape";

    public static final String ORIENTATION_PORTRAIT = "Portrait";
    public static final String ORIENTATION_LANDSCAPE = "Landscape";
    public static final String ORIENTATION_SQUARE = "Square";
}
