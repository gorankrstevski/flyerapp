package com.cuponation.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.model.Category;
import com.cuponation.android.tracking.BatchUtil;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.CategoryVouchersActivity;
import com.cuponation.android.ui.viewholder.CategoryViewHolder;
import com.cuponation.android.util.Constants;

import java.util.List;

/**
 * Created by goran on 2/10/17.
 */

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Category> categories;

    public CategoryAdapter(List<Category> categories){
        this.categories = categories;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_category, viewGroup, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final Context context = holder.itemView.getContext();

        final Category category = categories.get(position);
        CategoryViewHolder viewHolder = (CategoryViewHolder) holder;
        viewHolder.title.setText(category.getName());
        viewHolder.logo.setText(category.getLogo());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GATracker.getInstance(context).trackCategoryWidget(category.getName());
                BatchUtil.setInterest(category.getName());
                Intent activityIntent = new Intent(context, CategoryVouchersActivity.class);
                activityIntent.putExtra(Constants.EXTRAS_CATEGORY_ID, category.getId());
                context.startActivity(activityIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
