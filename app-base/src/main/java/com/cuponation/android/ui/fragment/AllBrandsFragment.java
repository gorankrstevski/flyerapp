package com.cuponation.android.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.cuponation.android.ui.adapter.RetailerSectionListAdapter;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

/**
 * Created by goran on 8/22/16.
 */

public class AllBrandsFragment extends BaseFragment {

    @BindView(R2.id.all_brands_recycle_view)
    RecyclerView allBrandsRecyclerView;
    @BindView(R2.id.alphabet_indexer)
    LinearLayout alphabetIndexer;

    @BindView(R2.id.clear_searchview)
    ImageView clearSearchView;
    @BindView(R2.id.search_field)
    AppCompatEditText searchField;

    @BindView(R2.id.search_bar_icon)
    ImageView searchBarIcon;
    @BindView(R2.id.all_brands_root_view)
    LinearLayout rootView;

    private Map<String, Integer> positionIndexer;
    private Map<String, List<Retailer>> sections;
    private List<String> sectonLabels;
    private int childrensCount;

    private char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static AllBrandsFragment getInstance() {
        return new AllBrandsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_brands, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        setupSearchField();
    }


    private void updateViews() {
        loadSections(sections, sectonLabels);
    }


    private void initView() {

        sections = new HashMap<>();
        List<Retailer> retailers = RetailerService.getInstance().getAllRetailers();
        positionIndexer = new HashMap<>();

        if (retailers == null) {
            return;
        }

        for (Retailer retailer : retailers) {
            String firstChar = retailer.getName().toUpperCase().substring(0, 1);
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

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1);
        allBrandsRecyclerView.setLayoutManager(manager);

        loadSections(sections, sectonLabels);

        initAlhpabetIndexer(manager);

    }

    private void setupSearchField() {
        searchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (lastListPosition == 0 && s.length() == 1) {
                    saveListScrollPosition();
                }
                if (s.length() == 0) {
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
        searchField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    if(TextUtils.isEmpty(searchField.getText())){
                        onClearSearchViewClick();
                        rootView.requestFocus();
                    }
                }
                return false;
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
            RetailerSectionListAdapter adapter = new RetailerSectionListAdapter(sections, sectionLabels, GATracker.SCREEN_NAME_FAVOURITE);
            adapter.setOnItemClickListener(onItemClickListener);
            allBrandsRecyclerView.setAdapter(adapter);
        }
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

        for (int i = 0; i < alphabet.length; i++) {

            TextView textView = new TextView(getContext());
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

    @OnClick(R2.id.clear_searchview)
    void onClearSearchViewClick() {
        searchField.setText("");
        Utils.hideKeyboard(getActivity());
        restoreListScrollPosition();
    }

    @OnClick(R2.id.search_bar_icon)
    void onClickSearchBarIcon(View v) {
        onClearSearchViewClick();
        rootView.requestFocus();
    }

    @OnFocusChange(R2.id.search_field)
    void onFocusSearchBarIcon(View v) {

        if (v.hasFocus()) {
            searchBarIcon.setImageResource(R.drawable.search_ic_arrow_back_black_24dp);
        } else {
            searchBarIcon.setImageResource(R.drawable.ic_search_black_24dp);
        }
    }

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), RetailerVouchersActivity.class);
            intent.putExtra(Constants.EXTRAS_RETAILER_ID, ((Retailer) v.getTag()).getId());
            startActivity(intent);
        }
    };

    private int lastListPosition = 0;

    private void saveListScrollPosition() {
        lastListPosition = ((GridLayoutManager) allBrandsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
    }

    private void restoreListScrollPosition() {
        if (lastListPosition != 0) {
            allBrandsRecyclerView.getLayoutManager().scrollToPosition(lastListPosition);
            lastListPosition = 0;
        }
    }

}
