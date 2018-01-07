package com.cuponation.android.ui.fragment;

import android.content.Intent;

import com.trello.rxlifecycle2.components.support.RxFragment;

/**
 * Created by goran on 7/25/16.
 */

public class BaseFragment extends RxFragment {

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
