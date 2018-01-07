package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.like.LikeButton;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 8/22/16.
 */

public class MyBrandViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.retailer_logo)
    public ImageView logo;
    @BindView(R2.id.retailer_fav)
    public LikeButton fav;

    public MyBrandViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}