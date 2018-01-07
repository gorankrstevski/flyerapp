package com.cuponation.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.viewholder.MyBrandViewHolder;
import com.cuponation.android.util.CircleTransform;
import com.cuponation.android.util.Utils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goran on 8/22/16.
 */

public class MyBrandsGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Retailer> retailers;
    private boolean undoRemoval;
    private boolean openRetailerOnClick = true;
    private View.OnClickListener onClickListener;
    private View.OnClickListener openRetailerClickListener;
    private UserInterestService userInterestService;
    private String screenName;

    private Context context;
    private GATracker gaTracker;

    private boolean showNotificationOnChange = true;

    private Map<String, Retailer> pendingRemoval;
    private Map<String, Integer> pendingRemovalPosition;

    public MyBrandsGridAdapter(Context context, List<Retailer> retailers, String screenName, boolean undoRemoval) {
        this.context = context;
        this.retailers = retailers;
        this.undoRemoval = undoRemoval;
        this.userInterestService = UserInterestService.getInstance();
        this.screenName = screenName;
        this.gaTracker = CouponingApplication.getGATracker();

        pendingRemoval = new HashMap<>();
        pendingRemovalPosition = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_my_brands, viewGroup, false);
        return new MyBrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Retailer retailer = retailers.get(position);

        ImageView imageView = ((MyBrandViewHolder) holder).logo;

        int size = context.getResources().getDimensionPixelSize(R.dimen.brands_image_size);
        Picasso.with(context)
                .load(retailer.getRetailerLogo())
                .resize(size, 0)
                .transform(new CircleTransform())
                .into(imageView);


        ((MyBrandViewHolder) holder).fav.setTag(retailer);
        ((MyBrandViewHolder) holder).fav.setTag(R.integer.tag_position, position);
        ((MyBrandViewHolder) holder).fav.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton v) {
                Retailer retailer = (Retailer) v.getTag();
                v.setTag(R.integer.tag_is_remove, false);

                if(!userInterestService.isRetailerLiked(retailer.getId())) {
                    if(showNotificationOnChange){
                        Utils.showFeedback(holder.itemView.getContext(), R.string.cn_app_retailer_added);
                    }
                    userInterestService.addLikedRetailer((Retailer) v.getTag());
                    gaTracker.trackFavoriteRetailer(retailer, true, screenName);
                }

                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
            }

            @Override
            public void unLiked(LikeButton v) {
                Retailer retailer = (Retailer) v.getTag();
                v.setTag(R.integer.tag_is_remove, false);
                if(undoRemoval){
                    addToPendingRemove(retailer, (Integer) v.getTag(R.integer.tag_position));
                }else{
                    if (userInterestService.isRetailerLiked(retailer.getId())) {
                        if(showNotificationOnChange) {
                            Utils.showFeedback(holder.itemView.getContext(), R.string.cn_app_retailer_removed);
                        }
                        userInterestService.removeFromLikedRetailers((Retailer) v.getTag());
                        gaTracker.trackFavoriteRetailer(retailer, false, screenName);
                    }
                }
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }

            }
        });

        ((MyBrandViewHolder) holder).itemView.setTag(retailer);
        ((MyBrandViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(openRetailerOnClick) {
                    if (openRetailerClickListener != null) {
                        openRetailerClickListener.onClick(v);
                    }
                }else{
                    ((MyBrandViewHolder) holder).fav.performClick();
                }
            }
        });
        ((MyBrandViewHolder) holder).fav.setLiked(userInterestService.isRetailerLiked(retailer.getId()));

    }

    @Override
    public int getItemCount() {
        return retailers != null ? retailers.size() : 0;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    public void setOpenRetailerClickListener(View.OnClickListener openRetailerClickListener) {
        this.openRetailerClickListener = openRetailerClickListener;
    }

    public void setRetailers(List<Retailer> retailers) {
        this.retailers = retailers;
    }

    public void setShowNotificationOnChange(boolean showNotificationOnChange) {
        this.showNotificationOnChange = showNotificationOnChange;
    }

    public void setOpenRetailerOnClick(boolean openRetailerOnClick) {
        this.openRetailerOnClick = openRetailerOnClick;
    }


    private void addToPendingRemove(Retailer retailer, int position) {
        retailers.remove(retailer);
        pendingRemoval.put(retailer.getId(), retailer);
        pendingRemovalPosition.put(retailer.getId(), position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, retailers.size());
        userInterestService.removeFromLikedRetailers(retailer);

    }

    public void undoRemoval(String retailerId) {
        Retailer retailer = pendingRemoval.remove(retailerId);
        Integer position = pendingRemovalPosition.remove(retailerId);
        userInterestService.addLikedRetailer(retailer);
        if (retailer != null) {
            int size = retailers.size();
            int insertPosition = position > size ? size : position;
            retailers.add(insertPosition, retailer);
            notifyItemInserted(insertPosition);
            if (position.intValue() == 0) {
                notifyDataSetChanged();
            }
            notifyItemRangeChanged(position, retailers.size());
        }
    }

    public void processPendingToRemove(String retailerId) {
        if (pendingRemoval.containsKey(retailerId)) {
            //userInterestService.removeFromLikedRetailers(RetailerService.getInstance().getRetailerById(retailerId));
            pendingRemoval.remove(retailerId);
        }
    }

}
