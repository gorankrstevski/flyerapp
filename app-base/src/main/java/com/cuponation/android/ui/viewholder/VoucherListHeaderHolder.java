package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 4/26/17.
 */

public class VoucherListHeaderHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.retailer_logo)
    public ImageView retailerLogo;

    public VoucherListHeaderHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
