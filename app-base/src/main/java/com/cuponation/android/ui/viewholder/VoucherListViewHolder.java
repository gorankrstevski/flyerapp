package com.cuponation.android.ui.viewholder;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 9/6/16.
 */

public class VoucherListViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.caption1)
    public TextView caption1;
    @BindView(R2.id.caption2)
    public TextView caption2;
    @BindView(R2.id.voucher_pager)
    public ViewPager voucherSwipeView;
    @BindView(R2.id.voucher_bookmark)
    public ImageView bookmarkVoucher;
    @BindView(R2.id.voucher_image)
    public ImageView voucherImage;
    @BindView(R2.id.caption_holder)
    public ViewGroup captionHolder;


    public VoucherListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


}
