package com.cuponation.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cuponation.android.R;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.base.ClickoutActivity;
import com.cuponation.android.ui.fragment.RetailerVouchersFragment;
import com.cuponation.android.util.Constants;

/**
 * Created by goran on 9/6/16.
 */

public class RetailerVouchersActivity extends ClickoutActivity {

    RetailerVouchersFragment retailerVouchersFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer);
        initActivity(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initActivity(intent);
    }

    private void initActivity(Intent intent) {

        originalUrl = intent.getStringExtra(Constants.EXTRAS_URL_KEY);

        retailerVouchersFragment = RetailerVouchersFragment.getInstance();
        retailerVouchersFragment.setArguments(intent.getExtras());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, retailerVouchersFragment)
                .commit();

        screenName = GATracker.SCREEN_NAME_RETAILER;
    }

    @Override
    public void onBackPressed() {
        retailerVouchersFragment.trackBackButtonClick();
        super.onBackPressed();
    }
}
