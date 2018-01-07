package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 2/3/17.
 */

public class ShareOptionViewHolder  extends RecyclerView.ViewHolder{

    @BindView(R2.id.title)
    public TextView title;

    @BindView(R2.id.logo)
    public ImageView logo;

    public ShareOptionViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
