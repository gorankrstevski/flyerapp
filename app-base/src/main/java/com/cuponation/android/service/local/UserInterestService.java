package com.cuponation.android.service.local;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.storage.base.FileStorageManager;
import com.cuponation.android.tracking.AdjustTracker;
import com.cuponation.android.tracking.BatchUtil;
import com.cuponation.android.util.CountryUtil;
import com.cuponation.android.util.FileNameUtil;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by goran on 7/15/16.
 */

public class UserInterestService {

    private static String[] ALLOWED_RETAILERS = new String[]{
            "venere",
            "groupon",
            "hawkers",
            "hotels.com",
            "nike",
            "adidas",
            "sephora",
            "asos",
            "lacoste",
            "yves rocher",
            "missguided",
            "converse",
            "reebok",
            "courir",
            "galeries lafayette",
            "levi's",
            "ralph lauren",
            "jd sports"};

    private FileStorageManager pushEnabledRetailersStorageManager;
    private FileStorageManager likedRetailersStorageManager;
    private FileStorageManager visitedRetailersStorageManager;
    private static UserInterestService me;

    private static Set<String> likedRetailerIds;
    private static Set<String> visitedRetailerIds;
    private static Set<String> pushEnabledRetailerIds;

    private UserInterestService() {
        super();
        initialize();
    }

    public static UserInterestService getInstance() {
        if (me == null) {
            me = new UserInterestService();
        }

        return me;
    }

    public void initialize() {

        ALLOWED_RETAILERS = CouponingApplication.getContext().getResources().getStringArray(getPushEnabledRetailers(CountryUtil.getCountryCode()));

        likedRetailersStorageManager = new FileStorageManager(FileNameUtil.getLikedRetailersStorageFile(CouponingApplication.getContext()));
        visitedRetailersStorageManager = new FileStorageManager(FileNameUtil.getVisitedRetailersStorageFile(CouponingApplication.getContext()));
        pushEnabledRetailersStorageManager = new FileStorageManager(FileNameUtil.getPushEnabledRetailersStorageFile(CouponingApplication.getContext()));

        likedRetailerIds = new HashSet<>();
        initializeSetWithIds(likedRetailerIds, getLikedRetailers());

        visitedRetailerIds = new HashSet<>();
        initializeSetWithIds(visitedRetailerIds, getVisitedRetailers());

        pushEnabledRetailerIds = new HashSet<>();
        initializeSetWithIds(pushEnabledRetailerIds , getPushEnabledRetailers());
    }

    private void initializeSetWithIds(Set<String> setIds, List<Retailer> retailers){
        if (retailers != null) {
            for (Retailer retailer : retailers) {
                setIds.add(retailer.getId());
            }
        }
    }

    public List<Retailer> getLikedRetailers() {
        return likedRetailersStorageManager.getAll(new TypeToken<List<Retailer>>() {
        }.getType());
    }

    public void addLikedRetailer(Retailer retailer) {
        // add to push enabled retailers
        if(isRetailerAllowed(retailer)){
            addPushEnabledRetailer(retailer);
        }

        SharedPreferencesUtil.getInstance().setUpdateNeededOnHome(true);
        BatchUtil.addFavouriteBrand(retailer.getName().toLowerCase());
        AdjustTracker.trackAddFavouriteRetailer();
        try {
            if (!likedRetailerIds.contains(retailer.getId())) {
                likedRetailerIds.add(retailer.getId());
                likedRetailersStorageManager.add(retailer, new TypeToken<List<Retailer>>() {
                }.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeFromLikedRetailers(Retailer retailer) {
        SharedPreferencesUtil.getInstance().setUpdateNeededOnHome(true);
        BatchUtil.removeFavouriteBrand(retailer.getName().toLowerCase());
        try {
            if (likedRetailerIds.contains(retailer.getId())) {

                Type type = new TypeToken<List<Retailer>>() {
                }.getType();
                likedRetailerIds.remove(retailer.getId());

                List<Retailer> retailers = likedRetailersStorageManager.getAll(type);

                int position = 0;
                for (Retailer iterator : retailers) {
                    if (iterator.getId().equals(retailer.getId())) {
                        likedRetailersStorageManager.remove(position, type);
                        return;
                    }
                    position++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRetailerLiked(String retailerId) {
        return likedRetailerIds.contains(retailerId);
    }

    // Visited retailers
    public void addVisitedRetailer(Retailer retailer) {
        try {
            if(!visitedRetailerIds.contains(retailer.getId())){

                if(isRetailerAllowed(retailer)){
                    addPushEnabledRetailer(retailer);
                }

                visitedRetailersStorageManager.add(retailer, new TypeToken<List<Retailer>>() {
                }.getType());
                visitedRetailerIds.add(retailer.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Retailer> getVisitedRetailers() {
        return visitedRetailersStorageManager.getAll(new TypeToken<List<Retailer>>() {
        }.getType());
    }

    // Push Enabled retailers

    public void addPushEnabledRetailer(Retailer retailer) {
        BatchUtil.setInterestPushOptin(retailer.getName(), true);
        try {
            if(!pushEnabledRetailerIds.contains(retailer.getId())) {
                pushEnabledRetailersStorageManager.add(retailer, new TypeToken<List<Retailer>>() {
                }.getType());
                pushEnabledRetailerIds.add(retailer.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Retailer> getPushEnabledRetailers() {
        return pushEnabledRetailersStorageManager.getAll(new TypeToken<List<Retailer>>() {}.getType());
    }

    public void removePushEnabledReatiler(Retailer retailer) {
        BatchUtil.setInterestPushOptin(retailer.getName(), false);
        try {

            if(pushEnabledRetailerIds.contains(retailer.getId())) {
                pushEnabledRetailerIds.remove(retailer.getId());
                Type type = new TypeToken<List<Retailer>>() {
                }.getType();

                List<Retailer> retailers = pushEnabledRetailersStorageManager.getAll(type);

                int position = 0;
                for (Retailer iterator : retailers) {
                    if (iterator.getId().equals(retailer.getId())) {
                        pushEnabledRetailersStorageManager.remove(position, type);
                        return;
                    }
                    position++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public boolean isRetailerPushEnabled(Retailer retailer){
//        return pushEnabledRetailerIds.contains(retailer.getId());
//    }

    public List<Retailer> getUserInterestRetaielrs(){
        List<Retailer> visitedRetailers = getVisitedRetailers();
        List<Retailer> likedRetailersRetailers = getLikedRetailers();

        List<Retailer> userInterestRetailers = new ArrayList<>();

        Set<String> ids = new HashSet<>();
        for(Retailer retailer : likedRetailersRetailers){
            if(isRetailerAllowed(retailer) && !ids.contains(retailer.getId())){
                userInterestRetailers.add(retailer);
                ids.add(retailer.getId());
            }
        }

        for(Retailer retailer : visitedRetailers){
            if(isRetailerAllowed(retailer) && !ids.contains(retailer.getId())){
                userInterestRetailers.add(retailer);
                ids.add(retailer.getId());
            }
        }

        return userInterestRetailers;
    }

    // temporary limitation
    private boolean isRetailerAllowed(Retailer retailer){
        return Arrays.asList(ALLOWED_RETAILERS).contains(retailer.getName().toLowerCase());
    }


    public static List<Retailer> filterRetailers(List<Retailer> retailers) {
        UserInterestService userInterestService = UserInterestService.getInstance();

        if (retailers != null && retailers.size() > 0) {
            int size = retailers.size();
            for (int i = size - 1; i >= 0; i--) {
                if (userInterestService.isRetailerLiked(retailers.get(i).getId())) {
                    retailers.remove(i);
                }
            }
        }

        return retailers;
    }

    private int getPushEnabledRetailers(String countryCode){
        if(CountryUtil.UK_COUNTRY_CODE.equals(countryCode)){
            return R.array.push_optin_retailers_uk;
        }else if(CountryUtil.SPAIN_COUNTRY_CODE.equals(countryCode)){
            return R.array.push_optin_retailers_es;
        }
        return R.array.push_optin_retailers_fr;
    }
}
