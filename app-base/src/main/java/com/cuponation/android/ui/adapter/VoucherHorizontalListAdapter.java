package com.cuponation.android.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.callback.VoucherClickCallback;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.eventbus.BookmarkAnimationEvent;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.service.local.VoucherLocalService;
import com.cuponation.android.ui.viewholder.BasicVocuherViewHolder;
import com.cuponation.android.ui.viewholder.BookmarkedVoucherViewHolder;
import com.cuponation.android.ui.viewholder.VoucherViewHolder;
import com.cuponation.android.util.AnimationUtil;
import com.cuponation.android.util.TimeUtil;
import com.cuponation.android.util.Utils;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goran on 10/3/16.
 */

public class VoucherHorizontalListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final boolean showOnlyExpireDates;
    private boolean removeItems = false;
    private List<Voucher> vouchers;
    VoucherClickCallback voucherClickCallback;
    private VoucherLocalService voucherLocalService;

    private BookmarkVoucherService bookmarkVoucherService;
    private Map<String, Voucher> pendingRemoval;
    private Map<String, Integer> pendingRemovalPosition;
    private String screenName;
    private boolean showRetailerLogo;

    private View.OnClickListener bookmarkClickListener;

    private long lastCheckTime;

    private Context context;
    public VoucherHorizontalListAdapter(Context context, List<Voucher> vouchers,
                                        VoucherClickCallback voucherClickCallback,
                                        boolean removeItems,
                                        String screenName,
                                        boolean showRetailerLogo,
                                        boolean showOnlyExpireDates){
        this.context = context;
        this.vouchers = vouchers;
        this.voucherClickCallback = voucherClickCallback;
        this.removeItems = removeItems;
        this.screenName = screenName;
        this.showRetailerLogo = showRetailerLogo;
        this.showOnlyExpireDates = showOnlyExpireDates;

        bookmarkVoucherService = BookmarkVoucherService.getInstance();
        voucherLocalService = VoucherLocalService.getInstance();
        pendingRemoval = new HashMap<>();
        pendingRemovalPosition = new HashMap<>();
        lastCheckTime = voucherLocalService.getLastSetupCheck();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if(showRetailerLogo){
            view = LayoutInflater.from(context).inflate(R.layout.item_bookmarked_voucher, viewGroup, false);
            return new BookmarkedVoucherViewHolder(view);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.item_voucher, viewGroup, false);
            return new BasicVocuherViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Voucher voucher = vouchers.get(position);

        final VoucherViewHolder viewHolder = (VoucherViewHolder) holder;

        if(!showRetailerLogo) {
            ((BasicVocuherViewHolder)viewHolder).category.setText(voucher.getCategory());
        }else{
            int size = context.getResources().getDimensionPixelSize(R.dimen.brands_image_size);

            ImageView logo = ((BookmarkedVoucherViewHolder)viewHolder).logo;
            Picasso.with(context)
                    .load(voucher.getRetailerLogoUrl())
                    .resize(size, size)
                    .centerInside()
                    .into(logo);
        }


        if(voucherLocalService.isVoucherNew(voucher, lastCheckTime)){
            viewHolder.newTag.setVisibility(View.VISIBLE);
        }else {
            viewHolder.newTag.setVisibility(View.GONE);
        }

        if (voucher.getVerified() == null || showOnlyExpireDates) {
            viewHolder.endDate.setVisibility(View.VISIBLE);
            if (voucher.getEndDate() != null) {
                viewHolder.endDate.setText(TimeUtil.getExpireTime(voucher.getEndDate(), context));
            }
            viewHolder.status.setVisibility(View.INVISIBLE);
            viewHolder.checkmark.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.endDate.setVisibility(View.GONE);
            viewHolder.status.setVisibility(View.VISIBLE);
            viewHolder.checkmark.setVisibility(View.VISIBLE);
        }
        viewHolder.title.setText(voucher.getShortTitle());
        viewHolder.type.setText(voucher.isCode() ? context.getString(R.string.cn_app_code) : context.getString(R.string.cn_app_deal));
        if (Voucher.TYPE_DEAL.toLowerCase().equals(voucher.getType().toLowerCase())) {
            viewHolder.type.setBackgroundResource(R.drawable.deal_bg);
            viewHolder.type.setTextColor(ContextCompat.getColor(context, R.color.deal_badge_text_color));
            viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.deal_text_color));
        } else {
            viewHolder.type.setBackgroundResource(R.drawable.code_bg);
            viewHolder.type.setTextColor(ContextCompat.getColor(context, R.color.code_badge_text_color));
            viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.code_text_color));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voucherClickCallback.onVoucherClick(voucher, v);
            }
        });

        if (bookmarkVoucherService.isVoucherBookmarked(voucher)) {
            viewHolder.bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24_fill);
        } else {
            viewHolder.bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24);
        }
        viewHolder.bookmarkVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookmarkClickListener != null) {
                    v.setTag(voucher);
                    bookmarkClickListener.onClick(v);
                }

                boolean isVoucherAdded;
                if (bookmarkVoucherService.isVoucherBookmarked(voucher)) {
                    isVoucherAdded = false;
                    if (!removeItems) {
                        BookmarkVoucherService.getInstance().removeVoucherFromSaved(voucher);
                    }
                    ((ImageView) v).setImageResource(R.drawable.ic_bookmark_24);
                } else {
                    isVoucherAdded = true;
                    BookmarkVoucherService.getInstance().addVoucherToSaved(voucher);
                    ((ImageView) v).setImageResource(R.drawable.ic_bookmark_24_fill);
                }

                if (removeItems && !isVoucherAdded) {
                    addToPendingRemove(voucher, position);
                }

                AnimationSet animation = AnimationUtil.getGrowAndBounceAnimation();
                v.startAnimation(animation);
                if (isVoucherAdded) {
                    Bitmap bitmap = Utils.loadBitmapFromVoucherView(voucher, viewHolder.itemView.getContext(),
                            R.layout.item_voucher,
                            viewHolder.itemView.getWidth(),
                            viewHolder.itemView.getHeight());
                    EventBus.getDefault().post(new BookmarkAnimationEvent(bitmap));
                }
                CouponingApplication.getGATracker().trackBookmark(voucher, isVoucherAdded, screenName);

            }
        });

    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }


    public void swap(List<Voucher> vouchers) {
        this.vouchers.clear();
        this.vouchers.addAll(vouchers);
        notifyDataSetChanged();
    }

    public void setOnBookmarkClickListener(View.OnClickListener bookmarkClickListener) {
        this.bookmarkClickListener = bookmarkClickListener;
    }


    private void addToPendingRemove(Voucher voucher, int position) {
        vouchers.remove(voucher);
        pendingRemoval.put(voucher.getVoucherId(), voucher);
        pendingRemovalPosition.put(voucher.getVoucherId(), position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, vouchers.size());

    }

    public void undoRemoval(String voucherId) {
        Voucher voucher = pendingRemoval.remove(voucherId);
        Integer position = pendingRemovalPosition.remove(voucherId);
        if (voucher != null) {
            int size = vouchers.size();
            int insertPosition = position > size ? size : position;
            vouchers.add(insertPosition, voucher);
            notifyItemInserted(insertPosition);
            if (position.intValue() == 0) {
                notifyDataSetChanged();
            }
            notifyItemRangeChanged(position, vouchers.size());
        }
    }

    public void processPendingToRemove(String voucherId) {
        if (pendingRemoval.containsKey(voucherId)) {
            BookmarkVoucherService.getInstance().removeVoucherFromSaved(pendingRemoval.get(voucherId));
            pendingRemoval.remove(voucherId);
        }
    }
}
