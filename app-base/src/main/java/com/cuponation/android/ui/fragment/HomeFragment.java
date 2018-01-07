package com.cuponation.android.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.callback.VoucherClickCallback;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.eventbus.BannerAnimationEvent;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.SuggestedRetailersService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.BatchUtil;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.MainActivity;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.activity.VoucherDetailsActivity;
import com.cuponation.android.ui.adapter.MyBrandsGridAdapter;
import com.cuponation.android.ui.view.AddMoreRetailersButton;
import com.cuponation.android.ui.view.BannerView;
import com.cuponation.android.ui.view.BottomBarView;
import com.cuponation.android.ui.view.CategoriesListView;
import com.cuponation.android.ui.view.InterceptableRelativeLayout;
import com.cuponation.android.ui.view.RetailerVouchersView;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.SearchUtility;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.lapism.searchview.SearchView;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.gits.baso.BasoProgressView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import it.sephiroth.android.library.tooltip.Tooltip;

/**
 * Created by goran on 7/25/16.
 */


public class HomeFragment extends BaseFragment {

    public static final int REQUEST_CODE_VOUCHER_DETAILS = 121;

    @BindView(R2.id.voucher_container)
    LinearLayout voucherContainer;

    @BindView(R2.id.searchView)
    SearchView mSearchView;

    @BindView(R2.id.brands_recycler_view)
    RecyclerView recyclerView;

    @BindView(R2.id.code_btn_checkmark)
    ImageView codeCheckmark;

    @BindView(R2.id.deal_btn_checkmark)
    ImageView dealCheckmark;

    @BindView(R2.id.progressview_container)
    BasoProgressView progressView;

    @BindView(R2.id.home_scroll_view)
    NestedScrollView scrollView;

    @BindView(R2.id.root_view)
    InterceptableRelativeLayout rootView;

    private String screenName;

    boolean codeChecked = false;
    boolean dealChecked = false;

    private List<Retailer> aggregateRetailersData;
    private RetailerVouchersView retailerVouchersView;
    private Map<String, RetailerVouchersView> retailerVouchersViewMap = new HashMap<>();

    private BannerView bannerView = null;
    private boolean isBannerVisible = false;
    private boolean isAnimationStarted = false;

    // Tooltips handler and timer
    private long lastTimeTouched = 0;
    private boolean tooltipLooperStarted = false;
    private Handler handler = new Handler();

    // Banner handler and timer
    MyInnerHandler myHandler = new MyInnerHandler(this);
    private long lastTouchOnScreen = 0;

    public static HomeFragment getInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        screenName = GATracker.SCREEN_NAME_HOME;

        List<Retailer> likedRetailers = UserInterestService.getInstance().getLikedRetailers();
        if(likedRetailers!=null) {
            CouponingApplication.getGATracker().trackScreen(screenName, null, GATracker.RETAILER_NUMBER_CUSTOM_DIMENSION, "" +likedRetailers.size());
        }

        final int itemSize = getResources().getDimensionPixelSize(R.dimen.retailer_feed_padding);

        recyclerView.addItemDecoration(
                new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        super.getItemOffsets(outRect, view, parent, state);

                        outRect.set(itemSize, 0, itemSize, 0);
                    }
                }
        );

        progressView.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUI();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (isAnimationStarted) {
                        EventBus.getDefault().post(new BannerAnimationEvent(false));
                        isAnimationStarted = false;
                    } else {
                        if (bannerView != null) {
                            Rect bounds = new Rect();
                            bannerView.getHitRect(bounds);

                            Rect scrollBounds = new Rect();
                            scrollView.getDrawingRect(scrollBounds);

                            isBannerVisible = Rect.intersects(scrollBounds, bounds);
                        }
                        lastTimeTouched = System.currentTimeMillis();
                    }
                }
            });
            startTouchListener();
        }

        if (!SharedPreferencesUtil.getInstance().isOpenVoucherTooltipShown()
                || !SharedPreferencesUtil.getInstance().isBookmarkTooltipShown()) {
            registerRootTouchListener();
        }

        loadUI();

        SuggestedRetailersService.getInstance().getUserCategoryPreference();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPreferencesUtil.getInstance().isUpdateNeededOnHome()) {
            loadUI();
        }
        SearchUtility.setupSearchView(getActivity(), mSearchView);

        // if one of the tooltip is not shown, register root view touch listener
        if (SharedPreferencesUtil.getInstance().isOpenVoucherTooltipShown()
                && !SharedPreferencesUtil.getInstance().isBookmarkTooltipShown()) {
            lastTouchOnScreen = 0;
            startTooltipLooper();
        }
    }

    @Override
    public void onDetach() {
        retailerVouchersViewMap.clear();
        handler.removeCallbacksAndMessages(null);
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VOUCHER_DETAILS && resultCode == Activity.RESULT_OK) {
            String retailerName = data.getStringExtra(Constants.EXTRAS_RETAILER);
            Retailer retailer = RetailerService.getInstance().matchRetailerName(retailerName);
            if (retailer != null) {
                RetailerVouchersView retailerVouchersView = retailerVouchersViewMap.get(retailer.getId());
                if (retailerVouchersView != null) {
                    retailerVouchersView.updateVouchers();
                }
            }
        }
    }

    private void loadUI() {
        // load retailers searches
        loadRetailerSearches();

        RetailerService.getInstance().cacheLocallyAllRetailers(3)
                .compose(this.<List<Retailer>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Retailer>>() {
                    @Override
                    public void accept(List<Retailer> suggestionsList) throws Exception {
                        loadBrandLogos(suggestionsList);
                        SearchUtility.populateSuggestionTable(getActivity(), mSearchView, suggestionsList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (isAdded()) {
                            progressView.stopAndError(getString(R.string.cn_app_loading_error));
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                });
    }

    private void loadRetailerSearches() {
        // Searched retailers for which we should fetch voucher data
        UserInterestService userInterestService = UserInterestService.getInstance();
        List<Retailer> retailers = userInterestService.getLikedRetailers();
        Collections.reverse(retailers);

        // fetch data page by page
        aggregateRetailersData = new ArrayList<>();

        loadRetailerSearchesPage(1, retailers);

    }

    private void loadRetailerSearchesPage(final int page, final List<Retailer> retailers) {
        progressView.startProgress();

        final int allRetailersSize = retailers.size();

        int fromIndex = Math.max(0, (page - 1) * Constants.RETAILERS_ITEMS_PER_PAGE);
        int toIndex = Math.min(page * Constants.RETAILERS_ITEMS_PER_PAGE, allRetailersSize);

        if (fromIndex <= toIndex && fromIndex >= 0 && toIndex <= allRetailersSize && toIndex != 0) {
            RetailerService.getInstance().getRetailersData(retailers.subList(fromIndex, toIndex))
                    .compose(this.<List<Retailer>>bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<Retailer>>() {
                        @Override
                        public void accept(List<Retailer> result) throws Exception {
                            voucherContainer.removeAllViews();

                            progressView.stopAndGone();
                            aggregateRetailersData.addAll(result);
                            if (aggregateRetailersData.size() < allRetailersSize) {
                                loadRetailerSearchesPage((page + 1), retailers);
                            } else {
                                BatchUtil.setInterest(aggregateRetailersData);
                                SuggestedRetailersService.getInstance().addCategoryLiked(aggregateRetailersData);
                                renderRetailerSearches(aggregateRetailersData);
                                startTooltipLooper();
                            }
                        }


                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            if (isAdded()) {
                                progressView.stopAndError(getString(R.string.cn_app_loading_error));
                            }
                        }
                    });

        } else {
            if (toIndex == 0) {
                voucherContainer.removeAllViews();
                progressView.stopAndGone();
            }
            BatchUtil.setInterest(aggregateRetailersData);
            renderRetailerSearches(aggregateRetailersData);
        }
    }

    private void renderRetailerSearches(List<Retailer> retailers) {
        retailerVouchersViewMap.clear();
        SharedPreferencesUtil.getInstance().setUpdateNeededOnHome(false);

        if (retailers == null) {
            return;
        }

        boolean isBannerAdded = false;

        int retailersCount = retailers.size();
        int position = 0;
        for (final Retailer retailer : retailers) {
            position++;
            retailerVouchersView = new RetailerVouchersView(getActivity(), retailer, new VoucherClickCallback() {
                @Override
                public void onVoucherClick(Voucher voucher, View view) {
                    Intent intent = new Intent(CouponingApplication.getContext(), VoucherDetailsActivity.class);
                    intent.putExtra(Constants.EXTRAS_VOUCHER, voucher);
                    intent.putExtra(Constants.EXTRAS_SCREEN_NAME, screenName);

                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(getActivity(), view.findViewById(R.id.voucher_cardview), "cardview");
                    startActivityForResult(intent, REQUEST_CODE_VOUCHER_DETAILS, options.toBundle());
                }

                @Override
                public void onMoreClick(Retailer retailer) {
                    startRetaielerVoucherActivity(retailer);
                }
            }, getVouchersFilter());

            voucherContainer.addView(retailerVouchersView);
            retailerVouchersViewMap.put(retailer.getId(), retailerVouchersView);

            if (!isBannerAdded) {
                isBannerAdded = true;
                View bannerView = getBannerView();
                voucherContainer.addView(bannerView);

            }
            if (retailersCount == 1 || (retailersCount > 1 && position == 2)) {
                View categoriesView = new CategoriesListView(getContext());
                voucherContainer.addView(categoriesView);
            }
        }

        voucherContainer.addView(new AddMoreRetailersButton(getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CouponingApplication.getGATracker().trackAddMoreRetailersWidget();
                ((MainActivity) getActivity()).onItemClick(BottomBarView.State.FAVOURITE_SCREEN, null);
            }
        }));

    }

    private View getBannerView() {
        if (bannerView == null) {
            bannerView = new BannerView(CouponingApplication.getContext(), getChildFragmentManager());
        }
        return bannerView;
    }

    private void startRetaielerVoucherActivity(Retailer retailer) {
        Intent intent = new Intent(getContext(), RetailerVouchersActivity.class);
        intent.putExtra(Constants.EXTRAS_RETAILER_ID, retailer.getId());
        startActivity(intent);
    }

    private void loadBrandLogos(List<Retailer> retailers) {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, OrientationHelper.HORIZONTAL);

        recyclerView.setLayoutManager(layoutManager);

        MyBrandsGridAdapter adapter = new MyBrandsGridAdapter(getActivity(), retailers, screenName, false);

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retailer retailer = (Retailer) v.getTag();
                CouponingApplication.getGATracker().trackRetailerWidget(retailer.getName());
                loadRetailerSearches();
            }
        });
        adapter.setOpenRetailerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retailer retailer = (Retailer) v.getTag();
                CouponingApplication.getGATracker().trackRetailerWidget(retailer.getName());
                startRetaielerVoucherActivity(retailer);
            }
        });
        recyclerView.setAdapter(adapter);

    }


    @OnClick(R2.id.code_btn)
    public void onCodeFilter() {
        codeChecked = !codeChecked;
        if (dealChecked && codeChecked) {
            dealChecked = false;
        }
        setFilter();
    }

    @OnClick(R2.id.deal_btn)
    public void onDealFilter() {
        dealChecked = !dealChecked;
        if (dealChecked && codeChecked) {
            codeChecked = false;
        }
        setFilter();
    }

    private void setFilter() {
        codeCheckmark.setColorFilter(ContextCompat.getColor(getContext(), codeChecked ? R.color.colorPrimary : R.color.text_color_light_gray));
        dealCheckmark.setColorFilter(ContextCompat.getColor(getContext(), dealChecked ? R.color.colorPrimary : R.color.text_color_light_gray));

        voucherContainer.removeAllViews();
        renderRetailerSearches(aggregateRetailersData);
    }

    private String getVouchersFilter() {
        if (codeChecked) {
            // load codes
            return Voucher.TYPE_CODE;
        } else if (dealChecked) {
            // load deals
            return Voucher.TYPE_DEAL;
        } else {
            // load all
            return null;
        }
    }

    private void startTouchListener() {
        if (!isAnimationStarted) {
            myHandler.sendMessageDelayed(new Message(), 500);
        }
    }

    static class MyInnerHandler extends Handler {
        WeakReference<HomeFragment> homeFragment;

        MyInnerHandler(HomeFragment fragment) {
            homeFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message message) {
            HomeFragment fragment = homeFragment.get();
            if (fragment == null) {
                return;
            }
            if (fragment.isBannerVisible && System.currentTimeMillis() - fragment.lastTimeTouched > 2750) {
                EventBus.getDefault().post(new BannerAnimationEvent(true));
                fragment.isAnimationStarted = true;
            } else {
                fragment.startTouchListener();
            }
        }

    }

    private boolean calculateAndShowTooltip(boolean isBookmarkTooltip) {
        int viewCount = voucherContainer.getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View view = voucherContainer.getChildAt(i);
            if (view instanceof RetailerVouchersView) {

                Rect bounds = new Rect();
                Rect parentRect = new Rect();
                voucherContainer.getHitRect(parentRect);
                view.getHitRect(bounds);

                Rect scrollBounds = new Rect(scrollView.getScrollX(), scrollView.getScrollY(),
                        scrollView.getScrollX() + scrollView.getWidth(), scrollView.getScrollY() + scrollView.getHeight());

                int labelHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
                int bookmarkHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());
                int voucherViewTop = (bounds.top + parentRect.top) + labelHeight;

                if (voucherViewTop > scrollBounds.top && voucherViewTop < scrollBounds.bottom
                        && (voucherViewTop + bookmarkHeight) > scrollBounds.top
                        && (voucherViewTop + bookmarkHeight) < scrollBounds.bottom) {

                    try {
                        showTooltip(((RetailerVouchersView) view).getFirstVisibleVoucher(isBookmarkTooltip), isBookmarkTooltip);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("debug", "anchor view was not able to be determinate");
                    }
                }
            }
        }
        return false;
    }

    private void showTooltip(View anchorView, boolean isBookmarkTooltip) {
        Tooltip.TooltipView tooltipView = Tooltip.make(getContext(),
                new Tooltip.Builder(101)
                        .anchor(anchorView, Tooltip.Gravity.TOP)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 10000)
                        .activateDelay(800)
                        .showDelay(0)
                        .text(getResources(), isBookmarkTooltip ? R.string.bookmark_tooltip : R.string.cn_app_home_voucher_tooltip)
                        .withArrow(true)
                        .withOverlay(false)
                        .withStyleId(R.style.ToolTipLayoutCustomStyle)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build());
        tooltipView.show();
        if(isBookmarkTooltip) {
            SharedPreferencesUtil.getInstance().setBookmarkTooltipShown(true);
        }else{
            SharedPreferencesUtil.getInstance().setOpenVoucherTooltipShown(true);
        }
    }

    private void registerRootTouchListener() {
        rootView.setInterceptableTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                lastTouchOnScreen = System.currentTimeMillis();
                return false;
            }
        });
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (lastTouchOnScreen == 0) {
                lastTouchOnScreen = System.currentTimeMillis();
            }
            boolean isLastTouch3SecondsAgo = System.currentTimeMillis() - lastTouchOnScreen > 3 * 1000;
            SharedPreferencesUtil preferencesUtil = SharedPreferencesUtil.getInstance();
            if (!preferencesUtil.isOpenVoucherTooltipShown() && isLastTouch3SecondsAgo) {
                if(preferencesUtil.isFirstVoucherOpened()){
                    SharedPreferencesUtil.getInstance().setOpenVoucherTooltipShown(true);
                    stopTooltipLooper();
                    return;
                }
                if (calculateAndShowTooltip(false)) {
                    stopTooltipLooper();
                    return;
                }
            }

            if (preferencesUtil.isFirstVoucherOpened() &&
                    !preferencesUtil.isBookmarkTooltipShown() && isLastTouch3SecondsAgo) {
                if (calculateAndShowTooltip(true)) {
                    stopTooltipLooper();
                    return;
                }
            }

            if (tooltipLooperStarted) {
                startTooltipLooper();
            }
        }
    };

    public void stopTooltipLooper() {
        tooltipLooperStarted = false;
        handler.removeCallbacks(runnable);
    }

    public void startTooltipLooper() {
        tooltipLooperStarted = true;
        handler.postDelayed(runnable, 1000);
    }

}
