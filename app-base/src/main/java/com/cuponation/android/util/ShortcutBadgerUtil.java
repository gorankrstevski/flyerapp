package com.cuponation.android.util;

import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.service.local.NotificationService;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by goran on 7/13/17.
 */

public class ShortcutBadgerUtil {

    public static void updateAppBadge(NotificationService notificationService){
        int countUnread = notificationService.getUnreadNotifications().size();
        if(countUnread > 0) {
            ShortcutBadger.applyCount(CouponingApplication.getContext(), countUnread);
        }else {
            ShortcutBadger.removeCount(CouponingApplication.getContext());
        }
    }
}
