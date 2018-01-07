package com.cuponation.android.service.local;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

/**
 * Created by goran on 12/13/17.
 */

public interface IBatchService {

    void onStart(Activity activity);
    void onStop(Activity activity);
    void onDestroy(Activity activity);
    void onNewIntent(Activity activity, Intent intent);

    void showBatchLanding(Context context, Intent intent, FragmentManager fragmentManager);

    void writeToIntent(Object batchMessage, Intent intent);

    void setAttribute(String key, boolean value);

    void addTag(String key, String value);

    void removeTag(String key, String value);

    void trackEvent(String eventName, String eventLabel, String dataKey, String dataValue);
}
