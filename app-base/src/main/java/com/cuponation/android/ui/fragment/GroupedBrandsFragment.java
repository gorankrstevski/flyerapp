package com.cuponation.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.adapter.MyBrandsGridAdapter;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 8/25/16.
 */

public final class GroupedBrandsFragment extends Fragment {

    private ArrayList<Retailer> retailers;
    private View.OnClickListener clickListener;

    private RecyclerView recyclerView;
    private MyBrandsGridAdapter adapter;
    private String screenName;

    public static GroupedBrandsFragment newInstance(List<Retailer> retailers, String screenName) {
        GroupedBrandsFragment groupedBrandsFragment = new GroupedBrandsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.EXTRAS_RETAILERS, new ArrayList<>(retailers));
        args.putString(Constants.EXTRAS_SCREEN_NAME, screenName);
        groupedBrandsFragment.setArguments(args);
        return groupedBrandsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retailers = getArguments().getParcelableArrayList(Constants.EXTRAS_RETAILERS);
        screenName = getArguments().getString(Constants.EXTRAS_SCREEN_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.my_brands_page, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_view);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);

        adapter = new MyBrandsGridAdapter(getActivity(), retailers, screenName, false);
        adapter.setOpenRetailerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RetailerVouchersActivity.class);
                intent.putExtra(Constants.EXTRAS_RETAILER_ID, ((Retailer)v.getTag()).getId());
                startActivity(intent);
            }
        });
        adapter.setOnClickListener(clickListener);

        RecyclerView.ItemDecoration itemDecoration = Utils.getItemDecoration(getActivity(), R.dimen.brands_image_size,-1, 3);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void updateView(List<Retailer> retailers){
            adapter.setRetailers(retailers);
            adapter.notifyDataSetChanged();
    }
}
