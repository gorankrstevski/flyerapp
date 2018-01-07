package com.cuponation.android.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cuponation.android.service.notif.WebHistoryObserverService;
import com.google.android.instantapps.InstantApps;

/**
 * Created by goran on 7/8/16.
 */

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent arg1) {
        if(!InstantApps.isInstantApp(context)) {
            if (arg1 != null && Intent.ACTION_BOOT_COMPLETED.equals(arg1.getAction())) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    Intent intent = new Intent(context, WebHistoryObserverService.class);
                    context.startService(intent);
                    startAlarm(context);
                }
            }
        }
    }


    private void startAlarm(Context context){
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent recurringIntent= PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 30*1000, recurringIntent); // Log repetition

    }
}
