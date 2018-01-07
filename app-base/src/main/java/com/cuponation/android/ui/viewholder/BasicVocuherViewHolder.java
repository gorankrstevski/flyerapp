package com.cuponation.android.ui.viewholder;

import android.view.View;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;

/**
 * Created by goran on 4/11/17.
 */

public class BasicVocuherViewHolder extends VoucherViewHolder {

    @BindView(R2.id.voucher_category)
    public TextView category;

    public BasicVocuherViewHolder(View itemView) {
        super(itemView);
    }
}
