package com.cuponation.android.network.retrofit;

import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.UniqueCode;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by goran on 2/22/17.
 */

public interface VouchersApi {

    @GET
    Observable<Map<String, Retailer>> getVoucherSeed(@Url String url);

    @GET
    Observable<List<VoucherFull>> getRetailerVouchers(@Url String url);

    @GET
    Observable<List<Retailer>> getAllRetailers(@Url String url);

    @GET
    Observable<Map<String, List<Voucher>>> getCategoryVouchers(@Url String url);

    @GET
    Observable<UniqueCode> getUniqueCode(@Url String url);

    @GET
    Observable<VoucherFull> getVoucher(@Url String url);
}
