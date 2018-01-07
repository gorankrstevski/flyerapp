package com.cuponation.android.service.local;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

/**
 * Created by goran on 12/13/17.
 */

public class DummyBatchService implements IBatchService {
    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }

    @Override
    public void onNewIntent(Activity activity, Intent intent) {

    }

    @Override
    public void showBatchLanding(Context context, Intent intent, FragmentManager fragmentManager) {

    }

    @Override
    public void writeToIntent(Object batchMessage, Intent intent) {

    }

    @Override
    public void setAttribute(String key, boolean value) {

    }

    @Override
    public void addTag(String key, String value) {

    }

    @Override
    public void removeTag(String key, String value) {

    }

    @Override
    public void trackEvent(String eventName, String eventLabel, String dataKey, String dataValue) {

    }
}
