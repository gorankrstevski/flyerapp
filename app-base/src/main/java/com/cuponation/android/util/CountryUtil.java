package com.cuponation.android.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.cuponation.android.BuildConfig;
import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.CountryConfig;
import com.cuponation.android.network.URLBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by goran on 8/3/16.
 */

public class CountryUtil {

    private static Map<String, String> imageCdnUrls = new HashMap<>();
    private static Map<String, String> countryDomains = new HashMap<>();
    private static Map<String, CountryConfig> bfCountryDomains = new HashMap<>();

    public static final String BASE_URL_STG = "https://dz6fkg3nymt6t.cloudfront.net/";
    public static final String BASE_URL_CENTRAL_PROD = "https://d1kg33vaiunvhp.cloudfront.net/";
    public static final String BASE_URL_WEST_PROD = "https://d1sh4017ov1ixo.cloudfront.net";


    public static final String LEXPRESS_CLIENT_ID = "68c2e8460970236756cad4ad54776831";
    public static final String CUPONATION_ES_CLIENT_ID = "e916c61feaf5e98bc9b770a02f24eefc";
    public static final String CUPONATION_UK_CLIENT_ID = "a6bb77bb88d8192d3780742623fae0f8";

    public static final String SPAIN_COUNTRY_CODE = "es";
    public static final String UK_COUNTRY_CODE = "uk";
    public static final String FRANCE_COUNTRY_CODE = "fr";

    private static String DEFAULT_COUNTRY_CODE = SPAIN_COUNTRY_CODE;



    static {
        imageCdnUrls.put(LEXPRESS_CLIENT_ID, "dd1qg5ikdi0ks.cloudfront.net");
        imageCdnUrls.put(CUPONATION_ES_CLIENT_ID, "dw7w11gpimxcq.cloudfront.net");
        imageCdnUrls.put(CUPONATION_UK_CLIENT_ID, "d2a6g4szxlmkgf.cloudfront.net");
//        imageCdnUrls.put("cuponationfr", "d1a6n8yuy6me0g.cloudfront.net");
//        imageCdnUrls.put("cuponationbr", "d26zal413ep5w4.cloudfront.net");

        countryDomains.put(FRANCE_COUNTRY_CODE, "fr");
        countryDomains.put(SPAIN_COUNTRY_CODE, "es");
        countryDomains.put(UK_COUNTRY_CODE, "co.uk");

        bfCountryDomains.put("es",
                new CountryConfig(SPAIN_COUNTRY_CODE,
                "Spain",
                CUPONATION_ES_CLIENT_ID,
                R.drawable.ic_flag_es,
                "es_",
                BuildConfig.DEBUG ? BASE_URL_STG : BASE_URL_CENTRAL_PROD,
                "blackfriday_app_es"));
        bfCountryDomains.put("gb",
                new CountryConfig(UK_COUNTRY_CODE,
                "UK", CUPONATION_UK_CLIENT_ID,
                R.drawable.ic_flag_uk, "uk_",
                BuildConfig.DEBUG ? BASE_URL_STG : BASE_URL_WEST_PROD,
                "blackfriday_app_uk"));
    }

    public static String getCountryCode() {
        return getCountrySelection().getCountryCode();
    }

    public static String getClientId() {
        return getCountrySelection().getClientId();
    }

    public static String getCountryDomain(){
        return countryDomains.get(getCountrySelection().getCountryCode());
    }

    public static String getCountryBaseUrl(){
        return getCountrySelection().getBaseUrl();
    }

    public static Map<String, CountryConfig> getBFAppCountries(){
        return bfCountryDomains;
    }

    public static String getClickoutDeviceValue(){
        return BuildConfig.IS_WL_BUILD ? Constants.DEVICE_SOURCE_LEXPRESS_VALUE : Constants.DEVICE_SOURCE_BLACKFRIDAY_VALUE;
    }

    public static String getCdnImageUrl(String path) {
        if (path == null) {
            return "";
        }

        String url = imageCdnUrls.get(getCountrySelection().getClientId());

        if (url != null) {
            return "https://" + url + "/images/" + path.toLowerCase().charAt(0) + "/" + path;
        } else {
            return URLBuilder.BACKEND_URL + CountryUtil.getCountryDomain() + "/images/" + path.toLowerCase().charAt(0) + "/" + path;
        }

    }

    public static void initializeCountrySelection(){
        getCountrySelection();
    }

    public static CountryConfig getCountrySelection() {
        if(BuildConfig.IS_WL_BUILD){
            // file prefix must be empty for compatibility
            return new CountryConfig(FRANCE_COUNTRY_CODE,
                    "France", LEXPRESS_CLIENT_ID,
                    -1,
                    "",
                    BuildConfig.DEBUG ? BASE_URL_STG : BASE_URL_CENTRAL_PROD,
                    "lexpress_app");
        }
        String selection = SharedPreferencesUtil.getInstance().getCountrySelection();
        if (selection == null) {
            String countryCode = detectCountryCode();
            CountryConfig countryConfig = bfCountryDomains.get(countryCode);
            if(countryConfig != null){
                SharedPreferencesUtil.getInstance().setCountrySelection(countryCode);
                return countryConfig;
            }else{
                SharedPreferencesUtil.getInstance().setCountrySelection(DEFAULT_COUNTRY_CODE);
                return bfCountryDomains.get(DEFAULT_COUNTRY_CODE);
            }
        }else{
            return bfCountryDomains.get(selection);
        }
    }

    private static String detectCountryCode() {
        Context context = CouponingApplication.getContext();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String countryCode = telephonyManager.getNetworkCountryIso();
        if (!TextUtils.isEmpty(countryCode) && bfCountryDomains.containsKey(countryCode.toLowerCase())) {
                return countryCode.toLowerCase();
        }

        countryCode = telephonyManager.getSimCountryIso();
        if (!TextUtils.isEmpty(countryCode) && bfCountryDomains.containsKey(countryCode.toLowerCase())) {
            return countryCode.toLowerCase();
        }


        countryCode = context.getResources().getConfiguration().locale.getCountry();
        if (!TextUtils.isEmpty(countryCode)&& bfCountryDomains.containsKey(countryCode.toLowerCase())) {
            return countryCode.toLowerCase();
        }

        return DEFAULT_COUNTRY_CODE;
    }

}
