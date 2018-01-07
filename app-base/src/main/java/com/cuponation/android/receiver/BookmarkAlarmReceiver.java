package com.cuponation.android.receiver;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.cuponation.android.service.notif.ExpiredVouchersJobService;
import com.cuponation.android.service.notif.ExpiredVouchersService;

/**
 * Created by goran on 3/21/17.
 */

public class BookmarkAlarmReceiver extends BroadcastReceiver {
    private int EXPIRED_VOUCHERS_JOB_ID = 101;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent serviceIntent = new Intent(context, ExpiredVouchersService.class);
            context.startService(serviceIntent);
        }else{
            JobScheduler jobScheduler =
                    (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(new JobInfo.Builder(EXPIRED_VOUCHERS_JOB_ID,
                    new ComponentName(context, ExpiredVouchersJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build());
            Log.d("goran", "job scheduled");
        }

    }
}
