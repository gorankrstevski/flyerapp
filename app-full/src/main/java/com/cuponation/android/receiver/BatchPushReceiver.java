package com.cuponation.android.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.cuponation.android.service.notif.BatchPushService;
import com.cuponation.android.util.SharedPreferencesUtil;

/**
 * Created by goran on 9/21/16.
 */

public class BatchPushReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(SharedPreferencesUtil.getInstance().getMyRetailersNotification()){
            ComponentName comp = new ComponentName(context.getPackageName(), BatchPushService.class.getName());
            startWakefulService(context, intent.setComponent(comp));
        }
        setResultCode(Activity.RESULT_OK);
    }

}