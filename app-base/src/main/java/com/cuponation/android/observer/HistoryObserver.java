package com.cuponation.android.observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.UniqueCode;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.service.remote.VoucherService;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.NotificationsUtil;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.cuponation.android.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

/**
 * Created by goran on 7/6/16.
 */

public class HistoryObserver extends ContentObserver {

    public final String TAG = "HistoryObserver";

    private static final long URL_CHECK_THRESHOLD = 60 * 60 * 1000;

    private Context context;
    private String uriString;

    private Map<String, Long> urlRegistry = new HashMap<>();

    public HistoryObserver(Context context, String uri) {
        super(null);
        this.context = context;
        this.uriString = uri;

        Log.d(TAG, "History Observer created " + uriString);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }

    @SuppressLint("NewApi")
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        if(!SharedPreferencesUtil.getInstance().getMyRetailersNotification() || CouponingApplication.isCustomTabOpened){
            return;
        }

        String[] projection = new String[]{Constants.DB_URL_KEY};
        String selection = Constants.DB_BOOKMARK_KEY  + " = 0"; // 0 = history, 1 = bookmark
        Uri providerURI = Uri.parse(uriString);
        Cursor mCursor = context.getContentResolver().query(providerURI, projection, selection, null, Constants.DB_DATE_SORT);
        //this.startManagingCursor(mCursor);
        mCursor.moveToFirst();

        if (mCursor.moveToLast() && mCursor.getCount() > 0) {

            while (!mCursor.isAfterLast()) {
                final String url = mCursor.getString(mCursor.getColumnIndex(Constants.DB_URL_KEY));
                final Retailer retailer = RetailerService.getInstance().matchUrl(url);
                if(retailer!=null && !isHostRecentlyChecked(retailer.getName())) {

                    VoucherService.getInstance().getMatchingVouchers(retailer).subscribe(
                            new Consumer<Map<String, Retailer>>() {
                                @Override
                                public void accept(Map<String, Retailer> retailerMap) throws Exception {
                                    final Retailer retailerFromResponse = retailerMap.get(retailer.getId());
                                    List<Voucher> result = retailerFromResponse.getVouchers();
                                    if (result != null && result.size() > 0) {
                                        if(Utils.isUniqueCodePresent(retailerFromResponse)){
                                            Voucher voucher = Utils.getCodeVouchers(retailerFromResponse).get(0);
                                            VoucherService.getInstance().getUniqueCode(voucher.getVoucherId(), Utils.getUniqueDeviceId(context, voucher.getEndDate()))
                                                    .subscribe(new Consumer<UniqueCode>() {
                                                        @Override
                                                        public void accept(UniqueCode uniqueCode) throws Exception {
                                                            NotificationsUtil.publishNotification(context, retailerFromResponse, url, uniqueCode.getCode());
                                                            UserInterestService.getInstance().addLikedRetailer(RetailerService.getInstance().matchUrl(url));
                                                        }
                                                    }, new Consumer<Throwable>() {
                                                        @Override
                                                        public void accept(Throwable throwable) throws Exception {
                                                            Log.e("debug", "Unique code could not be obtained");
                                                        }
                                                    });
                                        }else{
                                            NotificationsUtil.publishNotification(context, retailerFromResponse, url, null);
                                            UserInterestService.getInstance().addLikedRetailer(RetailerService.getInstance().matchUrl(url));
                                        }
                                    }
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.d("debug", "error ");
                                }
                            }
                    );
                }

                mCursor.moveToNext();
            }

        }
        mCursor.close();
    }

    private boolean isHostRecentlyChecked(String host) {
        long timestamp = urlRegistry.get(host)!=null ? urlRegistry.get(host) : 0;

        if (System.currentTimeMillis() - timestamp > URL_CHECK_THRESHOLD) {
            urlRegistry.put(host, System.currentTimeMillis());
            Log.d(TAG, "false isHostRecentlyChecked = " + host + " timestamp=" + (System.currentTimeMillis() - timestamp));
            return false;
        } else {
            Log.d(TAG, "true isHostRecentlyChecked = " + host + " timestamp=" + (System.currentTimeMillis() - timestamp));
            return true;
        }
    }
}
