package com.cuponation.android.service.local;


import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.storage.base.FileStorageManager;
import com.cuponation.android.util.FileNameUtil;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 7/22/16.
 */

public class NotificationService {

    private static int NOTIFICATION_COUNT_LIMIT = 100;
    private FileStorageManager fileStorageManager;

    private static NotificationService me;

    private NotificationService(){
        super();
        initialize();
    }

    public static NotificationService getInstance(){
        if(me == null){
            me = new NotificationService();
        }
        return me;
    }

    public void initialize(){
        fileStorageManager = new FileStorageManager(FileNameUtil.getNotificationsStorageFile(CouponingApplication.getContext()));
    }

    public void addNewNotification(Notification notification){
        try {
            fileStorageManager.add(notification, new TypeToken<List<Notification>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getAllNotifications(){
        List<Notification> notifications = fileStorageManager.getAll(new TypeToken<List<Notification>>(){}.getType());
        if(notifications!=null && notifications.size() > NOTIFICATION_COUNT_LIMIT){
            try {
                fileStorageManager.clear();
                List<Notification> result = notifications.subList(0, NOTIFICATION_COUNT_LIMIT);
                fileStorageManager.addList(result, new TypeToken<List<Notification>>(){}.getType());
                return  result;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return notifications;
    }

    public void removeNotification(Notification notification){
        try {
            List<Notification> notifications = getAllNotifications();
            int position = 0;
            for(Notification item : notifications){
                if(item.getDate() == notification.getDate()){
                    fileStorageManager.remove(position, new TypeToken<List<Notification>>(){}.getType());
                }
                position++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateNotification(Notification notification){
        try {
            List<Notification> notifications = getAllNotifications();
            int position = 0;
            if(notifications!=null) {
                for (Notification item : notifications) {
                    if (item.getDate() == notification.getDate()) {
                        fileStorageManager.set(position, notification, new TypeToken<List<Notification>>() {
                        }.getType());
                    }
                    position++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Notification getNotification(long notificationId){
        List<Notification> allNotifications = getAllNotifications();
        for(Notification notification: allNotifications){
            if(notification.getDate() == notificationId){
                return notification;
            }
        }

        return null;
    }

    public List<Notification> getUnreadNotifications(){
        List<Notification> allNotifications = getAllNotifications();

        List<Notification> unreadNotifications = new ArrayList<>();

        for(Notification notification : allNotifications){
            if(notification.isUnread()){
                unreadNotifications.add(notification);
            }
        }

        return unreadNotifications;
    }

    public void markAllNotificationAsRead(){
        List<Notification> allNotifications = getAllNotifications();
        for(Notification notification : allNotifications){
            notification.setUnread(false);
        }

        try {
            fileStorageManager.clear();
            fileStorageManager.addList(allNotifications, new TypeToken<List<Notification>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void markNotificationAsRead(long date){
        List<Notification> allNotifications = getAllNotifications();
        for(Notification notification : allNotifications){
            if(notification.getDate() == date) {
                notification.setUnread(false);
                break;
            }
        }

        try {
            fileStorageManager.clear();
            fileStorageManager.addList(allNotifications, new TypeToken<List<Notification>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
