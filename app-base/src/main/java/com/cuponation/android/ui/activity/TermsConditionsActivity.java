package com.cuponation.android.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.util.CountryUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by goran on 11/2/16.
 */

public class TermsConditionsActivity extends BaseActivity{

    @BindView(R2.id.web_view)
    WebView mWebView;

    @BindView(R2.id.toolbar)
    Toolbar toolbar;

    @BindView(R2.id.header_back_btn)
    ImageView backBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_cond);

        loadUrl();

        toolbar.setTitle(R.string.cn_app_terms_and_conditions);
        backBtn.setVisibility(View.VISIBLE);
        screenName = GATracker.SCREEN_NAME_SETTINGS;

    }


    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(){
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSaveFormData(false);
        mWebView.getSettings().setUseWideViewPort(true);
        //noinspection deprecation
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

        });
        if(CountryUtil.UK_COUNTRY_CODE.equals((CountryUtil.getCountryCode()))){
            mWebView.loadUrl("file:///android_asset/terms_uk.html");
        }else if(CountryUtil.SPAIN_COUNTRY_CODE.equals((CountryUtil.getCountryCode()))){
            mWebView.loadUrl("file:///android_asset/terms_es.html");
        }else{
            mWebView.loadUrl("file:///android_asset/terms.html");
        }
    }

    @OnClick(R2.id.header_back_btn)
    void onBackButtonClick(){
        onBackPressed();
    }
}
