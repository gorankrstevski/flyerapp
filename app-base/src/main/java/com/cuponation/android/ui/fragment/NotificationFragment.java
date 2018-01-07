package com.cuponation.android.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.eventbus.BookmarkOnNotificationEvent;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.service.local.NotificationService;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.MainActivity;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.activity.VoucherDetailsActivity;
import com.cuponation.android.ui.adapter.GridSpacingItemDecoration;
import com.cuponation.android.ui.adapter.MyBrandsGridAdapter;
import com.cuponation.android.ui.adapter.NotificationListAdapter;
import com.cuponation.android.ui.adapter.RetailerSectionListAdapter;
import com.cuponation.android.ui.listener.OnGridGestureListener;
import com.cuponation.android.ui.view.BottomBarView;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.DeepLinkHandler;
import com.cuponation.android.util.SharedPreferencesUtil;
import com.cuponation.android.util.ShortcutBadgerUtil;
import com.cuponation.android.util.TimeUtil;
import com.cuponation.android.util.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import id.gits.baso.BasoProgressView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import it.sephiroth.android.library.tooltip.Tooltip;

/**
 * Created by goran on 7/25/16.
 */

public class NotificationFragment extends BaseFragment implements OnGridGestureListener, View.OnClickListener {

    private static final int ITEMS_PER_PAGE = 9;

    @BindView(R2.id.notifications_recycle_view)
    RecyclerView notificationRecyclerView;

    @BindView(R2.id.no_notification_view)
    View noNotificationsView;

    @BindView(R2.id.no_notification_message)
    TextView noNotificationMessage;

    @BindView(R2.id.notif_create_button)
    Button toolbarButton;

    @BindView(R2.id.recycle_view)
    RecyclerView recyclerView;

    @BindView(R2.id.alphabet_indexer)
    LinearLayout alphabetIndexer;

    @BindView(R2.id.all_brands_container)
    View allBrandsContainer;

    @BindView(R2.id.popular_recycle_view)
    RecyclerView popularRecyclerView;

    @BindView(R2.id.popular_brands_container)
    View popularBrands;

    @BindView(R2.id.back_btn)
    ImageView backButton;

    @BindView(R2.id.clear_searchview)
    ImageView clearSearchView;

    @BindView(R2.id.search_field)
    EditText searchField;

    @BindView(R2.id.progressview_container)
    BasoProgressView progressView;

    @BindView(R2.id.create_alert_view)
    View createAlertView;

    @BindView(R2.id.toolbar)
    ViewGroup toolbar;

    private NotificationListAdapter adapter;

    private boolean allBrandsAreShown = false;

    private Tooltip.TooltipView tooltipView;
    private Map<String, Integer> positionIndexer;
    private int childrensCount;

    private char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private Map<String, List<Retailer>> sections;
    private List<String> sectonLabels;

    private int gridSpacing = 0;
    private String screenName;

    private List<Notification> notifications;

    public static NotificationFragment getInstance() {
        return new NotificationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this, view);

        screenName = GATracker.SCREEN_NAME_NOTIFICATIONS;
        CouponingApplication.getGATracker().trackScreen(screenName, null);

        checkAndRemoveOldNotification();
        NotificationService notificationService = NotificationService.getInstance();
        notificationService.markAllNotificationAsRead();

        SharedPreferencesUtil.getInstance().setNewNotificationAdded(false);
        ShortcutBadgerUtil.updateAppBadge(notificationService);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

        reloadRetailers();

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.removeAllPending();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private void initView() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        float itemSize = getContext().getResources().getDimensionPixelSize(R.dimen.brands_image_size);
        gridSpacing = (int) ((width - (3 * itemSize)) / 3);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        notificationRecyclerView.setHasFixedSize(true);
        notificationRecyclerView.setLayoutManager(manager);


        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();

        notifications = NotificationService.getInstance().getAllNotifications();

        Collections.sort(notifications, new Comparator<Notification>() {
            @Override
            public int compare(Notification o1, Notification o2) {
                return o1.getDate() > o2.getDate() ? -1 : o1 == o2 ? 0 : 1;
            }
        });

        adapter = new NotificationListAdapter(getContext(), notifications);
        adapter.setOnClickListener(this);
        adapter.setUndoOn(true);
        notificationRecyclerView.setAdapter(adapter);

        if (notifications.size() == 0) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                noNotificationMessage.setText(R.string.cn_app_notification_default6);
            }

            noNotificationsView.setVisibility(View.VISIBLE);
        }

        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                adapter.removeAllPending();
                return false;
            }
        });
        notificationRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN
                        && notificationRecyclerView.findChildViewUnder(event.getX(), event.getY()) == null) {
                    adapter.removeAllPending();
                }
                return false;
            }
        });
    }

    @OnClick(R2.id.to_favorite_btn)
    void onToFavoriteClick() {
        ((MainActivity) getActivity()).onItemClick(BottomBarView.State.FAVOURITE_SCREEN, null);
    }

    @Override
    public void onItemClicked(int position, View view) {
        Boolean isBookmark = (Boolean) view.getTag(Constants.TAG_IS_BOOKMARK);
        Notification.NotificationType notificationType = (Notification.NotificationType) view.getTag(R.integer.tag_notification_type);

        if (isBookmark != null && isBookmark.booleanValue()) {
            EventBus.getDefault().post(new BookmarkOnNotificationEvent((Voucher) view.getTag()));
        } else if (view.getTag() instanceof Voucher) {
            Voucher voucher = (Voucher) view.getTag();
            Intent intent = new Intent(getActivity(), VoucherDetailsActivity.class);
            intent.putExtra(Constants.EXTRAS_VOUCHER, voucher);
            intent.putExtra(Constants.EXTRAS_SCREEN_NAME, screenName);
            Pair<View, String> pair = Pair.create(view.findViewById(R.id.voucher_cardview), "cardview");
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), pair);
            startActivity(intent, options.toBundle());
        } else if (view.getTag() instanceof Retailer) {
            Retailer retailer = (Retailer) view.getTag();
            Intent intent = new Intent(getActivity(), RetailerVouchersActivity.class);
            intent.putExtra(Constants.EXTRAS_RETAILER_ID, retailer.getId());
            startActivity(intent);
        } else if (view.getTag() instanceof String) {

            DeepLinkHandler.handelDeepLink(getActivity(), (String) view.getTag(), null, notificationType, notifications.get(position).getDate());
        } else if (view.getTag() == null) {
            ((MainActivity) getActivity()).onItemClick(BottomBarView.State.HOME_SCREEN, null);
        }
    }

    private boolean isCreateAlertDialogOpened = false;

    @OnClick(R2.id.notif_create_button)
    void onToolbarBtnClick() {
        adapter.removeAllPending();
        if (isCreateAlertDialogOpened) {
            createAlertView.setVisibility(View.GONE);
            toolbarButton.setText(R.string.cn_app_create);
            String message;
            switch (retailersAdded.size()) {
                case 0:
                    message = getString(R.string.cn_app_no_alerts_added);
                    break;
                case 1:
                    message = getString(R.string.cn_app_one_alerts_added);
                    break;
                default:
                    message = retailersAdded.size() + getString(R.string.cn_app_more_alerts_added);
                    break;
            }

            Utils.showFeedback(getContext(), message);
            retailersAdded = new HashSet<>();

        } else {
            createAlertView.setVisibility(View.VISIBLE);
            toolbarButton.setText(R.string.cn_app_done);
        }
        isCreateAlertDialogOpened = !isCreateAlertDialogOpened;
    }

    public boolean isAllBrandsShown() {
        return allBrandsAreShown;
    }

    public void hideAllBrands() {
        allBrandsContainer.setVisibility(View.GONE);
        popularBrands.setVisibility(View.VISIBLE);
        allBrandsAreShown = false;
        reloadRetailers();
    }

    private void reloadRetailers() {
        RetailerService.getInstance().cacheLocallyAllRetailers(3)
                .compose(this.<List<Retailer>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Retailer>>() {
                    @Override
                    public void accept(List<Retailer> retailers) throws Exception {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            initPopularBrands(retailers);
                            setupSearchField();
                            initAllBrands(retailers);
                            progressView.stopAndGone();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            progressView.stopAndError(getString(R.string.cn_app_loading_error));
                        }
                    }
                });
    }


    private boolean isPopularBrandsViewInitilized = false;

    private void initPopularBrands(List<Retailer> retailers) {
        if (!isPopularBrandsViewInitilized) {
            isPopularBrandsViewInitilized = true;

            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);

            popularRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, gridSpacing, false));
            popularRecyclerView.setLayoutManager(layoutManager);

        }

        MyBrandsGridAdapter adapter = new MyBrandsGridAdapter(getActivity(), retailers, screenName, false);
        popularRecyclerView.setAdapter(adapter);
        adapter.setOpenRetailerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RetailerVouchersActivity.class);
                intent.putExtra(Constants.EXTRAS_RETAILER_ID, ((Retailer) v.getTag()).getId());
                startActivity(intent);
            }
        });
        adapter.setOnClickListener(this);
        adapter.setShowNotificationOnChange(false);

    }

    private void initAllBrands(List<Retailer> retailers) {

        sections = new HashMap<>();
        positionIndexer = new HashMap<>();

        for (Retailer retailer : retailers) {
            String firstChar = retailer.getName().substring(0, 1);
            if (sections.containsKey(firstChar)) {
                sections.get(firstChar).add(retailer);
            } else {
                List<Retailer> sectionRetailers = new ArrayList<>();
                sectionRetailers.add(retailer);
                sections.put(firstChar, sectionRetailers);
            }
        }

        sectonLabels = new ArrayList<>(sections.keySet());

        // split retailer list
        List<String> alphabetLabels = new ArrayList<>();
        List<String> othersLabels = new ArrayList<>();
        for (String label : sectonLabels) {
            Collections.sort(sections.get(label), new Comparator<Retailer>() {
                @Override
                public int compare(Retailer lhs, Retailer rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            int letterCode = label.toUpperCase().charAt(0);
            if (letterCode >= 65 && letterCode <= 90) {
                alphabetLabels.add(label);
            } else {
                othersLabels.add(label);
            }
        }

        Collections.sort(alphabetLabels);
        sectonLabels = new ArrayList<>(alphabetLabels);
        sectonLabels.addAll(othersLabels);

        positionIndexer = new HashMap<>();
        int count = 0;
        for (String label : sectonLabels) {
            positionIndexer.put(label, count);
            count = count + sections.get(label).size() + 1;
        }

        RetailerSectionListAdapter adapter = new RetailerSectionListAdapter(sections, sectonLabels, screenName);
        adapter.setOnHeartClickListener(this);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(manager);
        adapter.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        initAlhpabetIndexer(manager);
    }

    private void initAlhpabetIndexer(final GridLayoutManager gridLayoutManager) {

        childrensCount = 0;

        alphabetIndexer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    int height = v.getHeight();
                    float itemSize = height / childrensCount;
                    int position = Math.round(event.getY() / itemSize);

                    if (position >= 0 && position < alphabet.length) {
                        gridLayoutManager.scrollToPositionWithOffset(positionIndexer.get("" + alphabet[position]), 0);
                        Log.d("goran", "position " + alphabet[position] + " = " + positionIndexer.get("" + alphabet[position]));
                    }

                }
                return true;
            }
        });

        alphabetIndexer.removeAllViews();
        for (int i = 0; i < alphabet.length; i++) {

            TextView textView = new TextView(getActivity().getApplicationContext());
            textView.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(param);
            textView.setText(String.valueOf(alphabet[i]));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.alphabet_indexer_letter_size));
            alphabetIndexer.addView(textView);


            childrensCount++;
        }

    }

    private void setupSearchField() {
        searchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                filterRetailers(s.toString());
                if (s.length() > 0) {
                    clearSearchView.setVisibility(View.VISIBLE);
                } else {
                    clearSearchView.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void filterRetailers(String queryString) {

        queryString = queryString.toUpperCase();

        if (queryString.length() >= 1) {

            // filter only first section
            Map<String, List<Retailer>> filteredSections = new HashMap<>();
            List<String> sectionLabels = new ArrayList<>();

            String label = queryString.substring(0, 1);

            if (queryString.length() > 1) {
                sectionLabels.add(label);
                List<Retailer> sectionAllRetailers = sections.get(label);
                List<Retailer> filteredRetailers = new ArrayList<>();
                if (sectionAllRetailers != null) {
                    for (Retailer retailer : sectionAllRetailers) {
                        if (retailer.getName().toLowerCase().startsWith(queryString.toLowerCase())) {
                            filteredRetailers.add(retailer);
                        }
                    }
                }
                filteredSections.put(label, filteredRetailers);
            } else {
                sectionLabels.add(label);
                filteredSections.put(label, sections.get(label));
            }

            loadSections(filteredSections, sectionLabels);
        } else {
            // load all sections
            loadSections(sections, sectonLabels);
        }
    }

    private void loadSections(Map<String, List<Retailer>> sections, List<String> sectionLabels) {

        if (sections != null && sections.size() > 0) {
            RetailerSectionListAdapter adapter = new RetailerSectionListAdapter(sections, sectionLabels, screenName);
            adapter.setShowNotificationOnChange(false);
            adapter.setOnHeartClickListener(this);
            adapter.setLayoutManager((GridLayoutManager) recyclerView.getLayoutManager());
            recyclerView.setAdapter(adapter);
        }

    }

    @OnFocusChange(R2.id.search_field)
    void onSearchFieldFocus(boolean hasFocus) {
        if (hasFocus) {
            allBrandsContainer.setVisibility(View.VISIBLE);
            popularBrands.setVisibility(View.GONE);
            allBrandsAreShown = true;
            backButton.setImageResource(R.drawable.ic_back_orange);
        }
    }

    @OnClick(R2.id.clear_searchview)
    void onClearSearchViewClick() {
        searchField.setText("");
        Utils.hideKeyboard(getActivity());
    }

    @OnClick(R2.id.back_btn)
    void onBackBtnClick() {
        searchField.setText("");
        Utils.hideKeyboard(getActivity());
        hideAllBrands();
        backButton.setImageResource(R.drawable.ic_search_black_24dp);
        searchField.clearFocus();
    }

    private Set<String> retailersAdded = new HashSet<>();

    @Override
    public void onClick(View v) {
        Retailer retailer = (Retailer) v.getTag();
        if (UserInterestService.getInstance().isRetailerLiked(retailer.getId())) {
            retailersAdded.add(retailer.getId());
            Utils.showFeedback(getContext(), getString(R.string.cn_app_alert_added_for) + retailer.getName());
        } else {
            retailersAdded.remove(retailer.getId());
        }
    }

    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.WHITE);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                NotificationListAdapter adapter = (NotificationListAdapter) recyclerView.getAdapter();
                if (adapter.isUndoOn() && adapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                NotificationListAdapter adapter = (NotificationListAdapter) notificationRecyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(notificationRecyclerView);
    }

    private void setUpAnimationDecoratorHelper() {
        notificationRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }

    private void checkAndRemoveOldNotification() {
        NotificationService notificationService = NotificationService.getInstance();
        List<Notification> notifications = notificationService.getAllNotifications();
        for (Notification notification : notifications) {
            if (notification.getType() == Notification.NotificationType.EXPIRED_NOTIF
                    && TimeUtil.getDiff(notification.getVoucher().getEndDate()) < 0) {
                notificationService.removeNotification(notification);
            }
        }
    }
}
