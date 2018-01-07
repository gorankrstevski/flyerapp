package com.cuponation.android.tracking;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by goran on 12/16/16.
 */

public class AdjustTracker {

    private static final String CLICKOUT_TOKEN = "zhdz5h";

    private static final String RETAILER_FAVORITE_TOKEN = "de4v1a";

    private static final String VOUCHER_BOOKMARK_TOKEN = "3ssv88";

    private static final String VOUCHER_SHARE_TOKEN = "8oswmu";

    private static final String VOUCHER_FEEDBACK_VOTE_TOKEN  = "4tjjm9";

    private static final String VIEW_CONTENT = "nvl9mz";

    public static void trackClickout(String voucherId){
        trackEvent(CLICKOUT_TOKEN, getParams(voucherId));
    }

    public static void trackViewContent(String voucherId){
        trackEvent(VIEW_CONTENT, getParams(voucherId));
    }

    public static void trackAddFavouriteRetailer(){
        trackEvent(RETAILER_FAVORITE_TOKEN, null);
    }

    public static void trackBookmarkRetailer(){
        trackEvent(VOUCHER_BOOKMARK_TOKEN, null);
    }

    public static void trackShare(){
        trackEvent(VOUCHER_SHARE_TOKEN, null);
    }

    public static void trackVoucherFeedbackVote(){
        trackEvent(VOUCHER_FEEDBACK_VOTE_TOKEN, null);
    }

    public static void trackEvent(String event, Map<String, String> params){
        AdjustEvent adjustEvent = new AdjustEvent(event);

        if(params!=null && !params.isEmpty()) {
            for(String key : params.keySet()) {
                adjustEvent.addPartnerParameter(key, params.get(key));
            }
        }
        Adjust.trackEvent(adjustEvent);
    }

    private static Map<String, String> getParams(String voucherId){
        Map<String, String> params = new HashMap<>();
        params.put("fb_content_id", voucherId);
        params.put("fb_content_type", "product");
        return params;
    }
}
