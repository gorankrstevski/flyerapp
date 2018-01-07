package com.cuponation.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.callback.VoucherClickCallback;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.adapter.VoucherHorizontalListAdapter;
import com.cuponation.android.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by goran on 7/14/16.
 */

public class RetailerVouchersView extends RelativeLayout {

    private TextView retailerName;
    private TextView moreBtn;
    private View rootView;
    private RecyclerView voucherRecyclerView;
    private Retailer retailer;
    private VoucherClickCallback voucherClickCallback;
    private String voucherTypeFilter;


    public RetailerVouchersView(Context context, Retailer retailer, VoucherClickCallback voucherClickCallback, String voucherTypeFilter) {
        super(context);
        this.retailer = retailer;
        this.voucherClickCallback = voucherClickCallback;
        this.voucherTypeFilter = voucherTypeFilter;
        init(context);
    }

    public RetailerVouchersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RetailerVouchersView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RetailerVouchersView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.retailer_vouchers_view, this);
        retailerName = (TextView) rootView.findViewById(R.id.retailer_title);
        moreBtn = (TextView) rootView.findViewById(R.id.more_btn);
        moreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                voucherClickCallback.onMoreClick(retailer);
            }
        });

        if (retailer.getName() != null) {
            retailerName.setText(retailer.getName().toUpperCase());
        }
        voucherRecyclerView = (RecyclerView) rootView.findViewById(R.id.voucher_recycler_view);
        setVouchers(retailer);
    }

    private void setVouchers(Retailer retailer) {

        List<Voucher> vouchers;
        if (voucherTypeFilter != null) {
            vouchers = new ArrayList<>();
            for (Voucher voucher : retailer.getVouchers()) {
                if (voucher.getType().toLowerCase().equals(voucherTypeFilter.toLowerCase())) {
                    vouchers.add(voucher);
                }
            }
        } else {
            vouchers = retailer.getVouchers();
            if (vouchers == null ) {
                vouchers = new ArrayList<>();
            }
        }

        Collections.sort(vouchers, Utils.getVoucherComparator());

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, OrientationHelper.HORIZONTAL);

        voucherRecyclerView.setLayoutManager(layoutManager);

        VoucherHorizontalListAdapter voucherHorizontalListAdapter = new VoucherHorizontalListAdapter(getContext(), vouchers, voucherClickCallback, false, GATracker.SCREEN_NAME_HOME, false, false);
        voucherRecyclerView.setAdapter(voucherHorizontalListAdapter);
    }

    public View getFirstVisibleVoucher(boolean shouldReturnBookmark){
        int[] positions = new int[2];
        positions = ((StaggeredGridLayoutManager)voucherRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPositions(positions);
        return shouldReturnBookmark ? voucherRecyclerView.getChildAt(positions[0]).findViewById(R.id.voucher_bookmark) : voucherRecyclerView.getChildAt(positions[0]);

    }

    public void updateVouchers(){
        voucherRecyclerView.getAdapter().notifyItemRangeChanged(0, retailer.getVouchers().size());
    }
}
