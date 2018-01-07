package com.cuponation.android.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cuponation.android.ui.fragment.BannerFragment;

/**
 * Created by goran on 12/12/16.
 */

public class BannerFragmentAdapter extends FragmentStatePagerAdapter {

    private final int BANNER_ITEMS = 3;

    public BannerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return BannerFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return BANNER_ITEMS;
    }

}
