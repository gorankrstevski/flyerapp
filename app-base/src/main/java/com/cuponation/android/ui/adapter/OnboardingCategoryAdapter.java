package com.cuponation.android.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.model.Category;
import com.cuponation.android.ui.viewholder.CategoryViewHolder;
import com.cuponation.android.ui.viewholder.OnboardCategoryViewHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by goran on 2/10/17.
 */

public class OnboardingCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Category> categories;
    private View.OnClickListener clickListener;

    private Set<Integer> selectedPositions;

    public OnboardingCategoryAdapter(List<Category> categories) {
        this.categories = categories;
        this.selectedPositions = new HashSet<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_onboard_category, viewGroup, false);
        return new OnboardCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final Category category = categories.get(position);
        final OnboardCategoryViewHolder viewHolder = (OnboardCategoryViewHolder) holder;
        viewHolder.title.setText(category.getName());

        holder.itemView.setTag(position);
        holder.itemView.setTag(R.integer.tag_id, category.getId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (selectedPositions.contains(position)) {
                    setSelectedState(viewHolder,false);
                    selectedPositions.remove(position);
                } else {
                    setSelectedState(viewHolder,true);
                    selectedPositions.add(position);
                }

                if (clickListener != null) {
                    clickListener.onClick(v);
                }
            }
        });

        setSelectedState(viewHolder, selectedPositions.contains(position));

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private void setSelectedState(OnboardCategoryViewHolder viewHolder, boolean isSelected){
        if(isSelected) {
            viewHolder.itemView.setBackgroundResource(R.drawable.category_onboard_selected_bg);
            viewHolder.title.setTextColor(Color.WHITE);
            viewHolder.clearSelection.setVisibility(View.VISIBLE);
        }else{
            viewHolder.itemView.setBackgroundResource(R.drawable.category_onboard_bg);
            viewHolder.title.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorPrimary));
            viewHolder.clearSelection.setVisibility(View.GONE);
        }
    }
}
