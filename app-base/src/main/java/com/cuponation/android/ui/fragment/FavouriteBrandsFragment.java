package com.cuponation.android.ui.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by goran on 8/19/16.
 */

public class FavouriteBrandsFragment extends BaseFragment {

    @BindView(R2.id.all_brands)
    TextView allBrandsBtn;

    @BindView(R2.id.my_brands)
    TextView myBrandsBtn;

    @BindView(R2.id.popular)
    TextView popularBtn;

    @BindView(R2.id.pager)
    ViewPager viewPager;

    public static FavouriteBrandsFragment getInstance() {
        return new FavouriteBrandsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fav_retailers, container, false);
        ButterKnife.bind(this, view);

        CouponingApplication.getGATracker().trackScreen(GATracker.SCREEN_NAME_FAVOURITE, null);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setAdapter(new FavouritesFragmentAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setState(BrandsViewState.values()[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setState(BrandsViewState.ALL_BRANDS);

    }

    enum BrandsViewState {
        ALL_BRANDS, MY_BRANDS, POPULAR
    }

    @OnClick(R2.id.my_brands)
    void onMyBrandsClick(View v){
        viewPager.setCurrentItem(BrandsViewState.MY_BRANDS.ordinal());
        setState(BrandsViewState.MY_BRANDS);
    }

    @OnClick(R2.id.all_brands)
    void onAllBrandsClick(View v){
        viewPager.setCurrentItem(BrandsViewState.ALL_BRANDS.ordinal());
        setState(BrandsViewState.ALL_BRANDS);
    }

    @OnClick(R2.id.popular)
    void onPopularBrandsClick(View v){
        viewPager.setCurrentItem(BrandsViewState.POPULAR.ordinal());
        setState(BrandsViewState.POPULAR);
    }

    private void setState(BrandsViewState state){
        Utils.hideKeyboard(getActivity());

        int selectedColor = Color.BLACK;
        int notSelectedColor = ContextCompat.getColor(getContext(), R.color.text_color_dark);

        Typeface normalTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font-regular.ttf");
        Typeface boldTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font-bold.ttf");

        setTextViewStyle(allBrandsBtn, notSelectedColor, normalTypeface);
        setTextViewStyle(myBrandsBtn, notSelectedColor, normalTypeface);
        setTextViewStyle(popularBtn, notSelectedColor, normalTypeface);

        switch (state){
            case ALL_BRANDS:
                setTextViewStyle(allBrandsBtn, selectedColor, boldTypeface);
                break;
            case MY_BRANDS:
                setTextViewStyle(myBrandsBtn, selectedColor, boldTypeface);
                break;
            case POPULAR:
                setTextViewStyle(popularBtn, selectedColor, boldTypeface);
                break;
        }
    }

    private void setTextViewStyle(TextView textView, int color, Typeface typeface){
        textView.setTextColor(color);
        textView.setTypeface(typeface);
    }

    private static class FavouritesFragmentAdapter extends FragmentPagerAdapter {

        public FavouritesFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return AllBrandsFragment.getInstance();
                case 1:
                    return MyBrandsFragment.getInstance();
                case 2:
                    return PopularFragment.getInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
