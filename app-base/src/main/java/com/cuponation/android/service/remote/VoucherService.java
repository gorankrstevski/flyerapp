package com.cuponation.android.service.remote;


import android.support.annotation.NonNull;

import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.UniqueCode;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.network.RequestHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Function;


/**
 * Created by goran on 7/8/16.
 */

public class VoucherService {

    private static VoucherService me;

    private HashMap<String, VoucherFull> vouchers;

    private VoucherService(){
        vouchers = new HashMap<>();
    }

    public static VoucherService getInstance(){
        if(me == null){
            me = new VoucherService();
        }

        return me;
    }

    public static final Voucher NOT_FOUND = new Voucher();

    public Observable<Map<String, Retailer>> getMatchingVouchers(@NonNull final Retailer retailer) {
        return RequestHelper.getInstance().getVoucherFeed(retailer.getId());
    }

    public Observable<List<Voucher>> getCategoryVouchers(String categoryId, int page) {

        return RequestHelper.getInstance().getCategoryVouchers(categoryId, page).map(
                new Function<Map<String, List<Voucher>>, List<Voucher>>() {
                    @Override
                    public List<Voucher> apply(Map<String, List<Voucher>> vouchersListMap) throws Exception {
                        List<Voucher> vouchers = new ArrayList<>();

                        Set<String> retailerIds = vouchersListMap.keySet();
                        for (String retailerId : retailerIds) {
                                for (Voucher voucher : vouchersListMap.get(retailerId)) {
                                    vouchers.add(voucher);
                                }
                        }

                        return vouchers;
                    }
                }
        );
    }

    public Observable<UniqueCode> getUniqueCode(String voucherId, String userId) {
        return RequestHelper.getInstance().getUniqueCodes(voucherId, userId);
    }

    public Observable<VoucherFull> getVoucherFull(final String voucherId){

        if(vouchers.containsKey(voucherId)){
             return Observable.just(vouchers.get(voucherId));
        }

        return RequestHelper.getInstance().getVoucher(voucherId).map(new Function<VoucherFull, VoucherFull>() {
            @Override
            public VoucherFull apply(VoucherFull voucherFull) throws Exception {
                vouchers.put(voucherFull.getVoucherId(), voucherFull);
                return voucherFull;
            }
        });
    }

}
