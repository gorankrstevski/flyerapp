package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.like.LikeButton;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 8/19/16.
 */

public class RetailerListViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.retailer_title)
    public TextView text;
    @BindView(R2.id.retailer_fav)
    public LikeButton fav;

    public RetailerListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}