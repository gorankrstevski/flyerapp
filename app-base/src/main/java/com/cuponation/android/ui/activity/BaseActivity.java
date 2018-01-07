package com.cuponation.android.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;

import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.util.DialogUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by goran on 7/15/16.
 */

@SuppressLint("Registered")
public class BaseActivity extends RxAppCompatActivity {

    protected String screenName;

    private static final String TAG = BaseActivity.class.getSimpleName();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        CouponingApplication.getBatchService().onStart(this);
    }

    @Override
    protected void onStop() {
        CouponingApplication.getBatchService().onStop(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        CouponingApplication.getBatchService().onDestroy(this);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        CouponingApplication.getBatchService().onNewIntent(this, intent);
        super.onNewIntent(intent);
    }

    protected void onResume() {
        super.onResume();
        CouponingApplication.isCustomTabOpened = false;

        if (!isNetworkOnline()) {
            DialogUtil.showNoConnectionDialog(BaseActivity.this);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public boolean isNetworkOnline() {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        GATracker.getInstance(getApplicationContext()).trackOrientationChange(
                newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ?
                        GATracker.ORIENTATION_TO_LANDSCAPE :
                        GATracker.ORIENTATION_TO_PORTRAIT, screenName);
    }
}
