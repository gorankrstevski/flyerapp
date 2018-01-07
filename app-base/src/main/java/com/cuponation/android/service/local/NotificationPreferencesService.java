package com.cuponation.android.service.local;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

/**
 * Created by goran on 9/11/17.
 */

public class NotificationPreferencesService {

    private static String NOTIFICATION_PREFERENCES_KEY = "push_notif_pref";

    private static NotificationPreferencesService me;

    private Map<String, Long> retailerLastNotificationTimestamp;

    private NotificationPreferencesService() {
        initialize();
    }

    public static NotificationPreferencesService getInstance() {
        if (me == null) {
            me = new NotificationPreferencesService();
        }
        return me;
    }

    private void initialize() {
        retailerLastNotificationTimestamp = getRetailersMap();
        if (retailerLastNotificationTimestamp == null) {
            retailerLastNotificationTimestamp = new HashMap<>();
            Paper.book().write(NOTIFICATION_PREFERENCES_KEY, retailerLastNotificationTimestamp);
        }
    }

    private Map<String, Long> getRetailersMap() {
        return Paper.book().read(NOTIFICATION_PREFERENCES_KEY);
    }

    public void updateRetailerNotification(String retailerId) {
        retailerLastNotificationTimestamp.put(retailerId, Long.valueOf(System.currentTimeMillis()));
        Paper.book().write(NOTIFICATION_PREFERENCES_KEY, retailerLastNotificationTimestamp);
    }

    public long getLastRetailerNotification(String retailerId) {
        Long timestamp = retailerLastNotificationTimestamp.get(retailerId);
        if (timestamp == null) {
            return 0;
        }
        return timestamp.longValue();
    }
}
