package com.cuponation.android.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.eventbus.RetailerVoucherGoToShopEvent;
import com.cuponation.android.model.eventbus.RetailerVoucherOpenEvent;
import com.cuponation.android.service.local.CategoriesService;
import com.cuponation.android.service.remote.VoucherService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.base.ClickoutActivity;
import com.cuponation.android.ui.adapter.CategoryVoucherListAdapter;
import com.cuponation.android.ui.listener.EndlessRecyclerViewScrollListener;
import com.cuponation.android.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static com.cuponation.android.app.CouponingApplication.getContext;
import static com.cuponation.android.model.Voucher.TYPE_CODE;
import static com.cuponation.android.model.Voucher.TYPE_DEAL;

/**
 * Created by goran on 10/6/16.
 */

public class CategoryVouchersActivity extends ClickoutActivity {

    @BindView(R2.id.recycle_view)
    RecyclerView recyclerView;

    @BindView(R2.id.category_name)
    TextView categoryName;

    @BindView(R2.id.filter_bar)
    View filterBar;

    @BindView(R2.id.code_btn_checkmark)
    ImageView codeCheckmark;

    @BindView(R2.id.deal_btn_checkmark)
    ImageView dealCheckmark;

    boolean codeChecked = false;
    boolean dealChecked = false;

    private String categoryId = "";

    private List<Voucher> vouchers;
    private CategoryVoucherListAdapter adapter;

    private int page = 0;

    private int itemClicked = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_vouchers);

        Intent intent = getIntent();
        categoryId = intent.getStringExtra(Constants.EXTRAS_CATEGORY_ID);
        String name = CategoriesService.getInstance().getCategoryName(categoryId);
        if (name != null) {
            categoryName.setText(name);
        } else {
            finish();
        }

        String voucherType = intent.getStringExtra(Constants.EXTRAS_CATEGORY_VOUCHER_TYPE);
        if(TYPE_CODE.equals(voucherType)){
            codeChecked = true;
        }else if(TYPE_DEAL.equals(voucherType)){
            dealChecked = true;
        }

        initView();

        screenName = GATracker.SCREEN_NAME_CATEGORY;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null && itemClicked < adapter.getItemCount()) {
            adapter.notifyDataSetChanged();
            adapter.notifyItemChanged(itemClicked, 1);
        }
    }

    private void initView() {
        CategoryVouchersActivity.this.vouchers = new ArrayList<>();
        loadVouchers(page);
    }

    private void loadVouchers(final int page) {
        // load Vouchers
        VoucherService.getInstance().getCategoryVouchers(categoryId, page)
                .compose(this.<List<Voucher>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<List<Voucher>>() {
                            @Override
                            public void accept(List<Voucher> vouchers) throws Exception {
                                CategoryVouchersActivity.this.vouchers.addAll(vouchers);

                                int count = countCodes(vouchers);
                                if (count == vouchers.size()) {
                                    filterBar.setVisibility(View.GONE);
                                    onCodeFilter();
                                } else if (count == 0) {
                                    onDealFilter();
                                    filterBar.setVisibility(View.GONE);
                                } else {
                                    filterBar.setVisibility(View.VISIBLE);
                                    setFilter();
                                }
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e("debug", "Category loading failed : " + throwable.getMessage());
                            }
                        }
                );
    }

    private void renderVouchers(List<Voucher> vouchers) {

        if (adapter == null) {
            adapter = new CategoryVoucherListAdapter(vouchers, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    itemClicked = (int) v.getTag(R.integer.tag_position);

                    Intent intent = new Intent(CouponingApplication.getContext(), VoucherDetailsActivity.class);
                    intent.putExtra(Constants.EXTRAS_VOUCHER, (Voucher) v.getTag());
                    intent.putExtra(Constants.EXTRAS_SCREEN_NAME, GATracker.SCREEN_NAME_CATEGORY);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(CategoryVouchersActivity.this, v.findViewById(R.id.voucher_cardview), "cardview");
                        startActivity(intent, options.toBundle());
                    } else {
                        startActivity(intent);
                    }

                }
            });

            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(manager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    loadVouchers(page);
                }
            });
        } else {
            adapter.setVouchers(vouchers);
            adapter.notifyDataSetChanged();
        }
    }

    @OnClick(R2.id.back_btn)
    void onBackButtonClick() {
        super.onBackPressed();
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

        List<Voucher> filteredVouchers = new ArrayList<>();
        if (vouchers != null) {
            for (Voucher voucher : vouchers) {
                if (codeChecked && voucher.isCode()) {
                    filteredVouchers.add(voucher);
                } else if (dealChecked && !voucher.isCode()) {
                    filteredVouchers.add(voucher);
                } else if (!dealChecked && !codeChecked) {
                    filteredVouchers.add(voucher);
                }
            }
        }

        renderVouchers(filteredVouchers);
    }

    private int countCodes(List<Voucher> vouchers) {
        int count = 0;
        for (Voucher voucher : vouchers) {
            if (voucher.isCode()) {
                count++;
            }
        }
        return count;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RetailerVoucherOpenEvent event) {
        Intent intent = new Intent(CouponingApplication.getContext(), VoucherDetailsActivity.class);
        intent.putExtra(Constants.EXTRAS_VOUCHER, event.getVoucher());
        intent.putExtra(Constants.EXTRAS_SCREEN_NAME, GATracker.SCREEN_NAME_CATEGORY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(CategoryVouchersActivity.this, event.getView().findViewById(R.id.voucher_cardview), "cardview");
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RetailerVoucherGoToShopEvent event) {
        performClickout(event.getVoucher());
    }
}
