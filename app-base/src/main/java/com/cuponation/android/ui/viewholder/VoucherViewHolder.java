package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.ui.view.AutoResizeTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 10/3/16.
 */

public class VoucherViewHolder extends RecyclerView.ViewHolder {


    @BindView(R2.id.voucher_text)
    public TextView title;

    @BindView(R2.id.voucher_date)
    public TextView endDate;

    @BindView(R2.id.voucher_status)
    public TextView status;

    @BindView(R2.id.voucher_type)
    public TextView type;

    @BindView(R2.id.voucher_checkmark)
    public ImageView checkmark;

    @BindView(R2.id.voucher_bookmark)
    public ImageView bookmarkVoucher;

    @BindView(R2.id.new_tag)
    public AutoResizeTextView newTag;

    public VoucherViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
