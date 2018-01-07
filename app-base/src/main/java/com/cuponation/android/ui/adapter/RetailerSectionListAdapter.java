package com.cuponation.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.viewholder.RetailerListViewHolder;
import com.cuponation.android.util.Utils;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.List;
import java.util.Map;

/**
 * Created by goran on 8/19/16.
 */

public class RetailerSectionListAdapter extends SectionedRecyclerViewAdapter<RetailerListViewHolder> {

    private Map<String, List<Retailer>> retailerSections;
    private List<String> sectionLabels;
    private View.OnClickListener heartClickListener;
    private View.OnClickListener itemClickListener;

    private UserInterestService userInterestService;

    private boolean showNotificationOnChange = true;

    private GATracker gaTracker;
    private String screenName;
    private boolean openRetailerOnClick = true;

    public RetailerSectionListAdapter(Map<String, List<Retailer>> retailerSections, List<String> sectionLabels, String screenName){
        this.retailerSections = retailerSections;
        this.sectionLabels = sectionLabels;
        this.userInterestService = UserInterestService.getInstance();
        this.screenName = screenName;
        this.gaTracker = CouponingApplication.getGATracker();
    }

    @Override
    public int getSectionCount() {
        return retailerSections.size();
    }

    @Override
    public int getItemCount(int section) {
        if(sectionLabels.get(section)!=null && retailerSections.get(sectionLabels.get(section))!=null){
            return retailerSections.get(sectionLabels.get(section)).size();
        }else{
            return 0;
        }
    }

    @Override
    public void onBindHeaderViewHolder(RetailerListViewHolder holder, int section) {
        holder.text.setText(sectionLabels.get(section));
    }

    @Override
    public void onBindViewHolder(final RetailerListViewHolder holder, int section, int relativePosition, int absolutePosition) {

        final Retailer retailer = retailerSections.get(sectionLabels.get(section)).get(relativePosition);

        holder.text.setText(retailer.getName());
        holder.fav.setTag(retailer);

        holder.fav.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton v) {
                if(!userInterestService.isRetailerLiked(retailer.getId())) {
                    if (showNotificationOnChange) {
                        Utils.showFeedback(holder.itemView.getContext(), R.string.cn_app_retailer_added);
                    }
                    userInterestService.addLikedRetailer((Retailer) v.getTag());
                    gaTracker.trackFavoriteRetailer(retailer, true, screenName);
                }
                if(heartClickListener != null){
                    heartClickListener.onClick(v);
                }
            }

            @Override
            public void unLiked(LikeButton v) {
                if(userInterestService.isRetailerLiked(retailer.getId())){
                    if(showNotificationOnChange) {
                        Utils.showFeedback(holder.itemView.getContext(), R.string.cn_app_retailer_removed);
                    }
                    userInterestService.removeFromLikedRetailers((Retailer) v.getTag());
                    gaTracker.trackFavoriteRetailer(retailer, false, screenName);
                }
                if(heartClickListener != null){
                    heartClickListener.onClick(v);
                }
            }
        });

        holder.fav.setLiked(userInterestService.isRetailerLiked(retailer.getId()));
        holder.itemView.setTag(retailer);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(openRetailerOnClick) {
                    if(itemClickListener!=null) {
                        itemClickListener.onClick(v);
                    }
                }else{
                    holder.fav.performClick();
                }
            }
        });
    }

    @Override
    public RetailerListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layout;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                layout = R.layout.item_retailer_section_header;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.item_retailer;
                break;
            default:
                layout = R.layout.item_retailer;
                break;
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new RetailerListViewHolder(v);

    }

    public void setOnHeartClickListener(View.OnClickListener heartClickListener){
        this.heartClickListener = heartClickListener;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public void setShowNotificationOnChange(boolean showNotificationOnChange) {
        this.showNotificationOnChange = showNotificationOnChange;
    }

    public void setOpenRetailerOnClick(boolean openRetailerOnClick) {
        this.openRetailerOnClick = openRetailerOnClick;
    }
}
