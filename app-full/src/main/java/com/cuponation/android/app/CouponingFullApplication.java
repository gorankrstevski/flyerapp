package com.cuponation.android.app;

import android.support.v4.content.ContextCompat;

import com.batch.android.Batch;
import com.batch.android.Config;
import com.cuponation.android.BuildConfig;
import com.cuponation.android.service.BatchService;
import com.cuponation.android.service.UseButtonService;
import com.cuponation.android.util.SharedPreferencesUtil;

/**
 * Created by goran on 12/12/17.
 */

public class CouponingFullApplication extends CouponingApplication {


    @Override
    public void onCreate() {
        super.onCreate();

         initializeUseButton();
         useButtonService = new UseButtonService(getApplicationContext());

         batchService = new BatchService();
         initializeBatch();
    }

    private void initializeUseButton(){
        if (BuildConfig.DEBUG) {
            com.usebutton.sdk.Button.enableDebugLogging();
        }
        com.usebutton.sdk.Button.getButton(this).start();
    }

    private void initializeBatch() {
        if (SharedPreferencesUtil.getInstance().getMyRetailersNotification()) {
            Batch.Push.setGCMSenderId(getString(com.cuponation.android.R.string.gcm_sender_api));
        }
        Batch.Push.setSmallIconResourceId(com.cuponation.android.R.drawable.ic_notif_cn);
        Batch.Push.setNotificationsColor(ContextCompat.getColor(getApplicationContext(), com.cuponation.android.R.color.colorPrimary));
        Batch.Push.setManualDisplay(true);
        Batch.setConfig(new Config(getString(com.cuponation.android.R.string.batch_api_key)));
    }
}
