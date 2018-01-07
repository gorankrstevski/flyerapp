package com.cuponation.android.tracking;

import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.util.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by goran on 9/16/16.
 */

public class BatchUtil {

    public static final String HAS_COUPON_WIZZARD_ATTR = "has_coupon_wizard";
    public static final String LIKED_BRANDS_TAG = "liked_brands";
    public static final String CATEGORIES_TAG = "user_interests";
    public static final String VISITED_BRAND_EVENT = "visited_brand";
    public static final String USED_CODE_EVENT = "used_code";

    public static final String INTEREST_PUSH_OPTIN_TAG = "push_optin_retailer";
    public static final String PROMO_OPTIN_ATTR = "push_optin_promo";

    public static final String CATEGORY_PROPERTY = "category";


    public static void setAttrHasCouponWizzard() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            CouponingApplication.getBatchService().setAttribute(HAS_COUPON_WIZZARD_ATTR, false);
        } else {
            CouponingApplication.getBatchService().setAttribute(HAS_COUPON_WIZZARD_ATTR, true);
        }

    }

    public static void setPromoNotificationsAttribute(boolean enabled){
        CouponingApplication.getBatchService().setAttribute(PROMO_OPTIN_ATTR, enabled);
    }

    public static void addFavouriteBrand(String brandName) {
        CouponingApplication.getBatchService().addTag(LIKED_BRANDS_TAG, Utils.stripAccentsAndSpaces(brandName));
    }

    public static void removeFavouriteBrand(String brandName) {
        CouponingApplication.getBatchService().removeTag(LIKED_BRANDS_TAG, Utils.stripAccentsAndSpaces(brandName));
    }

    public static void setInterest(List<Retailer> retailers) {

        Set<String> categories = new HashSet<>();

        if (retailers != null) {
            for (Retailer retailer : retailers) {
                if(retailer.getVouchers()!=null) {
                    for (Voucher voucher : retailer.getVouchers()) {
                        categories.add(voucher.getCategory());
                    }
                }
            }
        }

        for (String category : categories) {
            if (category != null) {
                CouponingApplication.getBatchService().addTag(CATEGORIES_TAG, Utils.stripAccentsAndSpaces(category));
            }
        }
    }

    public static void setInterest(String categoryName){
        if (categoryName != null) {
            CouponingApplication.getBatchService().addTag(CATEGORIES_TAG, Utils.stripAccentsAndSpaces(categoryName));
        }
    }

    public static void setInterestPushOptin(String brandName, boolean enablePush){
        if(enablePush) {
            CouponingApplication.getBatchService().addTag(INTEREST_PUSH_OPTIN_TAG, Utils.stripAccentsAndSpaces(brandName));
        }else{
            CouponingApplication.getBatchService().removeTag(INTEREST_PUSH_OPTIN_TAG, Utils.stripAccentsAndSpaces(brandName));
        }
    }

    public static void trackVisitedBrandEvent(String brand, String category) {
        CouponingApplication.getBatchService().trackEvent(
                VISITED_BRAND_EVENT, brand,
                CATEGORY_PROPERTY, category);
    }

    public static void trackUsedCodeEvent(String brand, String category) {
        CouponingApplication.getBatchService().trackEvent(
                USED_CODE_EVENT, brand,
                CATEGORY_PROPERTY, category);
    }

    public static void trackToNewAccount(List<Retailer> likedRetailer, List<Retailer> pushEnabledRetailers, List<Retailer> visitedRetailers){
        for(Retailer retailer : likedRetailer){
            BatchUtil.addFavouriteBrand(retailer.getName().toLowerCase());
        }

        for(Retailer retailer : pushEnabledRetailers){
            BatchUtil.addFavouriteBrand(retailer.getName().toLowerCase());
        }

        for(Retailer retailer : visitedRetailers){
            BatchUtil.trackVisitedBrandEvent(retailer.getName(), null);
        }
    }
}
