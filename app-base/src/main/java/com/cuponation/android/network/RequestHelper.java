package com.cuponation.android.network;

import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.UniqueCode;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.network.retrofit.MediaMetrieAPI;
import com.cuponation.android.network.retrofit.RetrofitHelper;
import com.cuponation.android.network.retrofit.VouchersApi;
import com.cuponation.android.util.CountryUtil;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;


/**
 * Created by goran on 7/12/16.
 */

public class RequestHelper {

    private static RequestHelper me = null;

    private static VouchersApi vouchersApi;

    private RequestHelper() {
    }

    public static RequestHelper getInstance() {

        if (me == null) {
            me = new RequestHelper();
            vouchersApi = CouponingApplication.getRetrofit().create(VouchersApi.class);
        }

        return me;
    }

    public Observable<Map<String, Retailer>> getVoucherFeed(String retailerId) {
        String url = URLBuilder.buildUrlForPath(URLBuilder.URLPath.URL_PATH_RETAILER_FULL_INFO);
        url = appendClientUID(url);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_RETAILER, retailerId);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_LIMIT, URLBuilder.LIMIT_ITEMS);

        return vouchersApi.getVoucherSeed(url);
    }

    public Observable<List<VoucherFull>> getRetailerVocuhers(String retailerId) {
        String url = URLBuilder.buildUrlForPath(URLBuilder.URLPath.URL_PATH_RETAILER_VOUCHERS);
        url = appendClientUID(url);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_ID_RETAILER, retailerId);
        return vouchersApi.getRetailerVouchers(url);
    }

    public Observable<List<Retailer>> getAllRetailers() {
        String url = URLBuilder.buildUrlForPath(URLBuilder.URLPath.URL_PATH_RETAILER_SHORT_INFO);
        url = appendClientUID(url);

        return vouchersApi.getAllRetailers(url);
    }

    public Observable<Map<String, List<Voucher>>> getCategoryVouchers(String categoryId, int page) {
        String url = URLBuilder.buildUrlForPath(URLBuilder.URLPath.URL_PATH_CATEGORY_VOUCHERS);
        url = appendClientUID(url);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_CATEGORY, categoryId);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_LIMIT, page * URLBuilder.CATEGORY_VOUCHER_COUNT + "," + URLBuilder.CATEGORY_VOUCHER_COUNT);

        return vouchersApi.getCategoryVouchers(url);
    }

    public Observable<UniqueCode> getUniqueCodes(String voucherId, String userId) {
        String url = URLBuilder.buildUrlForPath(URLBuilder.URLPath.URL_PATH_UNIQUE_CODES);
        url = appendClientUID(url);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_ID_VOUCHER, voucherId);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_CUSTOMER_KEY, userId);

        return vouchersApi.getUniqueCode(url);
    }


    public Observable<VoucherFull> getVoucher(String voucherId) {
        String url = URLBuilder.buildUrlForPath(URLBuilder.URLPath.URL_PATH_VOUCHERS);
        url = appendClientUID(url);
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_ID_VOUCHER, voucherId);

        return vouchersApi.getVoucher(url);
    }

    private String appendClientUID(String url) {
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_CLIENT_ID, CountryUtil.getClientId());
        url = URLBuilder.appendParameters(url, URLBuilder.HTTPConstants.KEY_COUNTRY, CountryUtil.getCountryCode());
        return url;
    }

    public Observable<String> callMediaMetrie() {
        String url = URLBuilder.MEDIAMETRIE_URL;
        MediaMetrieAPI mediaMetrieAPI = RetrofitHelper.getSimpleRetrofitAdapter(CouponingApplication.getContext()).create(MediaMetrieAPI.class);
        return mediaMetrieAPI.getMediaMetrie(url);
    }
}
