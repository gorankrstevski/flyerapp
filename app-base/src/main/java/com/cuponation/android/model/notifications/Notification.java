package com.cuponation.android.model.notifications;

import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;

/**
 * Created by goran on 7/22/16.
 */

public class Notification {

    public enum NotificationType {
        APP_NOTIF, BATCH_MANUAL_NOTIF, BATCH_AUTO_NOTIF, EXPIRED_NOTIF
    }

    private NotificationType type;
    private Retailer retailer;
    private String retailerLabel;
    private Voucher voucher;
    private long date;

    private String title;
    private String message;
    private String deeplink;
    private String iconUrl;

    private boolean isUnread = false;
    private boolean isAutoCampaign = false;

    public Notification(Voucher voucher, long timestamp) {
        this.voucher = voucher;
        this.date = timestamp;
        this.type = NotificationType.APP_NOTIF;
        this.isUnread = true;
    }

    public Notification(Retailer retailer, String label, long timestamp) {
        this.retailer = retailer;
        this.retailerLabel = label;
        this.date = timestamp;
        this.type = NotificationType.APP_NOTIF;
        this.isUnread = true;
    }

    public Notification(String title, String message, String deeplink, long time, String iconUrl, boolean isAutoCampaign){
        this.title = title;
        this.message = message;
        this.deeplink = deeplink;
        this.iconUrl = iconUrl;
        this.setDate(time);
        this.setType(isAutoCampaign ? NotificationType.BATCH_AUTO_NOTIF : NotificationType.BATCH_MANUAL_NOTIF);
        this.isUnread = true;
        this.isAutoCampaign = isAutoCampaign;
    }

    public Notification(String title, String message, long timestamp, Voucher voucher){
        this.title = title;
        this.message = message;
        this.voucher = voucher;
        this.setDate(timestamp);
        this.setType(NotificationType.EXPIRED_NOTIF);
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Retailer getRetailer() {
        return retailer;
    }

    public void setRetailer(Retailer retailer) {
        this.retailer = retailer;
    }

    public String getRetailerLabel() {
        return retailerLabel;
    }

    public void setRetailerLabel(String retailerLabel) {
        this.retailerLabel = retailerLabel;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public boolean isAutoCampaign() {
        return isAutoCampaign;
    }

    public void setAutoCampaign(boolean autoCampaign) {
        isAutoCampaign = autoCampaign;
    }
}
