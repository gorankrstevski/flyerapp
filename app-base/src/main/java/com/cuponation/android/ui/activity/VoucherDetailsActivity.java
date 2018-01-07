package com.cuponation.android.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.callback.UseButtonActionListener;
import com.cuponation.android.model.Caption;
import com.cuponation.android.model.Condition;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.UniqueCode;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.service.local.IUseButtonService;
import com.cuponation.android.service.local.NotificationService;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.remote.VoucherService;
import com.cuponation.android.tracking.AdjustTracker;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.base.ClickoutActivity;
import com.cuponation.android.ui.adapter.BannerFragmentAdapter;
import com.cuponation.android.ui.adapter.ShareOptionsGridAdapter;
import com.cuponation.android.ui.dialog.AppRateDialog;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.CountryUtil;
import com.cuponation.android.util.ShareUtility;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.cuponation.android.util.TimeUtil;
import com.cuponation.android.util.Utils;
import com.cuponation.android.util.VoucherClickoutUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import me.relex.circleindicator.CircleIndicator;

import static com.cuponation.android.util.ShareUtility.MAIL_CLIENT_TYPE;
import static com.cuponation.android.util.Utils.isUniqueCode;

/**
 * Created by goran on 7/19/16.
 */

public class VoucherDetailsActivity extends ClickoutActivity {

    private Voucher voucher;
    private VoucherFull voucherExtras;
    private boolean voted = false;

    IUseButtonService useButtonService;

    @BindView(R2.id.retailer_logo)
    ImageView retailerLogo;
    @BindView(R2.id.voucher_image)
    ImageView voucherImage;
    @BindView(R2.id.voucher_title)
    TextView voucherTitle;
    @BindView(R2.id.voucher_code)
    TextView voucherCode;
    @BindView(R2.id.voucher_no_code)
    TextView noVoucherCode;
    @BindView(R2.id.captions_container)
    TextView voucherCaptions;
    @BindView(R2.id.voucher_desc)
    TextView voucherDescription;
    @BindView(R2.id.conditions_container)
    ViewGroup conditionsContainer;
    @BindView(R2.id.voucher_time_left)
    TextView voucherTimeLeft;
    @BindView(R2.id.voucher_votes_up)
    TextView voucherVotesUp;

    @BindView(R2.id.voucher_top)
    ViewGroup voucherTopContainer;
    @BindView(R2.id.voucher_bottom)
    ViewGroup voucherBottomContainer;

    @BindView(R2.id.voucher_bookmark)
    ImageView bookmarkVoucher;

    @BindView(R2.id.vote_up)
    ImageView voteUp;
    @BindView(R2.id.vote_down)
    ImageView voteDown;

    @BindView(R2.id.voucher_copy_and_go_to_shop)
    Button copyCodeAndGoToShopBtn;

    @BindView(R2.id.voucher_banner_view)
    ViewGroup bannerBiew;

    @BindView(R2.id.share_option2)
    View shareImage2;
    @BindView(R2.id.share_option3)
    View shareImage3;

    @BindView(R2.id.share_dialog_recycler_view)
    RecyclerView shareDialogRecyclerView;
    @BindView(R2.id.share_apps_view)
    View shareAppsView;

    @BindView(R2.id.elevation_dropshadow)
    View elevationDropShadow;

    private ViewPager viewpager;

    boolean isFromShare = false;

    int slideShowPosition = 0;
    private boolean slideShowStarted = false;
    private Handler handler = new Handler();

    private ActivityInfo shareOption2ActivityInfo = null;
    private ActivityInfo shareOption3ActivityInfo = null;
    private String shareOption2Package = null;
    private String shareOption3Package = null;

    private boolean isUniqueCodeAvailable = true;
    private String uniqueCode;
    private String callerScreenName;
    private boolean voucherDataChanged;
    private long notificationId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementEnterTransition(enterTransition());
        }

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // Do something for lollipop and above versions
            elevationDropShadow.setVisibility(View.VISIBLE);
        }


        CouponingApplication.getGATracker().trackScreen(GATracker.SCREEN_NAME_VOUCHER, null);

        useButtonService = CouponingApplication.getUseButtonService();

        if (getIntent().getExtras() != null) {
            voucher = getIntent().getParcelableExtra(Constants.EXTRAS_VOUCHER);
            originalUrl = getIntent().getExtras().getString(Constants.EXTRAS_URL_KEY);
            isFromShare = getIntent().getExtras().getBoolean(Constants.EXTRAS_IS_FROM_SHARE, false);
            callerScreenName = getIntent().getExtras().getString(Constants.EXTRAS_SCREEN_NAME, "");
            notificationId = getIntent().getExtras().getLong(Constants.EXTRAS_NOTIFICATION_ID, -1L);
        }

        if (voucher != null) {
            initView();
        }
        setShareOptions();

        getUniqueCodes();

        getVoucherExtrasData();

        screenName = GATracker.SCREEN_NAME_VOUCHER;

        AdjustTracker.trackViewContent(voucher.getVoucherId());
        SharedPreferencesUtil.getInstance().setFirstVoucherOpened(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Transition enterTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setInterpolator(new AccelerateDecelerateInterpolator());
        bounds.setDuration(600);

        return bounds;
    }

    private void initView() {

        voucherTitle.setText(voucher.getTitle());

        if (voucher.isCode()) {
            if(!isUniqueCode(voucher)) {
                voucherCode.setText(voucher.getCode());
            }
            voucherCode.setVisibility(View.VISIBLE);
            noVoucherCode.setVisibility(View.GONE);
        } else {
            noVoucherCode.setVisibility(View.VISIBLE);
            voucherCode.setVisibility(View.GONE);
            copyCodeAndGoToShopBtn.setText(R.string.cn_app_go_to_shop);
        }

        voucherDescription.setText(voucher.getDescription());
        if(TimeUtil.getDiff(voucher.getEndDate())<0){
            voucherTimeLeft.setText(R.string.cn_app_code_expired);
            voucherCode.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_color_light_gray));
        }else{
            voucherTimeLeft.setText(TimeUtil.getExpireTime(voucher.getEndDate(), getApplicationContext()));
        }
        voucherVotesUp.setText(String.format(getString(R.string.cn_app_likes), "23"));

        String captions = "";
        if (voucher.getCaptions() != null && voucher.getCaptions().size() > 0) {
            for (Caption caption : voucher.getCaptions()) {
                captions += caption.getTitle() + ": " + caption.getText() + "\r\n";
            }
        }
        voucherCaptions.setText(captions);
        if(BookmarkVoucherService.getInstance().isVoucherBookmarked(voucher)){
            bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24_fill);
        }else{
            bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24);
        }

        initBannerView();
    }

    @OnClick(R2.id.back_btn)
    void onBackBtnClick() {
        onBackPressed();
    }

    @OnClick(R2.id.voucher_copy_and_go_to_shop)
    void onCopyAndGoToShopCode() {

        if (isUniqueCode(voucher) && !isUniqueCodeAvailable) {
            return;
        }

        boolean isAutoCampaignNotification = false;
        boolean isBatchNotification = false;
        if(notificationId!=-1){
            Notification notification = NotificationService.getInstance().getNotification(notificationId);
            if(notification!=null){
                isBatchNotification = notification.getType() == Notification.NotificationType.BATCH_AUTO_NOTIF
                        || notification.getType() == Notification.NotificationType.BATCH_MANUAL_NOTIF;
                isAutoCampaignNotification = notification.isAutoCampaign();
            }
        }

        CouponingApplication.getGATracker().trackClickout(voucher, getTrackingMethod(isBatchNotification, isAutoCampaignNotification), BookmarkVoucherService.getInstance().isVoucherBookmarked(voucher));
        if (Voucher.TYPE_CODE.equals(voucher.getType())) {
            Toast.makeText(getApplicationContext(), R.string.cn_app_code_copied, isCopyCodeMessageFirstShow() ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            if (isUniqueCode(voucher) && isUniqueCodeAvailable) {
                Utils.copyCodeToClipboard(uniqueCode);
            } else {
                Utils.copyCodeToClipboard(voucher.getCode());
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.cn_app_opening_shop, isCopyCodeMessageFirstShow() ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
        }

        String merchantId = RetailerService.UseButtonId.getMerchantId(voucher.getRetailerName());
        if(voucher.isCode() && merchantId != null && useButtonService!=null) {

            useButtonService.doGetAction(merchantId, CountryUtil.getCountrySelection().getButtonsPubRef(), new UseButtonActionListener() {
                @Override
                public void onNoAction() {
                    VoucherClickoutUtil.performClickout(voucher, VoucherDetailsActivity.this, customTabActivityHelper.getSession());
                }
            });

        }else{
            VoucherClickoutUtil.performClickout(voucher, VoucherDetailsActivity.this, customTabActivityHelper.getSession());
        }

    }

    @OnClick(R2.id.vote_down)
    void voteDown() {
        if (!voted)
            vote(false);
        showBannerView();
    }

    @OnClick(R2.id.vote_up)
    void voteUp() {
        if (!voted)
            vote(true);
        showBannerView();
        showAppRateDialog();
    }

    @OnClick(R2.id.share_mail)
    void onEmailShareClick() {
        String appName = "Email";
        String text = ShareUtility.prepareShareContent(getApplicationContext(), voucher, appName);
        Intent intent = ShareUtility.getShareIntent(text);
        intent.setType(MAIL_CLIENT_TYPE);
        startShareActivity(intent, appName);
    }

    @OnClick(R2.id.share_option2)
    void onShareOption2Click() {

        String appName;
        if (shareOption2Package == null) {
            appName = ShareUtility.getApplicationName(shareOption2ActivityInfo.applicationInfo.packageName);
        }else{
            appName = ShareUtility.getApplicationName(shareOption2Package);
        }

        String text = ShareUtility.prepareShareContent(getApplicationContext(), voucher, appName);
        Intent intent = ShareUtility.getShareIntent(text);

        if (shareOption2Package == null) {
            ComponentName name = new ComponentName(
                    shareOption2ActivityInfo.applicationInfo.packageName, shareOption2ActivityInfo.name);
            intent.setComponent(name);

        } else {
            intent.setPackage(shareOption2Package);

        }
        startShareActivity(intent, appName);
    }

    @OnClick(R2.id.share_option3)
    void onShareOption3Click() {

        String appName;
        if (shareOption3Package == null) {
            appName = ShareUtility.getApplicationName(shareOption3ActivityInfo.applicationInfo.packageName);
        }else{
            appName = ShareUtility.getApplicationName(shareOption3Package);
        }

        String text = ShareUtility.prepareShareContent(getApplicationContext(), voucher,appName);
        Intent intent = ShareUtility.getShareIntent(text);

        if (shareOption3Package == null) {
            ComponentName name = new ComponentName(
                    shareOption3ActivityInfo.applicationInfo.packageName, shareOption3ActivityInfo.name);
            intent.setComponent(name);
        } else {
            intent.setPackage(shareOption3Package);
        }
        startShareActivity(intent, appName);
    }

    @OnClick(R2.id.share_more)
    void onOtherShareClick() {
        initShareDialog();
    }

    @OnClick(R2.id.share_dialog_back_btn)
    void onShareDialogBackClick() {
        shareAppsView.setVisibility(View.GONE);
    }

    @OnClick(R2.id.voucher_bookmark)
    void bookmarkVoucher(){
        //SharedPreferencesUtil.getInstance().setUpdateNeededOnHome(true);
        
        voucherDataChanged = true;

        BookmarkVoucherService bookmarkVoucherService = BookmarkVoucherService.getInstance();
        if(bookmarkVoucherService.isVoucherBookmarked(voucher)){
            bookmarkVoucherService.removeVoucherFromSaved(voucher);
            bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24);
            CouponingApplication.getGATracker().trackBookmark(voucher, false, callerScreenName+GATracker.SCREEN_APPEND_OVERLAY);
        }else{
            bookmarkVoucherService.addVoucherToSaved(voucher);
            bookmarkVoucher.setImageResource(R.drawable.ic_bookmark_24_fill);
            CouponingApplication.getGATracker().trackBookmark(voucher, true, callerScreenName+GATracker.SCREEN_APPEND_OVERLAY);
        }
    }

    private void startShareActivity(Intent intent, String appName) {
        try {
            GATracker.getInstance(getApplicationContext()).trackShare(voucher, ShareUtility.getShareMethod(intent.getType(), appName));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "App is not installed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void vote(boolean isVoteUp) {
        voted = true;
        Toast.makeText(getApplicationContext(), R.string.cn_app_vote_message, Toast.LENGTH_SHORT).show();
        if (isVoteUp) {
            voteUp.setImageResource(R.drawable.thumbs_up);
        } else {
            voteDown.setImageResource(R.drawable.thumbs_down);
        }
        CouponingApplication.getGATracker().trackVote(voucher, isVoteUp);
        AdjustTracker.trackVoucherFeedbackVote();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isCopyCodeMessageFirstShow() {
        SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil.getInstance();
        boolean value = sharedPreferencesUtil.getCopyCodeFirstShow();
        if (!value) {
            sharedPreferencesUtil.setCopyCodeFirstShow(true);
        }
        return value;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        if(voucherDataChanged) {
            Intent data = new Intent();
            data.putExtra(Constants.EXTRAS_RETAILER, voucher.getRetailerName());
            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    private void getUniqueCodes(){
        if(isUniqueCode(voucher)) {
            VoucherService.getInstance().getUniqueCode(voucher.getVoucherId(), Utils.getUniqueDeviceId(CouponingApplication.getContext(), voucher.getEndDate()))
                    .compose(this.<UniqueCode>bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<UniqueCode>() {
                        @Override
                        public void accept(UniqueCode code) throws Exception {
                            uniqueCode = code.getCode();
                            voucherCode.setText(code.getCode());
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            isUniqueCodeAvailable = false;
                            Toast.makeText(getApplicationContext(), R.string.cn_app_unique_code_not_available, Toast.LENGTH_LONG).show();
                            copyCodeAndGoToShopBtn.setBackgroundResource(R.drawable.rounded_corners_gray);
                            copyCodeAndGoToShopBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_color_light_gray));
                        }
                    });
        }
    }

    private void getVoucherExtrasData(){
        VoucherService.getInstance().getVoucherFull(voucher.getVoucherId())
                .compose(this.<VoucherFull>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<VoucherFull>() {
                            @Override
                            public void accept(VoucherFull voucherFull) throws Exception {
                                voucherExtras = voucherFull;
                                populateVoucherExtras(voucherExtras);

                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                //throwable.printStackTrace();
                            }
                        }
                );
    }

    private void populateVoucherExtras(VoucherFull voucherExtras) {
        conditionsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (voucherExtras.getConditions() != null) {
            for (Condition condition : voucherExtras.getConditions()) {

                if (!TextUtils.isEmpty(condition.getText())) {

                    View view = inflater.inflate(R.layout.item_condition, null);

                    ((TextView) view.findViewById(R.id.condition_label)).setText(condition.getName());
                    ((TextView) view.findViewById(R.id.condition_value)).setText(condition.getText());

                    conditionsContainer.addView(view);
                }
            }
        }

        if(voucherExtras.getImage()!=null){
            //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) retailerLogo.getLayoutParams();
            //params.height = getResources().getDimensionPixelSize(R.dimen.retailer_logo_height_with_image);
            //retailerLogo.setLayoutParams(params);
            loadRetailerLogo(R.dimen.retailer_logo_height_with_image);

            voucherImage.setVisibility(View.VISIBLE);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int size = getResources().getDimensionPixelSize(R.dimen.voucher_image_size);

            Picasso.with(getApplicationContext()).load(CountryUtil.getCdnImageUrl(voucherExtras.getImage()))
                    .noFade()
                    .centerCrop()
                    .resize(0, size)
                    .into(voucherImage);
            voucherBottomContainer.setVisibility(View.GONE);

        }else{
            loadRetailerLogo(R.dimen.retailer_logo_height_with_image);
        }

    }

    private void loadRetailerLogo(int dimensionResource){
        int size = getResources().getDimensionPixelSize(dimensionResource);
        Picasso.with(getApplicationContext()).load(voucher.getRetailerLogoUrl())
                .noFade()
                .resize(0, size)
                .onlyScaleDown()
                .centerInside()
                .into(retailerLogo);
    }

    private String getTrackingMethod(boolean isBatchNotification, boolean isAutoCampaign) {

        if(isBatchNotification){
            return isAutoCampaign ? GATracker.CLICKOUT_METHOD_BATCH_AUTO : GATracker.CLICKOUT_METHOD_BATCH_MANUAL;
        }

        if (voucher.isCode()) {
            return isFromShare ? GATracker.CLICKOUT_METHOD_SHARE_CODE : GATracker.CLICKOUT_METHOD_IN_APP_CODE;
        } else {
            return isFromShare ? GATracker.CLICKOUT_METHOD_SHARE_DEAL : GATracker.CLICKOUT_METHOD_IN_APP_DEAL;
        }
    }

    private void initBannerView() {
        BannerFragmentAdapter adapter = new BannerFragmentAdapter(getSupportFragmentManager());
        viewpager = ((ViewPager) findViewById(R.id.viewpager_default));
        viewpager.setAdapter(adapter);
        ((CircleIndicator) findViewById(R.id.indicator_default)).setViewPager(viewpager);
        viewpager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                stopBannerSlideShow();
                return false;
            }
        });
        bannerBiew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTutorial();
            }
        });
    }

    private void showBannerView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_from_left);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bannerBiew.setVisibility(View.VISIBLE);
                startBannerSlideShow();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bannerBiew.startAnimation(animation);
    }

    private void openTutorial() {
        Intent i = new Intent(getApplicationContext(), OnboardActivity.class);
        i.putExtra(Constants.EXTRAS_ONBOARDING_FROM_SETTINGS, true);
        startActivity(i);
    }

    @OnClick(R2.id.retailer_logo)
    void startRetaielerVoucherActivity() {
        Retailer retailer = RetailerService.getInstance().getRetailerById(voucher.getRetailerId());
        Intent intent = new Intent(this, RetailerVouchersActivity.class);
        intent.putExtra(Constants.EXTRAS_RETAILER_ID, retailer.getId());
        startActivity(intent);
    }

    private void setShareOptions() {

        // check whatsapp
        if (ShareUtility.isPackageInstalled(ShareUtility.WHATS_APP_PACKAGE)) {
            shareOption2Package = ShareUtility.WHATS_APP_PACKAGE;
        }else{
            shareImage2.setVisibility(View.GONE);
        }

        if (ShareUtility.isPackageInstalled(ShareUtility.MESSENGER_APP_PACKAGE)) {
                shareOption3Package = ShareUtility.MESSENGER_APP_PACKAGE;
        }else{
            shareImage3.setVisibility(View.GONE);
        }

    }

    private void initShareDialog() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);

        String text = ShareUtility.prepareShareContent(getApplicationContext(), voucher, "");
        List<ResolveInfo> resolveInfos = ShareUtility.getShareOptions(ShareUtility.getShareIntent(text));
        ShareOptionsGridAdapter shareOptionsGridAdapter = new ShareOptionsGridAdapter(resolveInfos);

        shareDialogRecyclerView.setLayoutManager(layoutManager);
        shareDialogRecyclerView.setAdapter(shareOptionsGridAdapter);

        shareOptionsGridAdapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = (String) v.getTag();
                String appName = ShareUtility.getApplicationName(packageName);
                String text = ShareUtility.prepareShareContent(getApplicationContext(),voucher, appName);
                Intent intent = ShareUtility.getShareIntent(text);
                intent.setPackage(packageName);
                startShareActivity(intent, appName);
            }
        });

        shareAppsView.setVisibility(View.VISIBLE);
    }

    private void startBannerSlideShow() {
        slideShowStarted = true;
        handler.postDelayed(slideShowRunnable, 2500);
    }

    private void stopBannerSlideShow() {
        handler.removeCallbacks(slideShowRunnable);
    }

    private Runnable slideShowRunnable = new Runnable() {
        @Override
        public void run() {
            slideShowPosition = (slideShowPosition + 1) % viewpager.getAdapter().getCount();
            viewpager.setCurrentItem(slideShowPosition, true);
            if (slideShowStarted) {
                startBannerSlideShow();
            }
        }
    };

    private void showAppRateDialog(){
        final SharedPreferencesUtil preferences = SharedPreferencesUtil.getInstance();
        if(!preferences.getAppRate() && preferences.getAppRateRemindTimestamp() < System.currentTimeMillis()){
            AppRateDialog appRateDialog = new AppRateDialog();
            appRateDialog.show(getSupportFragmentManager(), "");
        }
    }

}
