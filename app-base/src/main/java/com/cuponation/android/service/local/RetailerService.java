package com.cuponation.android.service.local;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.cuponation.android.BuildConfig;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Category;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.network.RequestHelper;
import com.cuponation.android.storage.base.FileStorageManager;
import com.cuponation.android.util.CountryUtil;
import com.cuponation.android.util.FileNameUtil;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

/**
 * Created by goran on 7/12/16.
 */

public class RetailerService {

    private static String[] popularBrands = new String[]{
            "adidas",
            "amazon",
            "nike",
            "yves rocher",
            "groupon",
            "puma",
            "boulanger",
            "la redoute",
            "galeries lafayette",
            "hotels.com",
            "cdiscount",
            "showroomprivé",
            "sephora",
            "courir",
            "asos",
            "bonprix",
            "zalando",
            "sfr",
            "converse",
            "blancheporte",
            "home24",
            "i-run",
            "jbl"
    };

    private static final int LEADING_CATEGORY_RETAILERS_COUNT = 5;

    private static final int INTERVAL_DAY = 24 * 60 * 60 * 1000;

    private static List<Retailer> retailerList = new ArrayList<>();

    private static Map<String, List<Retailer>> cachedRetailersData = null;

    private FileStorageManager fileStorageManager;
    private static RetailerService me;

    private RetailerService() {
        super();

        initialize();
    }

    public static RetailerService getInstance() {
        if (me == null) {
            me = new RetailerService();
        }
        return me;
    }

    public void initialize() {
        fileStorageManager = new FileStorageManager(FileNameUtil.getRetailersStorageFile(CouponingApplication.getContext()));
        cachedRetailersData = new HashMap<>();
        retailerList = getAllRetailers();
    }

    /**
     * Check provided URL as input against all retailers. If provided url is matched with retailer
     * URL then retailer object is returned.
     *
     * @param url - url that should be checked
     * @return Retailer which match with provided url
     */
    public Retailer matchUrl(String url) {

        if (retailerList == null || retailerList.size() == 0) {
            return null;
        }

        for (Retailer retailer : retailerList) {
            String host = Uri.parse(retailer.getDomain()).getHost();
            if (isHostMatched(host, url)) {
                return retailer;
            } else if (host.contains("." + CountryUtil.getCountryDomain())) {
                String alterateHost = host.replace("." + CountryUtil.getCountryDomain(), ".com");
                if (isHostMatched(alterateHost, url)) {
                    return retailer;
                }
            } else if (host.contains(".com")) {
                String alterateHost = host.replace(".com", "." + CountryUtil.getCountryDomain());
                if (isHostMatched(alterateHost, url)) {
                    return retailer;
                }
            }
        }

        return null;
    }

    private boolean isHostMatched(String host, String url) {
        return host != null && url.contains(host.replace("www.", ""));
    }

    /**
     * Find if ther is a retailer with provided name as input parametar
     *
     * @param term - retailer name that should be checked
     * @return Retailer which match with provided name
     */
    public Retailer matchRetailerName(String term) {

        if (retailerList != null) {
            for (Retailer retailer : retailerList) {

                if (term.toLowerCase().equals(retailer.getName().toLowerCase())) {
                    return retailer;
                }
            }
        }

        return null;
    }

    /**
     * Download and cache all retailers(short info)
     *
     * @param retryCounter - number of retries if download fail
     */
    public synchronized Observable<List<Retailer>> cacheLocallyAllRetailers(final int retryCounter) {

        // use retry counter,since the service call sometimes fails on first call
        if (retryCounter == 0) {
            return Observable.create(new ObservableOnSubscribe<List<Retailer>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Retailer>> e) throws Exception {
                    e.onError(new Throwable("Number of retries 3. All failed."));
                    e.onComplete();
                }
            });
        }

        long timestamp = SharedPreferencesUtil.getInstance().getRetailersDataTimestamp();

        if (System.currentTimeMillis() - timestamp < INTERVAL_DAY) {
            //retailersLoadedCallback.onRetailersLoaded(getAllRetailers());
            return Observable.create(new ObservableOnSubscribe<List<Retailer>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Retailer>> e) throws Exception {
                    e.onNext(getSortedPerUserPreferences(getSortedAllRetailers(getAllRetailers())));
                    e.onComplete();
                }
            });
        }

        try {
            fileStorageManager.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return RequestHelper.getInstance().getAllRetailers().map(new Function<List<Retailer>, List<Retailer>>() {
            @Override
            public List<Retailer> apply(List<Retailer> response) throws Exception {

                List<Retailer> sortedResponse;
                if (response != null) {
                    sortedResponse = getSortedPerUserPreferences(getSortedAllRetailers(response));
                    fileStorageManager.addList(sortedResponse, new TypeToken<List<Retailer>>() {
                    }.getType());
                    SharedPreferencesUtil.getInstance().setRetailersDataTimestamp(System.currentTimeMillis());
                    retailerList = getAllRetailers();
                } else {
                    // retry to download
                    throw new Exception("Network request faield");
                }

                return sortedResponse;
            }
        });

    }

    /**
     * Get all cached retailers
     *
     * @return list of retaiels
     */
    public List<Retailer> getAllRetailers() {
        return fileStorageManager.getAll(new TypeToken<List<Retailer>>() {
        }.getType());
    }


    /**
     * Put provided brands/retailers list at the begigning of the list
     *
     * @param allRetailers
     * @return
     */
    private List<Retailer> getSortedAllRetailers(List<Retailer> allRetailers) {

        if(CountryUtil.getCountrySelection().getCountryCode() == "fr") {
            List<String> popularBrandsList = Arrays.asList(popularBrands);

            Retailer[] sortedArray = new Retailer[popularBrands.length];
            List<Retailer> otherBrands = new ArrayList<>();

            for (Retailer retailer : allRetailers) {
                if (popularBrandsList.contains(retailer.getName().toLowerCase())) {
                    sortedArray[popularBrandsList.indexOf(retailer.getName().toLowerCase())] = retailer;
                } else {
                    otherBrands.add(retailer);
                }
            }

            int length = popularBrands.length;
            List<Retailer> result = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                if (sortedArray[i] != null) {
                    result.add(sortedArray[i]);
                }
            }

            result.addAll(otherBrands);

            return result;
        }else{
            return allRetailers;
        }
    }

    private List<Retailer> getSortedPerUserPreferences(List<Retailer> allRetailers) {
        List<String> userFavCategories = SuggestedRetailersService.getInstance().getUserCategoryPreference();

        List<Retailer> leadingRetailers = new ArrayList<>();
        Set<String> leadingRetailerIds = new HashSet<>();

        for(String categoryName : userFavCategories){
            Category category = CategoriesService.getInstance().getCategory(categoryName);
            if(category!=null){
                List<Retailer> retailers = getLeadingRetailersForCategory(allRetailers, category.getId(), LEADING_CATEGORY_RETAILERS_COUNT);
                for(Retailer retailer : retailers){
                    if(!leadingRetailerIds.contains(retailer.getId())) {
                        leadingRetailerIds.add(retailer.getId());
                        leadingRetailers.add(retailer);
                    }
                }
            }
        }

        List<Retailer> resultList = new ArrayList<>();

        int count  = allRetailers.size();

        for (int i = count-1; i>=0 ; i--) {
            if (leadingRetailerIds.contains(allRetailers.get(i).getId())) {
                allRetailers.remove(i);
            }
        }

        resultList.addAll(leadingRetailers);
        resultList.addAll(allRetailers);
        return resultList;
    }

    private List<Retailer> getLeadingRetailersForCategory(List<Retailer> allRetailers, String categoryId, int count){
        List<Retailer> resultRetailers = new ArrayList<>();

        for(Retailer retailer : allRetailers){
            if(retailer.getCategoryIds().contains(categoryId)){
                resultRetailers.add(retailer);
            }
            if(resultRetailers.size() == count){
                break;
            }
        }

//        Log.d("goran", " Category " + categoryId + " retailers " + Arrays.toString(resultRetailers.toArray()));
        return resultRetailers;
    }


    public Observable<List<Retailer>> getRetailersData(final List<Retailer> retailers) {

        List<String> retailerIds = new ArrayList<>();
        for (Retailer retailer : retailers) {
            retailerIds.add(retailer.getId());
        }

        final String retailersIdsString = arrayToString(retailerIds);

        if (cachedRetailersData.containsKey(retailersIdsString)) {
            return Observable.create(new ObservableOnSubscribe<List<Retailer>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Retailer>> e) throws Exception {
                    e.onNext(cachedRetailersData.get(retailersIdsString));
                    e.onComplete();
                }
            });
        }

        return RequestHelper.getInstance().getVoucherFeed(retailersIdsString).map(new Function<Map<String, Retailer>, List<Retailer>>() {
            @Override
            public List<Retailer> apply(Map<String, Retailer> retailersMap) throws Exception {
                for (Retailer retailer : retailers) {
                    retailer.setVouchers(retailersMap.get(retailer.getId()).getVouchers());
                }
                cachedRetailersData.put(retailersIdsString, retailers);
                return retailers;
            }
        });

    }

    public Retailer getRetailerById(String retailerId) {
        if (retailerList != null) {
            for (Retailer retailer : retailerList) {
                if (retailer.getId().equals(retailerId)) {
                    return retailer;
                }
            }
        }

        return null;
    }

    public String getRetailerLogo(String retailerId){
        Retailer retailer = getRetailerById(retailerId);
        if(retailer!=null){
            return retailer.getRetailerLogo();
        }
        return null;
    }

    public String getRetailerName(String retailerId){
        Retailer retailer = getRetailerById(retailerId);
        if(retailer!=null){
            return retailer.getName();
        }
        return null;
    }

    public static  Map<String,Integer> getRetailerCategoryCounts(Retailer retailer){
        List<Voucher> vouchers = retailer.getVouchers();
        Map<String,Integer> categoryCounterMap = new HashMap<>();

        if(vouchers!=null && vouchers.size()>0){
            for(Voucher voucher : vouchers){
                String category = voucher.getCategory();

                if(category!=null) {
                    Integer count = categoryCounterMap.get(category);
                    if (count == null) {
                        categoryCounterMap.put(category, new Integer(1));
                    } else {
                        categoryCounterMap.put(category, new Integer(count.intValue() + 1));
                    }
                }
            }
        }
        return  categoryCounterMap;
    }

    @NonNull
    private static String arrayToString(List<String> values) {
        StringBuilder builder = new StringBuilder("");
        int length = values.size();
        int count = 0;
        for (String value : values) {
            builder.append(value);
            if (count + 1 < length) {
                builder.append(",");
            }
            count++;
        }

        return builder.toString();
    }


    private static Map<String, String> useButtonsMerchantIds = new HashMap<String, String>();
    public static class UseButtonId {
        static {
            if(BuildConfig.IS_WL_BUILD) {
                useButtonsMerchantIds.put("hotels.com", "org-3a4aad7eb3f326d0");
                useButtonsMerchantIds.put("groupon", "org-46cb47cf8637e3d6");
            }else{
                useButtonsMerchantIds.put("hoteles.com", "org-3a4aad7eb3f326d0");
            }
        }

        public static String getMerchantId(String merchantName){
            if(merchantName !=null) {
                return useButtonsMerchantIds.get(merchantName.toLowerCase());
            }
            return null;
        }
    }
}
