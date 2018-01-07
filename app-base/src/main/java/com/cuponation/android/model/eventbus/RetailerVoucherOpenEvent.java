package com.cuponation.android.model.eventbus;

import android.view.View;

import com.cuponation.android.model.Voucher;

/**
 * Created by goran on 3/16/17.
 */

public class RetailerVoucherOpenEvent {

    private Voucher voucher;
    private View view;

    public RetailerVoucherOpenEvent(Voucher voucher, View view){
        this.voucher = voucher;
        this.view = view;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public View getView() {
        return view;
    }
}
