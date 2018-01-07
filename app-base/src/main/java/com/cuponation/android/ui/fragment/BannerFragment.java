package com.cuponation.android.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.eventbus.BannerAnimationEvent;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.OnboardActivity;
import com.cuponation.android.util.AnimationUtil;
import com.cuponation.android.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by goran on 12/12/16.
 */

public class BannerFragment extends Fragment {

    private static final int BANNER_INDEX_1 = 0;
    private static final int BANNER_INDEX_2 = 1;
    private static final int BANNER_INDEX_3 = 2;

    private int index = BANNER_INDEX_1;
    private ImageView animatedView = null;

    public static BannerFragment newInstance(int position) {

        BannerFragment bannerFragment = new BannerFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.EXTRAS_POSITION, position);
        bannerFragment.setArguments(args);

        return bannerFragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        index = getArguments().getInt(Constants.EXTRAS_POSITION);
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

    @SuppressLint("StringFormatInvalid")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.item_banner, container, false);
        TextView bannerText = (TextView) view.findViewById(R.id.banner_text);
        ImageView bannerImage = (ImageView) view.findViewById(R.id.banner_image);
        ImageView bannerAnimatedView = (ImageView) view.findViewById(R.id.banner_animated);
        bannerAnimatedView.setVisibility(View.GONE);

//        boolean isAndroid6 = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
        boolean isAndroid6 = true;

        switch (index) {
            case BANNER_INDEX_1:
                bannerText.setText(isAndroid6 ? R.string.cn_app_banner_1 : R.string.cn_app_banner_1_android5);
                bannerImage.setImageResource(isAndroid6 ? R.drawable.banner_1 : R.drawable.banner_android5_1);
                if (isAndroid6) {
                    bannerAnimatedView.setVisibility(View.VISIBLE);
                    animatedView = bannerAnimatedView;
                }
                break;
            case BANNER_INDEX_2:
                bannerText.setText(isAndroid6 ? R.string.cn_app_banner_2 : R.string.cn_app_banner_2_android5);
                bannerImage.setImageResource(isAndroid6 ? R.drawable.banner_2 : R.drawable.banner_android5_2);
                break;
            case BANNER_INDEX_3:
                bannerText.setText(isAndroid6 ? String.format(getString(R.string.cn_app_banner_3), getString(R.string.short_app_name)) : getString(R.string.cn_app_banner_3_android5));
                bannerImage.setImageResource(isAndroid6 ? R.drawable.banner_3 : R.drawable.banner_android5_3);
                break;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CouponingApplication.getGATracker().trackBannerWidget(GATracker.WIDGET_ACTION_TAP);
                Intent i = new Intent(getActivity(), OnboardActivity.class);
                i.putExtra(Constants.EXTRAS_ONBOARDING_FROM_SETTINGS, true);
                startActivity(i);
            }
        });

        view.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                stopAnimation();
                return false;
            }
        });

        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BannerAnimationEvent event) {
        if(event.shouldAnimationPlay){
            if(animatedView!=null){
                animatedView.setImageResource(R.drawable.hand);
                AnimationUtil.startDiagonalMoveAnimation(animatedView);
            }
        }else{
            stopAnimation();
        }
    }

    private void stopAnimation(){
        if(animatedView!=null) {
            animatedView.clearAnimation();
            animatedView.setImageResource(R.drawable.hand_with_click);
        }
    }
    
}
