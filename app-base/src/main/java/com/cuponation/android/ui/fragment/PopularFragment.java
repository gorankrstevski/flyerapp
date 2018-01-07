package com.cuponation.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.adapter.MyBrandsGridAdapter;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by goran on 8/22/16.
 */

public class PopularFragment extends BaseFragment {

    @BindView(R2.id.recycle_view)
    RecyclerView recyclerView;

    private List<Retailer> retailers;
    private MyBrandsGridAdapter adapter;

    public static PopularFragment getInstance() {
        return new PopularFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popular, container, false);
        ButterKnife.bind(this, view);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {

        retailers = RetailerService.getInstance().getAllRetailers();
        retailers = UserInterestService.filterRetailers(retailers);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);

        adapter = new MyBrandsGridAdapter(getActivity(), retailers, GATracker.SCREEN_NAME_FAVOURITE, false);

        recyclerView.addItemDecoration(Utils.getItemDecoration(getActivity(), R.dimen.brands_image_size, -1, 3));

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOpenRetailerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RetailerVouchersActivity.class);
                intent.putExtra(Constants.EXTRAS_RETAILER_ID, ((Retailer) v.getTag()).getId());
                startActivity(intent);
            }
        });

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && adapter!=null) {
            updateViews();
        }
    }

    private void updateViews() {
        retailers = UserInterestService.filterRetailers(RetailerService.getInstance().getAllRetailers());
        adapter.setRetailers(retailers);
        adapter.notifyDataSetChanged();
    }

}
