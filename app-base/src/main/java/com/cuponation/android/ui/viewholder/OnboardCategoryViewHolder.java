package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 2/10/17.
 */

public class OnboardCategoryViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.category_title)
    public TextView title;
    @BindView(R2.id.category_clear_selection)
    public TextView clearSelection;

    public OnboardCategoryViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }


}
