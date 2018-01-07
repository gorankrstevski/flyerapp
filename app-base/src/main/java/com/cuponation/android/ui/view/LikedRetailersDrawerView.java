package com.cuponation.android.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.adapter.MyBrandsGridAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 7/18/17.
 */

public class LikedRetailersDrawerView extends RelativeLayout {

    private View rootView;

    private RecyclerView recyclerView;

    private MyBrandsGridAdapter adapter ;
    private TextView counter;

    public LikedRetailersDrawerView(Context context) {
        super(context);
        init(context);
    }

    public LikedRetailersDrawerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LikedRetailersDrawerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LikedRetailersDrawerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        final int itemPadding = getResources().getDimensionPixelSize(R.dimen.retailer_feed_padding);

        rootView = LayoutInflater.from(context).inflate(R.layout.added_retailers_drawer, this);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.added_retailers_recycler_view);
        counter = (TextView) rootView.findViewById(R.id.added_retailers_counter);

        recyclerView.addItemDecoration(
                new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        super.getItemOffsets(outRect, view, parent, state);

                        outRect.set(itemPadding, 0, itemPadding, 0);
                    }
                }
        );

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, OrientationHelper.HORIZONTAL);

        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyBrandsGridAdapter(this.getContext(), new ArrayList<Retailer>(), GATracker.SCREEN_NAME_ONBOARDING, true);
        adapter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag(R.integer.tag_position);
                adapter.notifyItemRemoved(position);
                if(onClickListener!=null){
                    onClickListener.onClick(v);
                }
                updateCounter();
            }
        });
        recyclerView.setAdapter(adapter);
        loadLikedRetailers();
    }

    public void loadLikedRetailers(){
        List<Retailer> addedRetailers = UserInterestService.getInstance().getLikedRetailers();
        adapter.setRetailers(addedRetailers);
        adapter.notifyDataSetChanged();
        updateCounter();
    }

    public void updateCounter(){
        counter.setText(String.format(getContext().getString(R.string.cn_app_retailers_added), UserInterestService.getInstance().getLikedRetailers().size()+""));
    }

    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    public MyBrandsGridAdapter getAdapter(){
        return adapter;
    }
}
