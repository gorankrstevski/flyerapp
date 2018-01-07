package com.cuponation.android.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.model.eventbus.RetailerVoucherGoToShopEvent;
import com.cuponation.android.model.eventbus.RetailerVoucherOpenEvent;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.network.RequestHelper;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.SuggestedRetailersService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.AdjustTracker;
import com.cuponation.android.tracking.BatchUtil;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.activity.VoucherDetailsActivity;
import com.cuponation.android.ui.adapter.VoucherListAdapter;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.NotificationsUtil;
import com.cuponation.android.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by goran on 9/6/16.
 */

public class RetailerVouchersFragment extends BaseFragment {

    private static final int CATEGORY_BANNER_POSITION = 8;
    private static final int MIN_CATEGORY_BANNER_POSITION = 4;

    @BindView(R2.id.fav_btn)
    ImageView favBtn;

    @BindView(R2.id.recycle_view)
    RecyclerView recyclerView;

    @BindView(R2.id.elevation_dropshadow)
    View elevationDropShadow;

    private String retailerId;
    private Retailer retailer = null;
    private int codesCount = 0;
    private Notification.NotificationType notificationType = null;
    private String originalUrl;
    private boolean isFromShare = false;

    private UserInterestService userInterestService;
    private int itemClicked;

    public static RetailerVouchersFragment getInstance() {
        return new RetailerVouchersFragment();
    }

    private boolean fragmentDetached = false;

    private String screenName;
    private GATracker gaTracker;

    private VoucherListAdapter voucherListAdapter;

    private boolean shouldTrackBackButtonTap = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            retailer = getArguments().getParcelable(Constants.EXTRAS_RETAILER);
        }
        if (retailer == null && getArguments() != null) {
            retailerId = getArguments().getString(Constants.EXTRAS_RETAILER_ID);
            retailer = RetailerService.getInstance().getRetailerById(retailerId);
            codesCount = getArguments().getInt(Constants.EXTRAS_RETAILER_VOUCHERS, 0);
            notificationType = NotificationsUtil.getNotificationType(getArguments());
            originalUrl = getArguments().getString(Constants.EXTRAS_URL_KEY);
            isFromShare = getArguments().getBoolean(Constants.EXTRAS_IS_FROM_SHARE, false);
            if (notificationType != null) {
                shouldTrackBackButtonTap = true;
            }

        }

        userInterestService = UserInterestService.getInstance();
        gaTracker = CouponingApplication.getGATracker();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_retailer, container, false);
        ButterKnife.bind(this, view);

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // Do something for lollipop and above versions
            elevationDropShadow.setVisibility(View.VISIBLE);
        }

        initView();
        loadRetailerVouchers();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (voucherListAdapter != null && itemClicked < voucherListAdapter.getItemCount()) {
            voucherListAdapter.notifyDataSetChanged();
            voucherListAdapter.notifyItemChanged(itemClicked, 1);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDetach() {
        if (shouldTrackBackButtonTap) {
            gaTracker.trackPostNotificationAction(notificationType, codesCount, GATracker.ACTION_LEAVE_APP);
        }
        fragmentDetached = true;
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentDetached = false;
    }

    private void initView() {

        if (userInterestService.isRetailerLiked(retailerId)) {
            favBtn.setImageResource(R.drawable.ic_fav);
        }

        LinearLayoutManager manager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

    }

    @OnClick(R2.id.back_btn)
    void onBackButtonClick() {
        trackBackButtonClick();
        getActivity().onBackPressed();
    }

    public void trackBackButtonClick() {
        if (shouldTrackBackButtonTap) {
            shouldTrackBackButtonTap = false;
            gaTracker.trackPostNotificationAction(notificationType, codesCount, GATracker.ACTION_BACK_BUTTON);
        }
    }

    @OnClick(R2.id.fav_btn)
    void onFavBtnClick() {

        if (retailer == null) {
            return;
        }

        if (userInterestService.isRetailerLiked(retailerId)) {
            Utils.showFeedback(getActivity(), R.string.cn_app_retailer_removed);
            userInterestService.removeFromLikedRetailers(retailer);
            gaTracker.trackFavoriteRetailer(retailer, false, screenName);
            favBtn.setImageResource(R.drawable.ic_fav_empty);
        } else {
            Utils.showFeedback(getActivity(), R.string.cn_app_retailer_added);
            userInterestService.addLikedRetailer(retailer);
            gaTracker.trackFavoriteRetailer(retailer, false, screenName);
            favBtn.setImageResource(R.drawable.ic_fav);
        }

    }

    private List<VoucherFull> retailerVouchers;
    private void loadRetailerVouchers() {

        RequestHelper.getInstance().getRetailerVocuhers(retailerId)
                .compose(this.<List<VoucherFull>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<List<VoucherFull>>() {
                            @Override
                            public void accept(List<VoucherFull> retailerVouchers) throws Exception {
                                RetailerVouchersFragment.this.retailerVouchers = retailerVouchers;

                                codesCount = 0;
                                for(Voucher voucher : retailerVouchers){
                                    if(voucher.isCode()){
                                        codesCount++;
                                    }
                                }
                                loadVouchers();
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
//                                throwable.printStackTrace();
                                Log.e("debug", "Error in loading retaielr vouchers ");
                            }
                        }
                );
    }


    private void loadVouchers() {

        if (retailer == null) {
            getActivity().finish();
            return;
        } else if (fragmentDetached) {
            return;
        }

        UserInterestService.getInstance().addVisitedRetailer(retailer);
        SuggestedRetailersService.getInstance().addCategoryVisits(retailerVouchers);

        screenName = GATracker.SCREEN_NAME_RETAILER + retailer.getName();

        gaTracker.trackScreen(GATracker.SCREEN_NAME_RETAILER, retailer.getName());
        BatchUtil.trackVisitedBrandEvent(retailer.getName(), null);

        if (notificationType != null) {
            switch (notificationType) {
                case APP_NOTIF:
                    gaTracker.trackSeeMore(retailer.getName(),
                            codesCount > 1 ? GATracker.SEEMORE_METHOD_NOTIFICATION_CN_MULTIPLE
                                    : GATracker.SEEMORE_METHOD_NOTIFICATION_CN);
                    break;
                case BATCH_AUTO_NOTIF:
                case BATCH_MANUAL_NOTIF:
                    gaTracker.trackSeeMore(retailer.getName(),
                            codesCount > 1 ? GATracker.SEEMORE_METHOD_NOTIFICATION_BATCH_MULTIPLE
                                    : GATracker.SEEMORE_METHOD_NOTIFICATION_BATCH);
                    break;
            }
        }

        Collections.sort(retailerVouchers, Utils.getVoucherComparator());

        // empty voucher for header
        retailerVouchers.add(0, new VoucherFull());

        // cateogry item
        Map categoryCounts = RetailerService.getRetailerCategoryCounts(retailer);
        categoryCounts = Utils.sortByValue(categoryCounts);

        int i = 0;
        for (String category : new ArrayList<>((Set<String>) categoryCounts.keySet())) {
            VoucherFull categoryVoucher = new VoucherFull();
            categoryVoucher.setCategory(category);

            int firstPosition = codesCount >= CATEGORY_BANNER_POSITION || codesCount == 0 ? CATEGORY_BANNER_POSITION : codesCount + 1;

            if (firstPosition < MIN_CATEGORY_BANNER_POSITION) {
                firstPosition = MIN_CATEGORY_BANNER_POSITION;
            }

            if(firstPosition > retailerVouchers.size()){
                firstPosition = retailerVouchers.size();
            }

            if (i == 0) {
                retailerVouchers.add(firstPosition, categoryVoucher);
            } else {
                retailerVouchers.add(firstPosition + i * CATEGORY_BANNER_POSITION, categoryVoucher);
            }

            i++;
            if ((firstPosition + i * CATEGORY_BANNER_POSITION) > retailerVouchers.size()) {
                break;
            }
        }

        voucherListAdapter = new VoucherListAdapter(getActivity(), retailerVouchers, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClicked = (int) v.getTag(R.integer.tag_position);
                Voucher voucher = (Voucher) v.getTag();
                openVoucherDetails(voucher, v);
            }
        }, retailer.getRetailerLogo(), GATracker.SCREEN_NAME_RETAILERS);
        recyclerView.setAdapter(voucherListAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RetailerVoucherOpenEvent event) {
        openVoucherDetails(event.getVoucher(), event.getView());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RetailerVoucherGoToShopEvent event) {
        Voucher clickoutVoucher = event.getVoucher();
        clickoutVoucher.setRetailerId(retailer.getId());
        AdjustTracker.trackViewContent(clickoutVoucher.getVoucherId());
        ((RetailerVouchersActivity) getActivity()).performClickout(clickoutVoucher);
    }

    private void openVoucherDetails(Voucher voucher, View view) {
        shouldTrackBackButtonTap = false;

        Intent intent = new Intent(getActivity(), VoucherDetailsActivity.class);
        intent.putExtra(Constants.EXTRAS_VOUCHER, voucher);
        intent.putExtra(Constants.EXTRAS_IS_FROM_SHARE, isFromShare);
        if (originalUrl != null) {
            intent.putExtra(Constants.EXTRAS_URL_KEY, originalUrl);
        }
        intent.putExtra(Constants.EXTRAS_SCREEN_NAME, GATracker.SCREEN_NAME_RETAILER);
        if (view != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), view.findViewById(R.id.voucher_cardview), "cardview");
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}
