package com.cuponation.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.cuponation.android.R;
import com.cuponation.android.service.local.CategoriesService;
import com.cuponation.android.ui.adapter.CategoryAdapter;


/**
 * Created by goran on 2/10/17.
 */

public class CategoriesListView extends LinearLayout {

    @SuppressWarnings("FieldCanBeLocal")
    private View rootView;
    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView categoriesRecyclerView;

    public CategoriesListView(Context context) {
        super(context);
        initView(context);
    }

    public CategoriesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CategoriesListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CategoriesListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(VERTICAL);
        rootView = LayoutInflater.from(context).inflate(R.layout.view_category_list, this);
        categoriesRecyclerView = (RecyclerView) rootView.findViewById(R.id.category_recycler_view);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, OrientationHelper.HORIZONTAL);
        categoriesRecyclerView.setLayoutManager(layoutManager);

        CategoryAdapter categoryAdapter = new CategoryAdapter(CategoriesService.getInstance().getCategories());
        categoriesRecyclerView.setAdapter(categoryAdapter);
        categoriesRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = getContext().getResources().getDimensionPixelSize(R.dimen.category_item_padding);
                outRect.right = outRect.left;
            }
        });
    }
}
