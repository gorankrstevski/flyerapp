package com.cuponation.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.cuponation.android.service.notif.WebHistoryObserverService;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.google.android.instantapps.InstantApps;

/**
 * Created by goran on 7/8/16.
 */

public class AlarmReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent arg1) {
        if(!InstantApps.isInstantApp(context) && SharedPreferencesUtil.getInstance().getMyRetailersNotification()){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Intent intent = new Intent(context, WebHistoryObserverService.class);
                context.startService(intent);
            }
        }
    }

}
