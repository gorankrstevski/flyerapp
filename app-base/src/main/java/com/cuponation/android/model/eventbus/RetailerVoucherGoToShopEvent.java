package com.cuponation.android.model.eventbus;

import com.cuponation.android.model.Voucher;

/**
 * Created by goran on 3/16/17.
 */

public class RetailerVoucherGoToShopEvent {

    private Voucher voucher;

    public RetailerVoucherGoToShopEvent(Voucher voucher){
        this.voucher = voucher;
    }

    public Voucher getVoucher() {
        return voucher;
    }
}
