package com.cuponation.android.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.model.Retailer;
import com.cuponation.android.ui.fragment.GroupedBrandsFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 8/25/16.
 */

public class MyBrandsFragmentAdapter extends FragmentStatePagerAdapter {

    private static int itemsPerPage = 6;

    private List<Retailer> retailers;
    private View.OnClickListener clickListener;
    private String screenName;
    SparseArray<WeakReference<Fragment>> registeredFragments = new SparseArray<>();

    public MyBrandsFragmentAdapter(FragmentManager fm, List<Retailer> retailers, int itemsPerPage,
                                   String screenName, View.OnClickListener clickListener) {
        super(fm);
        this.retailers = retailers;
        this.itemsPerPage = itemsPerPage;
        this.clickListener = clickListener;
        this.screenName = screenName;
    }

    @Override
    public Fragment getItem(int position) {
        List<Retailer> groupRetailers = getFragmentRetailers(position);
        GroupedBrandsFragment groupedBrandsFragment = GroupedBrandsFragment.newInstance(groupRetailers, screenName);
        groupedBrandsFragment.setClickListener(clickListener);
        return groupedBrandsFragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        if (registeredFragments != null && registeredFragments.get(position) != null) {
            return registeredFragments.get(position).get();
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {

        if (retailers.size() % itemsPerPage == 0) {
            return retailers.size() / itemsPerPage;
        } else {
            return retailers.size() / itemsPerPage + 1;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public void setRetailers(List<Retailer> retailers) {
        this.retailers = retailers;
    }

    public List<Retailer> getFragmentRetailers(int position) {
        if (retailers.size() > 0) {
            int start = position * itemsPerPage;
            int end = Math.min((position + 1) * itemsPerPage, retailers.size());
            if (start <= end) {
                return retailers.subList(start, end);
            }
        }
        return new ArrayList<>();

    }
}