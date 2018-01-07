package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 6/30/17.
 */

public class VoucherListCategoryView extends RecyclerView.ViewHolder {

    @BindView(R2.id.category_logo)
    public TextView categoryLogo;

    @BindView(R2.id.category_text)
    public TextView categoryText;

    public VoucherListCategoryView(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
