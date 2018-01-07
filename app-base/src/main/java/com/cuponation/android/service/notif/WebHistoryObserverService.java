package com.cuponation.android.service.notif;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.cuponation.android.observer.HistoryObserver;

/**
 * Created by goran on 7/8/16.
 */

public class WebHistoryObserverService extends IntentService {

    String CHROME_URI = "content://com.android.chrome.browser/history";
    String BROWSER_URI = "content://browser/bookmarks";

    public WebHistoryObserverService() {
        // Used to name the worker thread, important only for debugging.
        super("history-observer-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        getContentResolver().registerContentObserver(Uri.parse(BROWSER_URI), true, new HistoryObserver(this, BROWSER_URI));
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            getContentResolver().registerContentObserver(Uri.parse(CHROME_URI), true, new HistoryObserver(this, CHROME_URI));
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("debug" , "on Start Command");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("debug", "service on destroy");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

}