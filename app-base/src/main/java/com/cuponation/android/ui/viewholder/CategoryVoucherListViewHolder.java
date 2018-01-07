package com.cuponation.android.ui.viewholder;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 9/6/16.
 */

public class CategoryVoucherListViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.voucher_type)
    public TextView voucherType;
    @BindView(R2.id.retailer_logo)
    public ImageView retailerLogo;
    @BindView(R2.id.voucher_pager)
    public ViewPager voucherSwipeView;
    @BindView(R2.id.voucher_bookmark)
    public ImageView bookmarkVoucher;

    public CategoryVoucherListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


}
