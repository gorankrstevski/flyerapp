package com.cuponation.android.network;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.cuponation.android.BuildConfig;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.CountryUtil;

/**
 * Created by goran on 7/12/16.
 */

public class URLBuilder {


    public static final int NUMBER_OF_PRODUCTS = 38;
    public static final String LIMIT_ITEMS = "0," + Constants.RETAILERS_ITEMS_PER_PAGE;

    public static final String BACKEND_URL = "https://backend.cuponation.";
    public static final String IMAGE_RESIZE_URL = "http://m.img.cuponation.com/unsafe/%sx%s/%s";

    public static final String MEDIAMETRIE_URL = "http://static.lexpress.fr/mnr/xpr_android_smartphone.xml";

    public static final int CATEGORY_VOUCHER_COUNT = 25;

    public static String appendParameters(@NonNull String url, @NonNull String key, @NonNull String value) {
        Uri uri = Uri.parse(url);
        return uri.buildUpon().appendQueryParameter(key, value).toString();
    }

    @NonNull
    public static String buildUrlForPath(@NonNull String path) {
        Uri uri = Uri.parse(CountryUtil.getCountryBaseUrl());
        uri = Uri.withAppendedPath(uri, path);
        return uri.toString();
    }

    public static String appendPageParameters(@NonNull String url, int page, int numberOfItems) {
        Uri uri = Uri.parse(url);
        return uri.buildUpon().appendQueryParameter("rows", "" + (page * numberOfItems) + "," + numberOfItems).toString();
    }

    public static String appendPageParameters(@NonNull String url, int page) {
        Uri uri = Uri.parse(url);
        return uri.buildUpon().appendQueryParameter("rows", "" + (page * NUMBER_OF_PRODUCTS) + "," + NUMBER_OF_PRODUCTS).toString();
    }

    public static class URLPath {
        public static final String URL_PATH_CLICKOUT = "/clickout/out/id/";

        public static final String URL_PATH_RETAILER_SHORT_INFO = "retailers";
        public static final String URL_PATH_RETAILER_FULL_INFO = "retailers/full";
        public static final String URL_PATH_RETAILER_VOUCHERS = "retailers/vouchers";
        public static final String URL_PATH_CATEGORY_VOUCHERS = "retailers/category";
        public static final String URL_PATH_UNIQUE_CODES = "get-voucher-code";

        public static final String URL_PATH_VOUCHERS = "vouchers";

    }

    public static class HTTPConstants {
        public static final String KEY_DEVICE_SOURCE = "device_source";
        public static final String KEY_CLIENT_ID = "clientId";
        public static final String KEY_COUNTRY = "country";
        public static final String KEY_RETAILER = "retailer";
        public static final String KEY_CATEGORY = "category";
        public static final String KEY_LIMIT = "limit";
        public static final String KEY_ID_VOUCHER = "idVoucher";
        public static final String KEY_ID_RETAILER = "idRetailer";
        public static final String KEY_CUSTOMER_KEY = "customerKey";

    }
}
