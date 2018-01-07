package com.cuponation.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.adapter.MyBrandsFragmentAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;

/**
 * Created by goran on 8/22/16.
 */

public class MyBrandsFragment extends BaseFragment {

    private static int ITEMS_PER_PAGE = 6;

    @BindView(R2.id.viewpager_default)
    ViewPager viewpager;

    @BindView(R2.id.indicator_default)
    CircleIndicator indicator;

    @BindView(R2.id.viewpager_popular)
    ViewPager viewpagerPopular;

    @BindView(R2.id.indicator_popular)
    CircleIndicator indicatorPopular;

    private List<Retailer> retailers;
    private MyBrandsFragmentAdapter myBrandsAdapter;

    public static MyBrandsFragment getInstance() {
        return new MyBrandsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_brands, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            updateViews();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMyRetailers();
        initPopularView();
    }

    private void updateViews() {
        reloadMyRetailers();
        initPopularView();
    }

    private void initMyRetailers(){
        retailers = UserInterestService.getInstance().getLikedRetailers();
        myBrandsAdapter = new MyBrandsFragmentAdapter(getChildFragmentManager(), retailers, ITEMS_PER_PAGE,
                GATracker.SCREEN_NAME_FAVOURITE, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopularView();
            }
        });
        viewpager.setAdapter(myBrandsAdapter);
        indicator.setViewPager(viewpager);
    }

    private void reloadMyRetailers() {
        List<Retailer> retailers = UserInterestService.getInstance().getLikedRetailers();
        myBrandsAdapter.setRetailers(retailers);
        myBrandsAdapter.notifyDataSetChanged();
        int count = viewpager.getChildCount();
        for (int position = 0; position < count; position++) {
            GroupedBrandsFragment fragment = (GroupedBrandsFragment) myBrandsAdapter.getRegisteredFragment(position);
            if(fragment!=null) {
                fragment.updateView(myBrandsAdapter.getFragmentRetailers(position));
            }
        }
        indicator.setViewPager(viewpager);
    }

    private void initPopularView() {
        List<Retailer> retailers = RetailerService.getInstance().getAllRetailers();

        if (retailers != null) {
            filterRetailers(retailers);
            viewpagerPopular.setAdapter(new MyBrandsFragmentAdapter(getChildFragmentManager(),
                    retailers.subList(0, 24), ITEMS_PER_PAGE, GATracker.SCREEN_NAME_FAVOURITE, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Retailer> retailers = UserInterestService.getInstance().getLikedRetailers();
                    myBrandsAdapter.setRetailers(retailers);
                    myBrandsAdapter.notifyDataSetChanged();

                    int count = viewpager.getChildCount();
                    for (int position = 0; position < count; position++) {
                        GroupedBrandsFragment fragment = (GroupedBrandsFragment) myBrandsAdapter.getRegisteredFragment(position);
                        if(fragment!=null) {
                            fragment.updateView(myBrandsAdapter.getFragmentRetailers(position));
                        }
                    }
                    indicator.setViewPager(viewpager);
                }
            }));
            indicatorPopular.setViewPager(viewpagerPopular);
        }

    }

    private void filterRetailers(List<Retailer> retailers) {
        UserInterestService userInterestService = UserInterestService.getInstance();

        int size = retailers.size();
        for (int i = size - 1; i >= 0; i--) {
            if (userInterestService.isRetailerLiked(retailers.get(i).getId())) {
                retailers.remove(i);
            }
        }
    }

}
