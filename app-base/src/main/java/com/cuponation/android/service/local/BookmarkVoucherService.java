package com.cuponation.android.service.local;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.eventbus.UpdateBottomBarEvent;
import com.cuponation.android.receiver.BookmarkAlarmReceiver;
import com.cuponation.android.storage.base.FileStorageManager;
import com.cuponation.android.tracking.AdjustTracker;
import com.cuponation.android.util.FileNameUtil;
import com.cuponation.android.util.NotificationsUtil;
import com.cuponation.android.util.TimeUtil;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goran on 3/3/17.
 */

public class BookmarkVoucherService {

    private static long EXPIRE_VOUCHERS_NOTIFY_PERIOD = (24 + 1) * 60 * 60 * 1000; // 24 + 1 hour

    private static BookmarkVoucherService me;

    private FileStorageManager fileStorageManager;

    private Map<String, Voucher> savedVouchersMap;

    private BookmarkVoucherService() {
        initialize();
    }

    public static BookmarkVoucherService getInstance() {
        if (me == null) {
            me = new BookmarkVoucherService();
        }
        return me;
    }

    public void initialize(){
        fileStorageManager = new FileStorageManager(FileNameUtil.getSavedVouchersStorageFile(CouponingApplication.getContext()));
        savedVouchersMap = new HashMap<>();
        List<Voucher> allVouchers = getAllSavedVouchers(false);
        for(Voucher voucher : allVouchers){
            savedVouchersMap.put(voucher.getVoucherId(), voucher);
        }
    }

    public List<Voucher> getAllSavedVouchers(boolean removeExpired) {
        List<Voucher> allVouchers = fileStorageManager.getAll(new TypeToken<List<Voucher>>() {
        }.getType());

        List<Voucher> resultList = new ArrayList<>();
        for(Voucher voucher : allVouchers){
            if(TimeUtil.getDiff(voucher.getEndDate())>= 0){
                resultList.add(voucher);
                if(voucher.getRetailerId() == null && voucher.getCompatRetailerName()!=null){
                    Retailer retailer = RetailerService.getInstance().matchRetailerName(voucher.getCompatRetailerName());
                    if(retailer!=null){
                        voucher.setRetailerId(retailer.getId());
                    }
                }
            }else if(removeExpired){
                removeVoucherFromSaved(voucher);
            }
        }

        Collections.sort(resultList, new Comparator<Voucher>() {
            @Override
            public int compare(Voucher v1, Voucher v2) {
                try {
                    return TimeUtil.parseTime(v1.getEndDate()) > TimeUtil.parseTime(v2.getEndDate()) ? 1 : -1;
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        return resultList;
    }

    public boolean isVoucherBookmarked(Voucher voucher){
        return savedVouchersMap.containsKey(voucher.getVoucherId());
    }

    public boolean addVoucherToSaved(Voucher voucher){
        AdjustTracker.trackBookmarkRetailer();
        try {
            if(!savedVouchersMap.containsKey(voucher.getVoucherId())) {
                fileStorageManager.add(voucher, new TypeToken<List<Voucher>>() {
                }.getType());
                savedVouchersMap.put(voucher.getVoucherId(), voucher);
            }
            return true;
        } catch (IOException e) {
            Log.d("debug", "Voucher could not be added to storage " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeVoucherFromSaved(Voucher voucher){
        return removeVoucherFromSaved(voucher.getVoucherId());
    }

    public boolean removeVoucherFromSaved(String voucherId){
        try {
            List<Voucher> vouchers = fileStorageManager.getAll(new TypeToken<List<Voucher>>() {
            }.getType());
            int position = 0;
            for(Voucher voucher1 : vouchers){
                if(voucher1.getVoucherId().equals(voucherId)){
                    break;
                }
                position++;
            }
            fileStorageManager.remove(position, new TypeToken<List<Voucher>>(){}.getType());
            savedVouchersMap.remove(voucherId);
            return true;
        } catch (IOException e) {
            Log.d("debug", "Voucher could not be added to storage " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void startBookmarkExpireAlarm(Context context){

        Intent intent = new Intent(context, BookmarkAlarmReceiver.class);
        PendingIntent recurringIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar firingCal= Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();

        firingCal.set(Calendar.HOUR, 6); // At the hour you wanna fire
        firingCal.set(Calendar.MINUTE, 0); // Particular minute
        firingCal.set(Calendar.SECOND, 0); // particular second
        firingCal.set(Calendar.AM_PM, Calendar.PM);

        long intendedTime = firingCal.getTimeInMillis();
        long currentTime = currentCal.getTimeInMillis();

        if(intendedTime >= currentTime){
            // you can add buffer time too here to ignore some small differences in milliseconds
            // set from today
            alarmManager.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, recurringIntent);
        } else{
            // set from next day
            // you might consider using calendar.add() for adding one day to the current day
            firingCal.add(Calendar.DAY_OF_MONTH, 1);
            intendedTime = firingCal.getTimeInMillis();

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, intendedTime, AlarmManager.INTERVAL_DAY, recurringIntent);
        }
    }

    public void sendNotificationForSoonExpiredVouchers(Context context){
        List<Voucher> vouchers = this.getAllSavedVouchers(false);
        for(Voucher voucher : vouchers){
            long expireTime = TimeUtil.getDiff(voucher.getEndDate());
            if(expireTime < EXPIRE_VOUCHERS_NOTIFY_PERIOD){
                NotificationsUtil.publishExpireVoucherNotification(context, voucher);
                EventBus.getDefault().post(new UpdateBottomBarEvent());
            }
        }
    }
}
