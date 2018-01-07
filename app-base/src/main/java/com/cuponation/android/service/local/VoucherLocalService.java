package com.cuponation.android.service.local;

import android.text.format.DateUtils;

import com.cuponation.android.model.Voucher;
import com.cuponation.android.util.TimeUtil;

import java.text.ParseException;

import io.paperdb.Paper;

/**
 * Created by goran on 11/13/17.
 */

public class VoucherLocalService {

    private final static String LAST_CHECK_TIMESTAMP_KEY = "lastTimestampCheck";
    private final static String PRE_LAST_CHECK_TIMESTAMP_KEY = "prelastTimestampCheck";

    private static VoucherLocalService me;

    private VoucherLocalService(){

    }

    public static VoucherLocalService getInstance(){
        if(me == null){
            me = new VoucherLocalService();
        }

        return me;
    }

    public boolean isVoucherNew(Voucher voucher, long lastCheckTime) {

        if(voucher.getLastUpdateTime() == null){
            return false;
        }

        try {
            long timestamp = TimeUtil.parseTime(voucher.getLastUpdateTime());

            if (timestamp > lastCheckTime) {
                return true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public long getLastSetupCheck(){

        long now = System.currentTimeMillis();

        long preLastCheckTimestamp = getPreLastTimestampCheck();
        long lastCheckTimestamp = getLastTimestampCheck();

        // set initial setup
        if(preLastCheckTimestamp == 0){
            preLastCheckTimestamp = now;
            setPreLastTimestampCheck(now);
        }

        //set initial setup
        if(lastCheckTimestamp == 0){
            lastCheckTimestamp = now;
            setLastTimestampCheck(now);
        }

        if(now-lastCheckTimestamp > DateUtils.DAY_IN_MILLIS){
            preLastCheckTimestamp = lastCheckTimestamp;
            setPreLastTimestampCheck(preLastCheckTimestamp);
        }

        setLastTimestampCheck(now);

        return preLastCheckTimestamp;

    }

    private long getLastTimestampCheck() {
        return Paper.book().read(LAST_CHECK_TIMESTAMP_KEY, 0l);
    }

    private void setLastTimestampCheck(long timestampCheck){
        Paper.book().write(LAST_CHECK_TIMESTAMP_KEY, timestampCheck);
    }

    private void setPreLastTimestampCheck(long timestampCheck){
        Paper.book().write(PRE_LAST_CHECK_TIMESTAMP_KEY, timestampCheck);
    }

    private long getPreLastTimestampCheck(){
        return Paper.book().read(PRE_LAST_CHECK_TIMESTAMP_KEY, 0l);
    }
}
