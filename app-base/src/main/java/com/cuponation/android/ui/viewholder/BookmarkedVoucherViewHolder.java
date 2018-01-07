package com.cuponation.android.ui.viewholder;

import android.view.View;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;

/**
 * Created by goran on 10/3/16.
 */

public class BookmarkedVoucherViewHolder extends VoucherViewHolder {

    @BindView(R2.id.retailer_logo)
    public ImageView logo;

    public BookmarkedVoucherViewHolder(View itemView) {
        super(itemView);
    }
}
