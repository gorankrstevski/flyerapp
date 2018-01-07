package com.cuponation.android.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.viewholder.CategoryVoucherListViewHolder;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by goran on 9/6/16.
 */

public class CategoryVoucherListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final BookmarkVoucherService bookmarkVoucherService;
    private List<Voucher> vouchers;
    private View.OnClickListener clickListener;

    public CategoryVoucherListAdapter(List<Voucher> vouchers, View.OnClickListener clickListener) {
        this.vouchers = vouchers;
        this.clickListener = clickListener;
        this.bookmarkVoucherService = BookmarkVoucherService.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_category_voucher, viewGroup, false);
        return new CategoryVoucherListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        final Voucher voucher = vouchers.get(position);

        final CategoryVoucherListViewHolder viewHolder = (CategoryVoucherListViewHolder) holder;

        ImageView imageView = viewHolder.retailerLogo;

        int size = context.getResources().getDimensionPixelSize(R.dimen.brands_image_size);

        Picasso.with(context)
                .load(voucher.getRetailerLogoUrl())
                .resize(size, size)
                .centerInside()
                .into(imageView);

        viewHolder.voucherType.setText(voucher.isCode() ? context.getString(R.string.cn_app_code) : context.getString(R.string.cn_app_deal));

        viewHolder.voucherSwipeView.setAdapter(new VoucherSwipeAdapter(context, voucher, viewHolder.itemView, true));

        if (Voucher.TYPE_DEAL.toLowerCase().equals(voucher.getType())) {
            viewHolder.voucherType.setBackgroundResource(R.drawable.deal_bg);
            viewHolder.voucherType.setTextColor(ContextCompat.getColor(context, R.color.deal_badge_text_color));

        } else {
            viewHolder.voucherType.setBackgroundResource(R.drawable.code_bg);
            viewHolder.voucherType.setTextColor(ContextCompat.getColor(context, R.color.code_badge_text_color));
        }

        setVoucherState(viewHolder, voucher);

        viewHolder.bookmarkVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isVoucherBookmarked = bookmarkVoucherService.isVoucherBookmarked(voucher);
                if(isVoucherBookmarked){
                    bookmarkVoucherService.removeVoucherFromSaved(voucher);
                }else{
                    bookmarkVoucherService.addVoucherToSaved(voucher);
                }
                setVoucherState(viewHolder, voucher);
                CouponingApplication.getGATracker().trackBookmark(voucher, !isVoucherBookmarked, GATracker.SCREEN_NAME_CATEGORY);
            }
        });

        viewHolder.itemView.setTag(voucher);
        viewHolder.itemView.setTag(R.integer.tag_position, position);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    public void setVouchers(List<Voucher> vouchers){
        this.vouchers = vouchers;
    }

    private void setVoucherState(CategoryVoucherListViewHolder viewHolder, Voucher voucher){
        if (bookmarkVoucherService.isVoucherBookmarked(voucher)) {
            viewHolder.bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24_fill);
        } else {
            viewHolder.bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24);
        }
    }
}
