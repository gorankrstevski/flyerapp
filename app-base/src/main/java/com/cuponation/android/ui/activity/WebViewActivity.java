package com.cuponation.android.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.util.Constants;

import butterknife.BindView;

/**
 * Created by goran on 9/15/16.
 */

public class WebViewActivity extends BaseActivity {

    @BindView(R2.id.web_view)
    WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);


        loadUrl(getIntent().getStringExtra(Constants.EXTRAS_URL_KEY));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url){
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSaveFormData(false);
//        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (url.equals(baseClickout)) {
//                    view.loadUrl(mProduct.getDeepLink());
//                } else {
//                    view.loadUrl(url);
//                }
//                return true;
//            }
//        });
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.loadUrl(url);
    }
}
