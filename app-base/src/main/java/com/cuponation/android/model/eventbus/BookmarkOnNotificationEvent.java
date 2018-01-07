package com.cuponation.android.model.eventbus;

import com.cuponation.android.model.Voucher;

/**
 * Created by goran on 3/23/17.
 */

public class BookmarkOnNotificationEvent {

    private Voucher voucher;

    public BookmarkOnNotificationEvent(Voucher voucher){
        this.voucher = voucher;
    }

    public Voucher getVoucher() {
        return voucher;
    }
}
