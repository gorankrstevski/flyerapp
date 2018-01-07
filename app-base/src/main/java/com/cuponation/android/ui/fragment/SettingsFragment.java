package com.cuponation.android.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.BuildConfig;
import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.CountryConfig;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.service.local.NotificationService;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.BatchUtil;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.OnboardActivity;
import com.cuponation.android.ui.activity.TermsConditionsActivity;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.CountryUtil;
import com.cuponation.android.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by goran on 7/25/16.
 */

public class SettingsFragment extends BaseFragment {

    @BindView(R2.id.my_retailer_switch)
    SwitchCompat retailerSwitch;

    @BindView(R2.id.bookmark_switch)
    SwitchCompat bookmarkNotificationsSwitch;

    @BindView(R2.id.promo_switch)
    SwitchCompat promoNotificationsSwitch;

    @BindView(R2.id.push_retailers_container)
    ViewGroup pushRetailersContainer;

    @BindView(R2.id.push_retailers_rows_holder)
    ViewGroup pushRetailersRowsContainer;

    @BindView(R2.id.prefereces_push)
    ViewGroup myRetailersContainer;

    @BindView(R2.id.push_desc)
    TextView myRetailerDesc;

    @BindView(R2.id.my_retailer_caption)
    TextView myRetailerCaption;

    @BindView(R2.id.toolbar)
    TextView toolbar;

    @BindView(R2.id.retailers_switch)
    SwitchCompat allRetailersSwitch;

    @BindView(R2.id.all_retailers_label)
    TextView allRetailersPushLabel;

    @BindView(R2.id.push_enabled_retailers_pref)
    View pushEnabledRetailersSection;

    @BindView(R2.id.country_selection_pref)
    View countrySelectionPref;

    @BindView(R2.id.country_selection_container)
    View countrySelectionSection;

    @BindView(R2.id.countries_rows_holder)
    ViewGroup countriesRowsHolder;

    @BindView(R2.id.country_selection_label)
    TextView countrySelectionLabel;

    @BindView(R2.id.country_selection_flag)
    ImageView countrySelectionFlag;

    @BindView(R2.id.terms_and_cond_btn)
    View termsAndConditions;

    private boolean skipAllSwitchChange = true;

    public static SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        toolbar.setText(R.string.cn_app_settings);

        retailerSwitch.setChecked(SharedPreferencesUtil.getInstance().getMyRetailersNotification());
        bookmarkNotificationsSwitch.setChecked(SharedPreferencesUtil.getInstance().isBokmarkNotificationEnabled());
        promoNotificationsSwitch.setChecked(SharedPreferencesUtil.getInstance().getPromoNotification());

        CouponingApplication.getGATracker().trackScreen(GATracker.SCREEN_NAME_SETTINGS, null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            myRetailerDesc.setText(R.string.cn_app_myretailers_settings_text);
            myRetailerCaption.setText(R.string.cn_app_myretailers_settings_title);
        }else{
            myRetailersContainer.setVisibility(View.GONE);
            myRetailerDesc.setVisibility(View.GONE);
        }

        if(UserInterestService.getInstance().getPushEnabledRetailers().size() == 0){
            pushEnabledRetailersSection.setVisibility(View.GONE);
        }

        allRetailersSwitch.setOnCheckedChangeListener(checkedChangeListener);

        loadPushRetailersOptions();

        if(BuildConfig.IS_WL_BUILD){
            countrySelectionPref.setVisibility(View.GONE);
        }else{
            loadCountrySelector();
        }

        return view;
    }

    @OnCheckedChanged(R2.id.my_retailer_switch)
    void onMyRetailerClick() {
        if (retailerSwitch.isChecked()) {
            SharedPreferencesUtil.getInstance().setMyRetailersNotification(true);
            CouponingApplication.getGATracker().trackSubscription(true);
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle("")
                    .setMessage(R.string.cn_app_settings_my_retailer_alert)
                    .setPositiveButton(R.string.cn_app_settings_alert_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            SharedPreferencesUtil.getInstance().setMyRetailersNotification(false);
                            CouponingApplication.getGATracker().trackSubscription(false);
                        }
                    })
                    .setNegativeButton(R.string.cn_app_settings_alert_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            retailerSwitch.setChecked(true);
                        }
                    })
                    .show();
        }
    }

    @OnCheckedChanged(R2.id.promo_switch)
    void onPromoClick() {
        boolean isEnabled = promoNotificationsSwitch.isChecked();
        SharedPreferencesUtil.getInstance().setPromoNotification(isEnabled);
        BatchUtil.setPromoNotificationsAttribute(isEnabled);
    }

    @OnCheckedChanged(R2.id.bookmark_switch)
    void onBookmarkNotificationClick(){
        if (bookmarkNotificationsSwitch.isChecked()) {
            SharedPreferencesUtil.getInstance().setBokmarkNotificationEnabled(true);
        }else{
            SharedPreferencesUtil.getInstance().setBokmarkNotificationEnabled(false);
        }
    }

    @OnClick(R2.id.terms_and_cond_btn)
    void onTermsAndConditionsClick(){
        Intent intent = new Intent(getActivity().getApplicationContext(), TermsConditionsActivity.class);
        startActivity(intent);
    }

    @OnClick(R2.id.onboarding_btn)
    void onOnboardingClick(){
        Intent i = new Intent(getActivity(), OnboardActivity.class);
        i.putExtra(Constants.EXTRAS_ONBOARDING_FROM_SETTINGS, true);
        startActivity(i);
    }

    @OnClick(R2.id.push_enabled_retailers_pref)
    void onAllNotificationsClick(){
        pushRetailersContainer.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.country_selection_pref)
    void clickOnCountrySelection(){
        countrySelectionSection.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.back_btn)
    void onBackBtnClick(){
        pushRetailersContainer.setVisibility(View.GONE);
    }

    @OnClick(R2.id.country_back_btn)
    void onCountryBackBtnClick(){
        countrySelectionSection.setVisibility(View.GONE);
    }

    public boolean shouldHandleBackButton(){
        return pushRetailersContainer.getVisibility() == View.VISIBLE;
    }

    public void handleBackButton(){
        pushRetailersContainer.setVisibility(View.GONE);
    }

    // Push retailers

    private List<SwitchCompat> retailerSwitches;

    private void loadPushRetailersOptions(){
        final UserInterestService userInterestService = UserInterestService.getInstance();

        final List<Retailer> userInterestRetailers = userInterestService.getUserInterestRetaielrs();
        if(userInterestRetailers.size() == 0){
            pushRetailersContainer.setVisibility(View.GONE);
            return;
        }

        retailerSwitches = new ArrayList<>();

        List<Retailer> enabledRetailers = userInterestService.getPushEnabledRetailers();
        Set<String> enabledIds = new HashSet<>();
        for(Retailer retailer : enabledRetailers){
            enabledIds.add(retailer.getId());
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for(final Retailer retailer : userInterestRetailers){
            View view = inflater.inflate(R.layout.item_push_retailer, null);
            ((TextView)view.findViewById(R.id.retailer_title)).setText(retailer.getName());

            SwitchCompat switchCompat = (SwitchCompat)view.findViewById(R.id.retailer_switch);
            switchCompat.setChecked(enabledIds.contains(retailer.getId()));
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        userInterestService.addPushEnabledRetailer(retailer);
                    }else{
                        userInterestService.removePushEnabledReatiler(retailer);
                    }
                    if(skipAllSwitchChange) {
                        changeSwitchAllIfNeeded();
                    }
                }
            });
            retailerSwitches.add(switchCompat);
            pushRetailersRowsContainer.addView(view);
        }
        changeSwitchAllIfNeeded();
    }

    private void changeSwitchAllIfNeeded(){
        allRetailersSwitch.setOnCheckedChangeListener(null);
        int size = UserInterestService.getInstance().getPushEnabledRetailers().size();
        if(size == 0){
            allRetailersSwitch.setChecked(false);
            allRetailersPushLabel.setText(R.string.cn_app_automatic_all_notifications_off);
        }else{
            allRetailersSwitch.setChecked(true);
            allRetailersPushLabel.setText(R.string.cn_app_automatic_all_notifications_on);
        }
        allRetailersSwitch.setOnCheckedChangeListener(checkedChangeListener);
    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            skipAllSwitchChange = false;
            if (isChecked) {
                for (SwitchCompat switchCompat : retailerSwitches) {
                    if (!switchCompat.isChecked()) {
                        switchCompat.setChecked(true);
                    }
                }
            } else {
                for (SwitchCompat switchCompat : retailerSwitches) {
                    if (switchCompat.isChecked()) {
                        switchCompat.setChecked(false);
                    }
                }
            }
            changeSwitchAllIfNeeded();
            skipAllSwitchChange = true;
        }
    };

    private View selectedView = null;
    private void loadCountrySelector() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final Map<String, CountryConfig> countries = CountryUtil.getBFAppCountries();
        String selectedCountry = SharedPreferencesUtil.getInstance().getCountrySelection();
        for (String country : countries.keySet()) {

            CountryConfig countryConfig = countries.get(country);
            View view = inflater.inflate(R.layout.item_country, null);
            ((TextView) view.findViewById(R.id.country_title)).setText(countryConfig.getCountryName());
            ((ImageView) view.findViewById(R.id.country_selection_flag)).setImageResource(countryConfig.getFlagResourceId());

            if (selectedCountry.equals(country)) {
                selectedView = view;
                view.findViewById(R.id.country_checkmark).setVisibility(View.VISIBLE);
                setCountrySelectionPref(countryConfig);
            } else {
                view.findViewById(R.id.country_checkmark).setVisibility(View.GONE);
            }

            view.setTag(country);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedView.findViewById(R.id.country_checkmark).setVisibility(View.GONE);
                    String countryCode = (String) v.getTag();
                    SharedPreferencesUtil.getInstance().setCountrySelection(countryCode);
                    v.findViewById(R.id.country_checkmark).setVisibility(View.VISIBLE);
                    setCountrySelectionPref(countries.get(countryCode));
                    selectedView = v;
                    reloadCountryData();
                    CouponingApplication.getGATracker().trackLanguageSwitch(countryCode);
                }
            });

            countriesRowsHolder.addView(view);
        }
    }

    private void setCountrySelectionPref(CountryConfig countryConfig){
        countrySelectionLabel.setText(countryConfig.getCountryName() + "   >");
        countrySelectionFlag.setImageResource(countryConfig.getFlagResourceId());
    }

    private void reloadCountryData(){

        SharedPreferencesUtil.getInstance().setRetailersDataTimestamp(0l);
        RetailerService.getInstance().cacheLocallyAllRetailers(3)
                .compose(this.<List<Retailer>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Retailer>>() {
                    @Override
                    public void onNext(List<Retailer> retailers) {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        UserInterestService.getInstance().initialize();
        RetailerService.getInstance().initialize();
        NotificationService.getInstance().initialize();
        BookmarkVoucherService.getInstance().initialize();
    }
}
