package com.cuponation.android.ui.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.adapter.BannerFragmentAdapter;

import org.greenrobot.eventbus.EventBus;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by goran on 12/12/16.
 */

public class BannerView extends LinearLayout{

    private FragmentManager fragmentManager;

    public BannerView(Context context, FragmentManager fragmentManager) {
        super(context);
        this.fragmentManager = fragmentManager;
        init(context);
    }

    public BannerView(Context context) {
        super(context);
        init(context);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_banner, this);

        BannerFragmentAdapter adapter = new BannerFragmentAdapter(fragmentManager);
        ViewPager viewpager = ((ViewPager)view.findViewById(R.id.viewpager_default));
        viewpager.setAdapter(adapter);
        ((CircleIndicator)view.findViewById(R.id.indicator_default)).setViewPager(viewpager);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CouponingApplication.getGATracker().trackBannerWidget(GATracker.WIDGET_ACTION_SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
