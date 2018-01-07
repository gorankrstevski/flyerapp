package com.cuponation.android.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.cuponation.android.BuildConfig;
import com.cuponation.android.R;
import com.cuponation.android.network.retrofit.RetrofitHelper;
import com.cuponation.android.service.local.DummyBatchService;
import com.cuponation.android.service.local.IBatchService;
import com.cuponation.android.service.local.IUseButtonService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.util.CountryUtil;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.google.android.instantapps.InstantApps;
import com.splunk.mint.Mint;

import io.paperdb.Paper;
import retrofit2.Retrofit;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by goran on 7/15/16.
 */

public class CouponingApplication extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static boolean isCustomTabOpened = false;

    private static GATracker tracker;
    private static Retrofit retrofit;

    protected static IUseButtonService useButtonService;

    public static IUseButtonService getUseButtonService(){
        return useButtonService;
    }

    protected static IBatchService batchService;

    public static IBatchService getBatchService(){
        if(batchService == null){
            batchService = new DummyBatchService();
        }
        return batchService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getBaseContext();

        CountryUtil.initializeCountrySelection();

        retrofit = RetrofitHelper.getRetrofitAdapter(context);

        Paper.init(context);

        // custom font lib config
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/font-regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // initialize Google Analyics
        tracker = GATracker.getInstance(getContext());

        // initialize Splunk Mint
        initializeSplunkMint();

        // initialize Adjust
        initializeAdjust();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }

    public static CouponingApplication get(Context context) {
        return (CouponingApplication) context.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static GATracker getGATracker() {
        return tracker;
    }

    private void initializeSplunkMint() {
        if (BuildConfig.ENABLE_SPLUNK && !InstantApps.isInstantApp(this)) {
            Mint.disableNetworkMonitoring();
            Mint.initAndStartSession(this, "4284fc45");
        }
    }

    private void initializeAdjust() {
        String appToken = getString(R.string.adjust_app_token);
        String environment = BuildConfig.DEBUG ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        Adjust.onCreate(config);
        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

}
