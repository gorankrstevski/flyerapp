package com.cuponation.android.callback;

import android.view.View;

import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;

/**
 * Created by goran on 7/20/16.
 */

public interface VoucherClickCallback {

    void onVoucherClick(Voucher voucher, View view);
    void onMoreClick(Retailer retailer);
}
