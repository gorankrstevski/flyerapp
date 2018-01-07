package com.cuponation.android.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.fragment.BaseFragment;
import com.cuponation.android.ui.fragment.onboard.OnboardFragment;
import com.cuponation.android.util.Constants;

/**
 * Created by goran on 10/2/17.
 */

public class OnboardActivity extends BaseActivity {

    private GATracker gaTracker;
    private BaseFragment currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);

        initActivity();
        gaTracker = CouponingApplication.getGATracker();
        gaTracker.trackOnboarding(GATracker.ONBOARDING_ACTION_START, 0, false, null);

    }


    private void initActivity() {

        currentFragment = OnboardFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, currentFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if(currentFragment instanceof OnboardFragment && ((OnboardFragment) currentFragment).isAllBrandsShown()){
            ((OnboardFragment) currentFragment).onSystemBackButtonClick();
        }else{
            setResult(RESULT_CANCELED);
            super.onBackPressed();

        }
    }
}
