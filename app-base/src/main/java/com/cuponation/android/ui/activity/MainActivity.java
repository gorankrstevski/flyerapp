package com.cuponation.android.ui.activity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cuponation.android.BuildConfig;
import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.model.eventbus.BookmarkAnimationEvent;
import com.cuponation.android.model.eventbus.BookmarkOnNotificationEvent;
import com.cuponation.android.model.eventbus.UpdateBottomBarEvent;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.network.RequestHelper;
import com.cuponation.android.receiver.AlarmReceiver;
import com.cuponation.android.service.local.BookmarkVoucherService;
import com.cuponation.android.service.local.NotificationService;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.service.notif.WebHistoryObserverService;
import com.cuponation.android.service.remote.VoucherService;
import com.cuponation.android.tracking.BatchUtil;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.base.ClickoutActivity;
import com.cuponation.android.ui.dialog.ExpiredVoucherDialog;
import com.cuponation.android.ui.fragment.BaseFragment;
import com.cuponation.android.ui.fragment.BookmarksFragment;
import com.cuponation.android.ui.fragment.FavouriteBrandsFragment;
import com.cuponation.android.ui.fragment.HomeFragment;
import com.cuponation.android.ui.fragment.NotificationFragment;
import com.cuponation.android.ui.fragment.SettingsFragment;
import com.cuponation.android.ui.view.BottomBarView;
import com.cuponation.android.util.AnimationUtil;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.DeepLinkHandler;
import com.cuponation.android.util.DialogUtil;
import com.cuponation.android.util.NotificationsUtil;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.cuponation.android.util.ShortcutBadgerUtil;
import com.cuponation.android.util.Utils;
import com.google.android.instantapps.InstantApps;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static com.cuponation.android.util.NotificationsUtil.DEFAULT_NOTIFICATION_ID;

public class MainActivity extends ClickoutActivity implements BottomBarView.OnMenuItemClickListener {

    private static final String LOG = MainActivity.class.getSimpleName();

    private static final int ONBOARDING_REQUEST_CODE = 101;

    @BindView(R2.id.bottom_bar)
    BottomBarView bottomBarView;

    @BindView(R2.id.bookmark_animation)
    ImageView bookmarkAnimationView;

    @BindView(R2.id.install_full_app_button)
    Button installFullApp;

    private BaseFragment currentFragment;
    private BottomBarView.State currentState;

    private GATracker gaTracker;

    private NotificationService notificationService;
    private int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            currentFragment = HomeFragment.getInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, currentFragment)
                    .commit();
            currentState = BottomBarView.State.HOME_SCREEN;
            screenName = GATracker.SCREEN_NAME_HOME;
        }

        bottomBarView.setOnMenuItemClick(this);
        gaTracker = CouponingApplication.getGATracker();
        notificationService = NotificationService.getInstance();

        UserInterestService userInterestService = UserInterestService.getInstance();
        if (BuildConfig.IS_WL_BUILD && !SharedPreferencesUtil.getInstance().isLexressBatchAccountTransferDone()) {
            BatchUtil.trackToNewAccount(userInterestService.getLikedRetailers(), userInterestService.getPushEnabledRetailers(), userInterestService.getVisitedRetailers());
            SharedPreferencesUtil.getInstance().setLexressBatchAccountTransferDone(true);
        }

        if (!InstantApps.isInstantApp(this)) {
            startService();
            startAlarm();
            startOnBoardingScreen();
            sendMediaMetrieRequest();
            handleExtrasAndDeepLinks(getIntent());
        }else{
            installFullApp.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomBarView.updateMarksForMenuItems();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleExtrasAndDeepLinks(intent);
    }

    private void handleExtrasAndDeepLinks(Intent intent) {
        orientation = getResources().getConfiguration().orientation;
        String action = intent.getAction();
        String data = intent.getDataString();
        String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Notification.NotificationType notificationType = NotificationsUtil.getNotificationType(intent.getExtras());
        String retailerId = intent.getStringExtra(Constants.EXTRAS_RETAILER_ID);

        if(intent.getExtras()!=null) {
            CouponingApplication.getBatchService().showBatchLanding(getApplicationContext(), intent, getSupportFragmentManager());
        }

        if (Intent.ACTION_VIEW.equals(action) && Notification.NotificationType.EXPIRED_NOTIF == notificationType) {
            updateBottomBarNotificationDot(false);
            Voucher voucher = intent.getParcelableExtra(Constants.EXTRAS_VOUCHER);
            gaTracker.trackExpireVoucherNotificationClick(voucher);
            onMessageEvent(new BookmarkOnNotificationEvent(voucher));
        } else if (Intent.ACTION_VIEW.equals(action) && data != null) {


            long notificationId = intent.getLongExtra(Constants.EXTRAS_LOCAL_NOTIFICATION_ID, 0);
            notificationService.markNotificationAsRead(notificationId);

            ShortcutBadgerUtil.updateAppBadge(notificationService);

            boolean handled = DeepLinkHandler.handelDeepLink(this, data, intent.getExtras(), notificationType, notificationId);

            if(!handled && DeepLinkHandler.getDeepLingType(data) == DeepLinkHandler.DeepLinkType.Notifications){
                updateBottomBarNotificationDot(false);
                onItemClick(BottomBarView.State.NOTIFICATION_SCREEN, null);
                gaTracker.trackOpenApp(GATracker.getOpenType(notificationType), null, null);
            }else if(!handled && DeepLinkHandler.getDeepLingType(data) == DeepLinkHandler.DeepLinkType.Settings){
                gaTracker.trackEvent(GATracker.ACTION_OPTIONS_BUTTON, GATracker.CATEGORY_NOTIFICATION_SETTINGS, "", 0);
                gaTracker.trackOpenApp(GATracker.getOpenType(notificationType), null, null);
                onItemClick(BottomBarView.State.SETTINGS_SCREEN, null);
            }
            updateBottomBarNotificationDot(false);
        } else if (Intent.ACTION_SEND.equals(action) && extraText != null) {
            // Other app try to share data with our app, most probably shopping Url
            gaTracker.trackOpenApp(GATracker.OPEN_APP_METHOD_SHARE, null, null);
            handleShareActionFromOtherApp(extraText);
        } else if (Constants.EXTRAS_COPY_ACTION.equals(intent.getAction())) {
            // handle copy action form notification
            handleCopyActionFromNotification(intent, Notification.NotificationType.APP_NOTIF);
        } else if(NotificationsUtil.ACTION_BOOKMARK.equals(action)){
            // handle bookmark voucher action
            downloadAndBookmarkVoucher(intent);
            gaTracker.trackOpenApp(GATracker.getOpenType(notificationType), null, null);
        }else if (retailerId != null) {
            // handle see more action from notification
            gaTracker.trackOpenApp(GATracker.getOpenType(notificationType), RetailerService.getInstance().getRetailerById(retailerId).getName(), null);
            handleSeeMoreActionFromNotification(intent);
            ShortcutBadgerUtil.updateAppBadge(notificationService);
            updateBottomBarNotificationDot(false);
        } else {
            if(NotificationService.getInstance().getUnreadNotifications().size() > 0){
                onItemClick(BottomBarView.State.NOTIFICATION_SCREEN, null);
            }
            gaTracker.trackOpenApp(GATracker.OPEN_APP_METHOD_MANUAL, null, null);
        }


        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.cancel(DEFAULT_NOTIFICATION_ID);

    }

    private void downloadAndBookmarkVoucher(Intent intent) {
        final String retailerIdd = intent.getStringExtra(Constants.EXTRAS_RETAILER_ID);
        final String voucherId = intent.getStringExtra(Constants.EXTRAS_VOUCHER_ID);
        final long notificationId = intent.getLongExtra(Constants.EXTRAS_LOCAL_NOTIFICATION_ID, 0);

        VoucherService.getInstance().getVoucherFull(voucherId)
                .compose(this.<VoucherFull>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VoucherFull>() {
                               @Override
                               public void accept(VoucherFull voucher) throws Exception {
                                   if (voucher != VoucherService.NOT_FOUND) {
                                       BookmarkVoucherService.getInstance().addVoucherToSaved(voucher);
                                       NotificationsUtil.cancelNotification(getApplicationContext(), 001);
                                       Bundle extras = new Bundle();
                                       extras.putString(Constants.EXTRAS_VOUCHER_ID, voucherId);
                                       onItemClick(BottomBarView.State.BOOKMARKS_SCREEN, extras);
                                       Toast.makeText(getApplicationContext(), R.string.cn_app_voucher_added, Toast.LENGTH_SHORT).show();
                                       NotificationService.getInstance().markNotificationAsRead(notificationId);
                                       updateBottomBarNotificationDot(false);

                                       CouponingApplication.getGATracker().trackBookmark(voucher, true, GATracker.LOCATION_NOTIFICATION);
                                       ShortcutBadgerUtil.updateAppBadge(notificationService);
                                       updateBottomBarNotificationDot(false);
                                   }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(MainActivity.this, R.string.cn_app_check_your_connection, Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void handleShareActionFromOtherApp(String extraText) {
        Retailer retailer = RetailerService.getInstance().matchUrl(extraText);
        if (retailer != null) {
            UserInterestService userInterestService = UserInterestService.getInstance();
            userInterestService.removeFromLikedRetailers(retailer);
            userInterestService.addLikedRetailer(retailer);
            Intent activityIntent = new Intent(this, RetailerVouchersActivity.class);
            activityIntent.putExtra(Constants.EXTRAS_URL_KEY, extraText);
            activityIntent.putExtra(Constants.EXTRAS_RETAILER_ID, retailer.getId());
            activityIntent.putExtra(Constants.EXTRAS_IS_FROM_SHARE, true);
            startActivity(activityIntent);
        } else {
            Toast.makeText(getApplicationContext(), "No voucher for this site", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCopyActionFromNotification(Intent intent, Notification.NotificationType notificationType) {
        Utils.copyCodeToClipboard(intent.getStringExtra(Constants.EXTRAS_CODE_KEY));

        try {
            clickoutVoucher = intent.getExtras().getParcelable(Constants.EXTRAS_VOUCHER);
            originalUrl = intent.getStringExtra(Constants.EXTRAS_URL_KEY);

            gaTracker.trackOpenApp(GATracker.getOpenType(notificationType),
                    clickoutVoucher.getRetailerName(), clickoutVoucher.getVoucherId());

            gaTracker.trackClickout(clickoutVoucher, notificationType == Notification.NotificationType.APP_NOTIF ?
                            GATracker.CLICKOUT_METHOD_NOTIFICATION_CN : GATracker.CLICKOUT_METHOD_NOTIFICATION_BATCH,
                    BookmarkVoucherService.getInstance().isVoucherBookmarked(clickoutVoucher));

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application can handle this request."
                    + " Please install a webbrowser", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void handleSeeMoreActionFromNotification(Intent intent) {
        Intent activityIntent = new Intent(this, RetailerVouchersActivity.class);
        activityIntent.putExtra(Constants.EXTRAS_URL_KEY, intent.getStringExtra(Constants.EXTRAS_URL_KEY));
        activityIntent.putExtras(intent.getExtras());
        startActivity(activityIntent);
    }


    /**
     * Start Background service {WebHistoryObserverService} which will track browsers history
     */
    private void startService() {
        Intent intent = new Intent(getApplicationContext(), WebHistoryObserverService.class);
        startService(intent);
    }

    private void startAlarm() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent recurringIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 30 * 1000, recurringIntent); // Log repetition

        BookmarkVoucherService.getInstance().startBookmarkExpireAlarm(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationsUtil.createNotificationChannel(getApplicationContext());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        BatchUtil.setAttrHasCouponWizzard();
        BatchUtil.setPromoNotificationsAttribute(SharedPreferencesUtil.getInstance().getPromoNotification());
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {

        if (currentState == BottomBarView.State.HOME_SCREEN) {
            super.onBackPressed();
        } else if(currentState == BottomBarView.State.SETTINGS_SCREEN && ((SettingsFragment)currentFragment).shouldHandleBackButton()){
            ((SettingsFragment) currentFragment).handleBackButton();
        }else {
            onItemClick(BottomBarView.State.HOME_SCREEN, null);
        }
    }

    @Override
    public void onItemClick(BottomBarView.State state, Bundle extras) {

        if(openInstallFullApp()){
            return;
        }

        currentState = state;
        switch (state) {
            case SETTINGS_SCREEN:
                SettingsFragment settingsFragment = SettingsFragment.getInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, settingsFragment)
                        .commit();
                currentFragment = settingsFragment;
                screenName = GATracker.SCREEN_NAME_SETTINGS;
                break;
            case HOME_SCREEN:
                HomeFragment homeFragment = HomeFragment.getInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .commit();
                currentFragment = homeFragment;
                screenName = GATracker.SCREEN_NAME_HOME;
                break;
            case NOTIFICATION_SCREEN:
                NotificationFragment notificationFragment = NotificationFragment.getInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, notificationFragment)
                        .commit();
                currentFragment = notificationFragment;
                screenName = GATracker.SCREEN_NAME_NOTIFICATIONS;
                break;
            case FAVOURITE_SCREEN:
                FavouriteBrandsFragment favFragment = FavouriteBrandsFragment.getInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, favFragment)
                        .commit();
                currentFragment = favFragment;
                screenName = GATracker.SCREEN_NAME_FAVOURITE;
                break;
            case BOOKMARKS_SCREEN:
                BookmarksFragment bookmarksFragment = BookmarksFragment.getInstance();
                if(extras!=null) {
                    bookmarksFragment.setArguments(extras);
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, bookmarksFragment)
                        .commit();
                currentFragment = bookmarksFragment;
                break;
        }

        bottomBarView.setActiveMenuItem(state.getIndex());
    }

    private void startOnBoardingScreen() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, OnboardActivity.class);
                    startActivityForResult(i, ONBOARDING_REQUEST_CODE);
                }
            }
        });

        // Start the thread
        t.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ONBOARDING_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BookmarkAnimationEvent event) {

        // set up image view
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bookmarkAnimationView.getLayoutParams();
        int width = bottomBarView.getBookmarksLeftPosition(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        layoutParams.setMargins(bottomBarView.getBookmarksLeftPosition(this), 0, 0, 0);
        layoutParams.width = width;
        layoutParams.height = event.getBitmap().getHeight() / (event.getBitmap().getWidth() / width);
        bookmarkAnimationView.setLayoutParams(layoutParams);
        bookmarkAnimationView.setImageBitmap(event.getBitmap());

        startBookmarkPreviewAnimation();

        // at the end animate bottom bar
        bottomBarView.startBookmarkAnimation(1000);
    }

    private void startBookmarkPreviewAnimation() {
        Animation animation = AnimationUtil.getGrowToFullSize();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bookmarkAnimationView.startAnimation(AnimationUtil.getScaleDownToBottom(400));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bookmarkAnimationView.startAnimation(animation);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BookmarkOnNotificationEvent event) {
        onItemClick(BottomBarView.State.BOOKMARKS_SCREEN, null);
        final Voucher voucher = event.getVoucher();
        BookmarkVoucherService.getInstance().getAllSavedVouchers(true);
        if (!BookmarkVoucherService.getInstance().isVoucherBookmarked(voucher)) {
            // show dialog
            ExpiredVoucherDialog dialog = new ExpiredVoucherDialog();
            dialog.setVoucher(voucher);
            dialog.show(getSupportFragmentManager(), "");
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateBottomBarEvent event) {
        bottomBarView.updateMarksForMenuItems();
    }

    private void updateBottomBarNotificationDot(boolean showNewNotificationsDot) {
        SharedPreferencesUtil.getInstance().setNewBookmarkNotificationAdded(showNewNotificationsDot);
        bottomBarView.updateMarksForMenuItems();
    }

    private void sendMediaMetrieRequest() {
        RequestHelper.getInstance().callMediaMetrie()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        Log.d(LOG, "MediaMetrieAPI request successfully sent" + result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(LOG, "MediaMetrieAPI request failed ");
                        throwable.printStackTrace();
                    }
                });

    }

    @OnClick(R2.id.install_full_app_button)
    void onInstallClick(){
        openInstallFullApp();
    }

    private boolean openInstallFullApp(){
        try {
            if (InstantApps.isInstantApp(getApplicationContext())) {
                DialogUtil.showInstallAppDialog(this);
                //InstantApps.showInstallPrompt(this, 101, "InstallApiActivity");
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;

    }
}
