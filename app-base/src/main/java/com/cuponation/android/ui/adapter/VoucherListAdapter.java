package com.cuponation.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Category;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.service.local.CategoriesService;
import com.cuponation.android.ui.activity.CategoryVouchersActivity;
import com.cuponation.android.ui.viewholder.VoucherListCategoryView;
import com.cuponation.android.ui.viewholder.VoucherListHeaderHolder;
import com.cuponation.android.ui.viewholder.VoucherListViewHolder;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.CountryUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.cuponation.android.model.Voucher.TYPE_CODE;

/**
 * Created by goran on 9/6/16.
 */

public class VoucherListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int CATEGORY_TYPE = 2;
    private int ITEM_TYPE = 1;
    private int HEADER_TYPE = 0;

    private final List<VoucherFull> vouchers;
    private View.OnClickListener clickListener;
    private String retailerLogo;
    private String screenName;
    private BookmarkVoucherService bookmarkVoucherService;
    private Context context;

    public VoucherListAdapter(Context context, List<VoucherFull> vouchers, View.OnClickListener clickListener, String retailerLogo, String screenName) {
        this.context = context;
        this.vouchers = vouchers;
        this.clickListener = clickListener;
        this.retailerLogo = retailerLogo;
        this.screenName = screenName;
        this.bookmarkVoucherService = BookmarkVoucherService.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        if(viewType == ITEM_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.item_list_voucher, viewGroup, false);
            return new VoucherListViewHolder(view);
        }else if(viewType == CATEGORY_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_retailer_category, viewGroup, false);
            return new VoucherListCategoryView(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_retailer_header, viewGroup, false);
            return new VoucherListHeaderHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof  VoucherListViewHolder) {
            final VoucherFull voucher = vouchers.get(position);

            final VoucherListViewHolder viewHolder = (VoucherListViewHolder) holder;

            viewHolder.caption1.setText(voucher.getCaption1());
            viewHolder.caption2.setText(voucher.getCaption2());

            if(voucher.getImage()!=null) {
                viewHolder.voucherImage.setVisibility(View.VISIBLE);
                Picasso.with(context).load(CountryUtil.getCdnImageUrl(voucher.getImage()))
                        .noFade()
                        .fit().centerCrop()
                        .into(viewHolder.voucherImage);
                viewHolder.caption1.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                viewHolder.caption2.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                viewHolder.captionHolder.setBackgroundResource(R.drawable.voucher_image_dropshadow);
            }else{
                viewHolder.caption1.setTextColor(ContextCompat.getColor(context, R.color.text_color_dark));
                viewHolder.caption2.setTextColor(ContextCompat.getColor(context, R.color.text_color_dark));
                viewHolder.captionHolder.setBackgroundDrawable(null);
                viewHolder.voucherImage.setVisibility(View.GONE);
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
                    CouponingApplication.getGATracker().trackBookmark(voucher, !isVoucherBookmarked, screenName);

                }
            });

            viewHolder.voucherSwipeView.setAdapter(new VoucherSwipeAdapter(context, voucher, viewHolder.itemView, false));

            viewHolder.itemView.setTag(voucher);
            viewHolder.itemView.setTag(R.integer.tag_position, position);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(v);
                }
            });
        }else if(holder instanceof VoucherListHeaderHolder){
            int size = context.getResources().getDimensionPixelSize(R.dimen.product_image_size);
            Picasso.with(context).load(retailerLogo)
                    .resize(size, size)
                    .centerInside().into(((VoucherListHeaderHolder)holder).retailerLogo);
        }else if(holder instanceof VoucherListCategoryView){
            final Voucher voucher = vouchers.get(position);
            List<Category> categories = CategoriesService.getInstance().getCategories();
            for(final Category category : categories){
                if(category.getName().equals(voucher.getCategory())){
                    ((VoucherListCategoryView) holder).categoryText.setText(context.getString(R.string.cn_app_top_code_category) + category.getName());
                    ((VoucherListCategoryView) holder).categoryLogo.setText(category.getLogo());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CouponingApplication.getGATracker().trackCategoryBanner(voucher.getRetailerName(), voucher.getCategory());
                            Intent activityIntent = new Intent(context, CategoryVouchersActivity.class);
                            activityIntent.putExtra(Constants.EXTRAS_CATEGORY_ID, category.getId());
                            activityIntent.putExtra(Constants.EXTRAS_CATEGORY_VOUCHER_TYPE, TYPE_CODE);
                            context.startActivity(activityIntent);
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return vouchers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return HEADER_TYPE;
        }else if(vouchers.get(position).getVoucherId() == null){
            return CATEGORY_TYPE;
        }else{
            return ITEM_TYPE;
        }
    }

    private void setVoucherState(VoucherListViewHolder viewHolder, Voucher voucher){
        if (bookmarkVoucherService.isVoucherBookmarked(voucher)) {
            viewHolder.bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24_fill);
        } else {
            viewHolder.bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24);
        }
    }
}
