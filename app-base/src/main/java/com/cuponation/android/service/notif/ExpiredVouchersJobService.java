package com.cuponation.android.service.notif;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.util.SharedPreferencesUtil;

/**
 * Created by goran on 12/15/17.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ExpiredVouchersJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        Log.d("goran", "job runned");
        if(SharedPreferencesUtil.getInstance().isBokmarkNotificationEnabled()) {
            new AsyncTask<Void, Void, Boolean>(){

                @Override
                protected Boolean doInBackground(Void... voids) {
                    BookmarkVoucherService.getInstance().sendNotificationForSoonExpiredVouchers(getApplicationContext());
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    jobFinished(jobParameters, !aBoolean);
                }
            }.execute();

        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
