package com.cuponation.android.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

import com.batch.android.Batch;
import com.batch.android.BatchMessage;
import com.batch.android.BatchMessagingException;
import com.batch.android.BatchPushPayload;
import com.batch.android.json.JSONException;
import com.batch.android.json.JSONObject;
import com.cuponation.android.service.local.IBatchService;
import com.cuponation.android.util.Utils;

/**
 * Created by goran on 12/13/17.
 */

public class BatchService implements IBatchService {
    @Override
    public void onStart(Activity activity) {
        Batch.onStart(activity);
    }

    @Override
    public void onStop(Activity activity) {
        Batch.onStop(activity);
    }

    @Override
    public void onDestroy(Activity activity) {
        Batch.onDestroy(activity);
    }

    @Override
    public void onNewIntent(Activity activity, Intent intent) {
        Batch.onNewIntent(activity, intent);
    }

    @Override
    public void showBatchLanding(Context context, Intent intent, FragmentManager fragmentManager) {
        try {

            BatchMessage message = BatchMessage.getMessageForBundle(intent.getExtras());
            if(message!=null) {
                Batch.Messaging.loadFragment(context, message)
                        .show(fragmentManager, "batch-landing");
            }
        } catch (BatchPushPayload.ParsingException e) {
            //most probably batch message is not present
        } catch (BatchMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToIntent(Object batchMessage, Intent intent) {
        ((BatchMessage)batchMessage).writeToIntent(intent);
    }

    @Override
    public void setAttribute(String key, boolean value) {
        Batch.User.editor().setAttribute(key, value).save();
    }

    @Override
    public void addTag(String key, String value) {
        Batch.User.editor().addTag(key, value).save();
    }

    @Override
    public void removeTag(String key, String value) {
        Batch.User.editor().removeTag(key, value).save();
    }

    public void trackEvent(String eventName, String eventLabel, String dataKey, String dataValue){
        JSONObject data = new JSONObject();
        try {
            if (dataValue != null) {
                data.put(dataKey, Utils.stripAccentsAndSpaces(dataValue.toLowerCase()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Batch.User.trackEvent(eventName, Utils.stripAccentsAndSpaces(eventLabel.toLowerCase()), data);
    }
}
