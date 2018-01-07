package com.cuponation.android.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.callback.VoucherClickCallback;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.MainActivity;
import com.cuponation.android.ui.activity.VoucherDetailsActivity;
import com.cuponation.android.ui.adapter.VoucherHorizontalListAdapter;
import com.cuponation.android.ui.view.BottomBarView;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by goran on 3/3/17.
 */

public class BookmarksFragment extends BaseFragment {

    public static BookmarksFragment getInstance() {
        return new BookmarksFragment();
    }

    @BindView(R2.id.bookmarks_recyclerview)
    RecyclerView recyclerView;

    @BindView(R2.id.toolbar)
    Toolbar toolbar;

    @BindView(R2.id.no_bookmarks)
    ViewGroup noBookmarksView;

    @BindView(R2.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private VoucherHorizontalListAdapter voucherHorizontalListAdapter;
    private RecyclerView.ItemDecoration itemDecoration;

    private String showVoucherId = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        ButterKnife.bind(this, view);

        toolbar.setTitle(R.string.cn_app_favorites);
        toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), R.color.text_color_dark));

        CouponingApplication.getGATracker().trackScreen(GATracker.SCREEN_NAME_BOOKMARKS, null);

        if (getArguments() != null) {
            showVoucherId = getArguments().getString(Constants.EXTRAS_VOUCHER_ID);
        }

        loadVouchers(getResources().getConfiguration());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (voucherHorizontalListAdapter != null) {
            voucherHorizontalListAdapter.swap(BookmarkVoucherService.getInstance().getAllSavedVouchers(true));
        }
        showNoBookmarksIfNeeded();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadVouchers(newConfig);
    }

    private void loadVouchers(Configuration configuration) {
        final int itemsInRow = Utils.getItemCount(getActivity(), R.dimen.voucher_item_width);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(itemsInRow, OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        if (itemDecoration != null) {
            recyclerView.removeItemDecoration(itemDecoration);
        }

        itemDecoration = Utils.getItemDecoration(getActivity(), R.dimen.voucher_item_width, R.dimen.voucher_item_margin, itemsInRow);
        recyclerView.addItemDecoration(itemDecoration);

        List<Voucher> vouchers = BookmarkVoucherService.getInstance().getAllSavedVouchers(true);
        noBookmarksView.setVisibility(View.GONE);
        voucherHorizontalListAdapter = new VoucherHorizontalListAdapter(getActivity(), vouchers, new VoucherClickCallback() {
            @Override
            public void onVoucherClick(Voucher voucher, View view) {
                Intent intent = new Intent(CouponingApplication.getContext(), VoucherDetailsActivity.class);
                intent.putExtra(Constants.EXTRAS_VOUCHER, voucher);
                intent.putExtra(Constants.EXTRAS_SCREEN_NAME, GATracker.SCREEN_NAME_BOOKMARKS);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), view.findViewById(R.id.voucher_cardview), "cardview");
                startActivity(intent, options.toBundle());
            }

            @Override
            public void onMoreClick(Retailer retailer) {

            }
        }, true, GATracker.SCREEN_NAME_BOOKMARKS, true, true);

        voucherHorizontalListAdapter.setOnBookmarkClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoBookmarksIfNeeded();
                showUndoSnackbar((Voucher) view.getTag());
            }
        });
        recyclerView.setAdapter(voucherHorizontalListAdapter);

        if(showVoucherId != null){
            int position = getVoucherPosition(vouchers, showVoucherId);
            if(position >= 0){
                layoutManager.scrollToPosition(position);
            }
        }

        showNoBookmarksIfNeeded();
    }

    @OnClick(R2.id.no_bookmarks_button)
    void onGoToHomeScreenClick() {
        ((MainActivity) getActivity()).onItemClick(BottomBarView.State.HOME_SCREEN, null);
    }

    private void showNoBookmarksIfNeeded() {
        if (BookmarkVoucherService.getInstance().getAllSavedVouchers(true).size() <= 0) {
            noBookmarksView.setVisibility(View.VISIBLE);
        }
    }

    private void showUndoSnackbar(final Voucher voucher) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, voucher.isCode() ? R.string.cn_app_code_removed : R.string.cn_app_deal_removed, Snackbar.LENGTH_LONG)
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        voucherHorizontalListAdapter.processPendingToRemove(voucher.getVoucherId());
                        showNoBookmarksIfNeeded();
                    }
                })
                .setAction(R.string.cn_app_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        voucherHorizontalListAdapter.undoRemoval(voucher.getVoucherId());
                    }
                });

        snackbar.show();
    }

    private int getVoucherPosition(List<Voucher> vouchers, String voucherId){
        for(int i=0;i<vouchers.size(); i++){
            if(vouchers.get(i).getVoucherId().equals(voucherId)){
                return i;
            }
        }

        return -1;
    }
}
