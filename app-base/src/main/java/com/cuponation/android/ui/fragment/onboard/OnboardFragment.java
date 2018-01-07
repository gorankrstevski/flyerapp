package com.cuponation.android.ui.fragment.onboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.cuponation.android.callback.CategorySelectedCallback;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.CategoriesService;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.local.UserInterestService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.adapter.MyBrandsGridAdapter;
import com.cuponation.android.ui.adapter.RetailerSectionListAdapter;
import com.cuponation.android.ui.fragment.BaseFragment;
import com.cuponation.android.ui.view.CategoriesFilterListView;
import com.cuponation.android.ui.view.InterceptableRelativeLayout;
import com.cuponation.android.ui.view.LikedRetailersDrawerView;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.Utils;
import com.like.LikeButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import id.gits.baso.BasoProgressView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import it.sephiroth.android.library.tooltip.Tooltip;
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;


/**
 * Created by goran on 8/26/16.
 */

public class OnboardFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R2.id.recycle_view)
    RecyclerView allBrandsListRecyclerView;

    @BindView(R2.id.alphabet_indexer)
    LinearLayout alphabetIndexer;

    @BindView(R2.id.all_brands_container)
    View allBrandsContainer;

    @BindView(R2.id.popular_brands_recycle_view)
    RecyclerView popularBrandsRecyclerView;

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

    @BindView(R2.id.retailer_added_icon)
    LikeButton retailerAddedIcon;

    @BindView(R2.id.retailers_added_counter)
    TextView retailerAddedCounter;

    @BindView(R2.id.retailer_added_holder)
    ViewGroup retailerAddedHolder;

    @BindView(R2.id.added_retailers_bg)
    ViewGroup likedRetailersBackground;
    @BindView(R2.id.added_retailers_view)
    LikedRetailersDrawerView likedRetailersDrawerView;

    @BindView(R2.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R2.id.category_filter)
    CategoriesFilterListView categoriesFilterListView;

    @BindView(R2.id.bottom_bar_label)
    TextView bottomBarLabel;

    @BindView(R2.id.done_btn)
    Button doneButton;

    @BindView(R2.id.onboard_bottom_bar)
    ViewGroup onboardBottomBar;

    @BindView(R2.id.snackbar_label_top)
    TextView snackBarLabelTop;

    @BindView(R2.id.snackbar_label_bottom)
    TextView snackBarLabelBottom;

    @BindView(R2.id.onboard_initial_snackbar)
    ViewGroup initialSnackbar;

    @BindView(R2.id.root_view)
    InterceptableRelativeLayout rootView;

    private List<Retailer> popularRetailers = new ArrayList<>();

    private boolean heartTooltipShown = false;
    private boolean doneTooltipShown = false;
    private boolean allBrandsAreShown = false;

    private Tooltip.TooltipView tooltipView;
    private Map<String, Integer> positionIndexer;
    private int childrensCount;

    private char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private Map<String, List<Retailer>> sections;
    private List<String> sectonLabels;

    private boolean retailerLoaded = false;
    private List<String> selectedCategoryIds = new ArrayList<>();

    public static OnboardFragment newInstance() {
        return new OnboardFragment();
    }

    private CompositeDisposable disposables = new CompositeDisposable();
    private String screenName;

    private MyBrandsGridAdapter retailersGridAdapter;

    private GATracker gaTracker;

    private long start;
    private long lastTouchOnScreen = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboard, container, false);
        ButterKnife.bind(this, view);

        start = System.currentTimeMillis();

        disposables = new CompositeDisposable();
        screenName = GATracker.SCREEN_NAME_ONBOARDING;

        gaTracker =  CouponingApplication.getGATracker();

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        disposables.clear();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressView.startProgress();
        progressView.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.startProgress();
                reloadRetailers();
            }
        });
        reloadRetailers();

        List<Retailer> favRetailers = UserInterestService.getInstance().getLikedRetailers();
        setDoneButtonState(favRetailers);
        updateRetailerAddedCounter(favRetailers);

        initGridWithPopularRetailers();

        likedRetailersDrawerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retailer retailer = (Retailer) v.getTag();
                //if(UserInterestService.getInstance().isRetailerLiked(retailer.getId())){
                showUndoSnackbar(retailer);
                List<Retailer> likedRetailers = UserInterestService.getInstance().getLikedRetailers();
                updateRetailerAddedCounter(likedRetailers);
                setDoneButtonState(likedRetailers);
                //}
            }
        });

        categoriesFilterListView.setOnSelectCategoryCallback(new CategorySelectedCallback() {
            @Override
            public void onCategorySelectionChange(List<String> selectedCategory) {
                selectedCategoryIds = selectedCategory;
                reloadRetailers();
            }
        });

        showInitialSnackbar();

        rootView.setInterceptableTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                lastTouchOnScreen = System.currentTimeMillis();
                return false;
            }
        });

    }

    @OnClick(R2.id.done_btn)
    void onDoneButtonClick(){

        String selectedFilterNames = "";
        if(selectedCategoryIds.size() > 0) {
            for(String categoryId : selectedCategoryIds){
                selectedFilterNames += CategoriesService.getInstance().getCategoryName(categoryId) + ",";
            }
            gaTracker.trackOnboarding(GATracker.ONBOARDING_ACTION_SUCCESS, (start - System.currentTimeMillis()) / 1000L,
                    true, selectedFilterNames.substring(0, selectedFilterNames.length()-1));
        }else{
            gaTracker.trackOnboarding(GATracker.ONBOARDING_ACTION_SUCCESS, (start - System.currentTimeMillis()) / 1000L,
                    false, selectedFilterNames);
        }
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean("firstStart", false);
        e.apply();
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
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
        disposables.add(RetailerService.getInstance().cacheLocallyAllRetailers(3)
                .compose(this.<List<Retailer>>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Retailer>>() {
                    @Override
                    public void onNext(List<Retailer> retailers) {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            retailerLoaded = true;
                            initAllBrands(retailers);
                            setupSearchField();
                            renderPopularRetailers(retailers);
                            progressView.stopAndGone();
                            startTooltipLooper();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressView.stopAndError(getString(R.string.cn_app_loading_error));
                    }

                    @Override
                    public void onComplete() {

                    }
                }));

    }

    private void initGridWithPopularRetailers() {

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        popularBrandsRecyclerView.setLayoutManager(layoutManager);

        popularBrandsRecyclerView.addItemDecoration(Utils.getItemDecoration(getActivity(), R.dimen.brands_image_size, -1, 3));
        popularBrandsRecyclerView.setItemAnimator(new ScaleInLeftAnimator());

        retailersGridAdapter = new MyBrandsGridAdapter(getActivity(), popularRetailers, GATracker.SCREEN_NAME_ONBOARDING, false);
        retailersGridAdapter.setOnClickListener(this);
        retailersGridAdapter.setOpenRetailerOnClick(false);
        popularBrandsRecyclerView.setAdapter(retailersGridAdapter);
        retailersGridAdapter.setOpenRetailerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), RetailerVouchersActivity.class);
                intent.putExtra(Constants.EXTRAS_RETAILER_ID, ((Retailer) v.getTag()).getId());
                startActivity(intent);
            }
        });

    }

    private void renderPopularRetailers(List<Retailer> retailers){

        popularRetailers = UserInterestService.filterRetailers(retailers);
        popularRetailers = filterRetailersPerCategory(popularRetailers);
        retailersGridAdapter.setRetailers(popularRetailers);
        retailersGridAdapter.notifyDataSetChanged();
    }

    private void initAllBrands(List<Retailer> retailers) {

        sections = new HashMap<>();
        positionIndexer = new HashMap<>();

        for (Retailer retailer : retailers) {
            String firstChar = retailer.getName().substring(0, 1).toUpperCase();
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
        adapter.setOpenRetailerOnClick(false);
        adapter.setOnHeartClickListener(sectionClickListener);

        if (getActivity() != null) {
            GridLayoutManager manager = new GridLayoutManager(getActivity().getApplicationContext(), 1);
            allBrandsListRecyclerView.setLayoutManager(manager);
            adapter.setLayoutManager(manager);
            allBrandsListRecyclerView.setAdapter(adapter);

            if (!isAlphabetIndexerInitialized) {
                initAlhpabetIndexer();
            }
        }
    }

    private boolean isAlphabetIndexerInitialized = false;

    private void initAlhpabetIndexer() {

        childrensCount = 0;

        alphabetIndexer.removeAllViews();

        alphabetIndexer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    int height = v.getHeight();
                    float itemSize = height / childrensCount;
                    int position = Math.round(event.getY() / itemSize);

                    if (position >= 0 && position < alphabet.length) {
                        ((GridLayoutManager)allBrandsListRecyclerView.getLayoutManager()).scrollToPositionWithOffset(positionIndexer.get("" + alphabet[position]), 0);
                    }

                }
                return true;
            }
        });

        for (int i = 0; i < alphabet.length; i++) {

            TextView textView = new TextView(getActivity().getApplicationContext());
            textView.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(param);
            textView.setText(String.valueOf(alphabet[i]));
            alphabetIndexer.addView(textView);


            childrensCount++;
        }
        isAlphabetIndexerInitialized = true;

    }

    private void setupSearchField() {
        searchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (lastListPosition == 0 && s.length() == 1) {
                    saveListScrollPosition();
                }
                if(s.length() == 0){
                    restoreListScrollPosition();
                }
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

                for (String sectionKey : sections.keySet()) {
                    if (sections.get(sectionKey) != null && sections.get(sectionKey).size() > 0) {

                        List<Retailer> filteredRetailers = new ArrayList<>();
                        for (Retailer retailer : sections.get(sectionKey)) {
                            if (retailer.getName().toLowerCase().contains(queryString.toLowerCase())) {
                                filteredRetailers.add(retailer);
                            }
                        }

                        if (filteredRetailers.size() > 0) {
                            sectionLabels.add(sectionKey);
                            filteredSections.put(sectionKey, filteredRetailers);
                        }
                    }
                }
            } else {
                sectionLabels.add(label);
                filteredSections.put(label, sections.get(label));
            }

            Collections.sort(sectionLabels);
            loadSections(filteredSections, sectionLabels);
        } else {
            // load all sections
            loadSections(sections, sectonLabels);
        }
    }

    private void loadSections(Map<String, List<Retailer>> sections, List<String> sectionLabels) {

        if (sections != null && sections.size() > 0) {
            RetailerSectionListAdapter adapter = new RetailerSectionListAdapter(sections, sectionLabels, screenName);
            adapter.setOnHeartClickListener(sectionClickListener);
            adapter.setLayoutManager((GridLayoutManager) allBrandsListRecyclerView.getLayoutManager());
            allBrandsListRecyclerView.setAdapter(adapter);
        }

    }

    View.OnClickListener sectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            List favRetailers = UserInterestService.getInstance().getLikedRetailers();
            setDoneButtonState(favRetailers);
            updateRetailerAddedCounter(favRetailers);
            likedRetailersDrawerView.loadLikedRetailers();
        }
    };

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

    @OnClick(R2.id.retailer_added_holder)
    void onAddedRetailersClick() {
        if (likedRetailersDrawerView.getVisibility() == View.GONE) {
            likedRetailersDrawerView.setVisibility(View.VISIBLE);
            likedRetailersBackground.setVisibility(View.VISIBLE);
        } else {
            likedRetailersDrawerView.setVisibility(View.GONE);
            likedRetailersBackground.setVisibility(View.GONE);
        }
    }

    @OnClick(R2.id.added_retailers_bg)
    void onAddedRetailerBagroundClick() {
        if (likedRetailersDrawerView.getVisibility() == View.VISIBLE) {
            likedRetailersDrawerView.setVisibility(View.GONE);
            likedRetailersBackground.setVisibility(View.GONE);
        }
    }

    @OnClick(R2.id.back_btn)
    void onBackBtnClick() {
        searchField.setText("");
        Utils.hideKeyboard(getActivity());
        hideAllBrands();
        backButton.setImageResource(R.drawable.ic_search_black_24dp);
        searchField.clearFocus();
    }

    public void onSystemBackButtonClick(){
        onBackBtnClick();
    }

    @Override
    public void onClick(View v) {

        int position = (int) v.getTag(R.integer.tag_position);
        boolean isRemove = (boolean) v.getTag(R.integer.tag_is_remove);
        if(!isRemove) {
            popularRetailers.remove(position);
            popularBrandsRecyclerView.getAdapter().notifyItemRemoved(position);
            popularBrandsRecyclerView.getAdapter().notifyItemRangeChanged(position, popularRetailers.size()-1);
        }

        List favRetailers = UserInterestService.getInstance().getLikedRetailers();
        setDoneButtonState(favRetailers);
        updateRetailerAddedCounter(favRetailers);
        likedRetailersDrawerView.loadLikedRetailers();
    }

    private void setDoneButtonState(List<Retailer> favRetailers) {
        if (favRetailers != null && favRetailers.size() > 0) {
            bottomBarLabel.setText(String.format(getString(R.string.cn_app_onboard_selected_number), String.valueOf(favRetailers.size())));
            doneButton.setVisibility(View.VISIBLE);
            onboardBottomBar.setVisibility(View.VISIBLE);
            heartTooltipShown = true;
            startTooltipLooper();
        } else {
            onboardBottomBar.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);
        }
    }


    private int lastListPosition = 0;

    private void saveListScrollPosition() {
        lastListPosition = ((GridLayoutManager) allBrandsListRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    }

    private void restoreListScrollPosition() {
        if (lastListPosition != 0) {
            allBrandsListRecyclerView.getLayoutManager().scrollToPosition(lastListPosition);
            lastListPosition = 0;
        }
    }

    private boolean isChecked = false;

    private void updateRetailerAddedCounter(List<Retailer> favRetailers) {

        if (favRetailers != null && favRetailers.size() > 0) {
            retailerAddedCounter.setText(favRetailers.size() + "");
            if (!isChecked) {
                retailerAddedIcon.performClick();
                isChecked = true;
            }else{
                retailerAddedIcon.performClick();
                retailerAddedIcon.performClick();
            }
        } else {
            retailerAddedCounter.setText("0");
            if (isChecked) {
                retailerAddedIcon.performClick();
                isChecked = false;
            }
        }
    }

    private void showUndoSnackbar(final Retailer retailer) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, R.string.cn_app_code_removed , Snackbar.LENGTH_LONG)
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        likedRetailersDrawerView.getAdapter().processPendingToRemove(retailer.getId());
                    }
                })
                .setAction(R.string.cn_app_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        likedRetailersDrawerView.getAdapter().undoRemoval(retailer.getId());
                        likedRetailersDrawerView.updateCounter();
                        updateRetailerAddedCounter(UserInterestService.getInstance().getLikedRetailers());
                        setDoneButtonState(UserInterestService.getInstance().getLikedRetailers());
                    }
                });

        snackbar.show();
    }

    private void showInitialSnackbar(){
        List<Retailer> likedRetailers = UserInterestService.getInstance().getLikedRetailers();
        if(likedRetailers!=null && likedRetailers.size()==0) {
            initialSnackbar.setVisibility(View.VISIBLE);
            snackBarLabelTop.setTypeface(Typeface.DEFAULT_BOLD);
            initialSnackbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initialSnackbar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showHeartTooltip() {
        List<Retailer> likedRetailers = UserInterestService.getInstance().getLikedRetailers();
        if (likedRetailers != null && likedRetailers.size() == 0) {
            if (!heartTooltipShown && retailerLoaded) {
                heartTooltipShown = true;
                tooltipView = createTooltip(R.string.cn_app_onboard_heart_tooltip, popularBrandsRecyclerView.getChildAt(4).findViewById(R.id.retailer_fav), Tooltip.Gravity.BOTTOM);
                tooltipView.show();
            }
        }
    }

    private void showDoneButtonTooltip() {
        if (!doneTooltipShown) {
            doneTooltipShown = true;
            tooltipView = createTooltip(R.string.cn_app_done_button_tooltip, doneButton, Tooltip.Gravity.TOP);
            tooltipView.show();
        }

    }

    private Tooltip.TooltipView createTooltip(int textResourceId, View anchorView, Tooltip.Gravity orientation){
        return Tooltip.make(getContext(),
                new Tooltip.Builder(101)
                        .anchor(anchorView, orientation)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 10000)
                        .activateDelay(800)
                        .showDelay(0)
                        .text(getResources(), textResourceId)
                        .withArrow(true)
                        .withOverlay(false)
                        .withStyleId(R.style.ToolTipLayoutCustomStyle)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        );
    }

    private List<Retailer> filterRetailersPerCategory(List<Retailer> retailers){
        if(selectedCategoryIds.size() > 0) {
            List<Retailer> filteredRetailers = new ArrayList<>();
            for (Retailer r : retailers) {
                if (r.getCategoryIds() != null && !Collections.disjoint(r.getCategoryIds(), selectedCategoryIds)) {
                    filteredRetailers.add(r);
                }
            }

            return filteredRetailers;
        }else{
            return retailers;
        }
    }

    // tooltip handlers
    private boolean started = false;
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(lastTouchOnScreen == 0){
                lastTouchOnScreen = System.currentTimeMillis();
            }
            boolean isLastTouch3SecondsAgo = System.currentTimeMillis() - lastTouchOnScreen > 3*1000;
            if(!heartTooltipShown && isLastTouch3SecondsAgo){
                showHeartTooltip();
                stopTooltipLooper();
                return;
            }

            if(!doneTooltipShown && isLastTouch3SecondsAgo){
                showDoneButtonTooltip();
                stopTooltipLooper();
                return;
            }

            if(started) {
                startTooltipLooper();
            }
        }
    };

    public void stopTooltipLooper() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void startTooltipLooper() {
        started = true;
        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
