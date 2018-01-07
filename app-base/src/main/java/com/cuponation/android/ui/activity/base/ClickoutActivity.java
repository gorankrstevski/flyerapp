package com.cuponation.android.ui.activity.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.widget.Toast;

import com.cuponation.android.R;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.BaseActivity;
import com.cuponation.android.util.Utils;
import com.cuponation.android.util.VoucherClickoutUtil;
import com.cuponation.android.util.customtabs.CustomTabActivityHelper;

/**
 * Created by goran on 3/17/17.
 */

public class ClickoutActivity extends BaseActivity {

    protected CustomTabActivityHelper customTabActivityHelper;
    protected Voucher clickoutVoucher = null;
    protected String originalUrl = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTabActivityHelper = new CustomTabActivityHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.setCustomTabsCallback(new CNCustomTabsCallback());
        customTabActivityHelper.setConnectionCallback(new CustomTabActivityHelper.ConnectionCallback() {
            @Override
            public void onCustomTabsConnected() {
                if (clickoutVoucher != null) {
                    VoucherClickoutUtil.performClickout(clickoutVoucher, ClickoutActivity.this, customTabActivityHelper.getSession());
                    clickoutVoucher = null;
                }
            }

            @Override
            public void onCustomTabsDisconnected() {

            }
        });
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        customTabActivityHelper.unbindCustomTabsService(this);
        super.onStop();
    }

    class CNCustomTabsCallback extends CustomTabsCallback {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            super.onNavigationEvent(navigationEvent, extras);

            if (navigationEvent == CustomTabsCallback.NAVIGATION_FINISHED && originalUrl != null) {
                VoucherClickoutUtil.openCustomTab(originalUrl, ClickoutActivity.this);
            }

        }

        @Override
        public void extraCallback(String callbackName, Bundle args) {
            super.extraCallback(callbackName, args);
        }
    }

    public void performClickout(Voucher voucher){
        GATracker.getInstance(getApplicationContext()).trackClickout(voucher,
                voucher.isCode() ? GATracker.CLICKOUT_METHOD_IN_APP_CODE : GATracker.CLICKOUT_METHOD_IN_APP_DEAL,
                BookmarkVoucherService.getInstance().isVoucherBookmarked(voucher));
        if (Voucher.TYPE_CODE.equals(voucher.getType())) {
            Toast.makeText(this, R.string.cn_app_code_copied, Toast.LENGTH_SHORT).show();
            Utils.copyCodeToClipboard(voucher.getCode());
        } else {
            Toast.makeText(this, R.string.cn_app_opening_shop, Toast.LENGTH_SHORT).show();
        }
        VoucherClickoutUtil.performClickout(voucher, this, customTabActivityHelper.getSession());
    }
}
