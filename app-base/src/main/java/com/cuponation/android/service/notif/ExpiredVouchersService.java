package com.cuponation.android.service.notif;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.util.SharedPreferencesUtil;

/**
 * Created by goran on 3/21/17.
 */

public class ExpiredVouchersService extends IntentService {

    public ExpiredVouchersService() {
        super("ExpiredVouchersService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(SharedPreferencesUtil.getInstance().isBokmarkNotificationEnabled()) {
            BookmarkVoucherService.getInstance().sendNotificationForSoonExpiredVouchers(getApplicationContext());
        }
    }
}
