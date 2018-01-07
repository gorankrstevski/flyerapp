package com.cuponation.android.util;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.tracking.GATracker;
import com.cuponation.android.ui.activity.RetailerVouchersActivity;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 9/7/16.
 */

public class SearchUtility {

    public static void setupSearchView(final Activity activity, final SearchView mSearchView) {
        mSearchView.setVersion(SearchView.VERSION_TOOLBAR_ICON);
        mSearchView.setNavigationIcon(R.drawable.ic_search_black_24dp);
        mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_TOOLBAR_SMALL);
        mSearchView.setHint(R.string.cn_app_search_bar);
        mSearchView.setTextSize(16);
        mSearchView.setDivider(false);
        mSearchView.setVoice(false);
        mSearchView.setShouldClearOnClose(true);
        mSearchView.setAnimationDuration(SearchView.ANIMATION_DURATION);
        mSearchView.setShadowColor(ContextCompat.getColor(activity, R.color.search_shadow_layout));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // mSearchView.close(false);
                SearchUtility.performSearch(activity, query, true, GATracker.SEARCH_SUGG_TYPE_NA);
                mSearchView.close(false);
                mSearchView.setText("");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public void onOpen() {
                mSearchView.setNavigationIcon(R.drawable.search_ic_arrow_back_black_24dp);
            }

            @Override
            public void onClose() {
                mSearchView.setNavigationIcon(R.drawable.ic_search_black_24dp);
            }
        });

        mSearchView.setOnMenuClickListener(new SearchView.OnMenuClickListener() {
            @Override
            public void onMenuClick() {
                mSearchView.close(false);
                mSearchView.setText("");
            }
        });

        mSearchView.setText("");

    }

    public static void performSearch(Activity activity, String searchTerm, boolean isManualSearch, String suggestionType) {

        SearchHistoryTable historyDatabase = new SearchHistoryTable(activity);

        historyDatabase.addItem(new SearchItem(searchTerm));
        historyDatabase.setHistorySize(2);
        Retailer retailer = RetailerService.getInstance().matchRetailerName(searchTerm);
        if (retailer != null) {
            GATracker.getInstance(activity.getApplicationContext()).trackSearch(
                    isManualSearch ? GATracker.SEARCH_METHOD_MANUAL : GATracker.SEARCH_METHOD_SUGGESTION,
                    searchTerm, true, suggestionType);
            startRetaielerVoucherActivity(activity, retailer);
            return;
        }

        GATracker.getInstance(activity.getApplicationContext()).trackSearch(
                isManualSearch ? GATracker.SEARCH_METHOD_MANUAL : GATracker.SEARCH_METHOD_SUGGESTION,
                searchTerm, false, suggestionType);
        DialogUtil.showAlertDialog(activity);
    }

    public static void populateSuggestionTable(final Activity activity, final SearchView mSearchView, List<Retailer> suggestionsList) {

        if (suggestionsList == null) {
            return;
        }

        List<SearchItem> searchItems = new ArrayList<>();
        for (Retailer retailer : suggestionsList) {
            searchItems.add(new SearchItem(retailer.getName()));
        }

        SearchAdapter searchAdapter = new SearchAdapter(activity, searchItems);
        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                String query = textView.getText().toString();


                String searchType = position < new SearchHistoryTable(activity).getAllItems().size() ?
                        GATracker.SEARCH_SUGG_TYPE_HISTORIC : GATracker.SEARCH_SUGG_TYPE_RETAILER;

                SearchUtility.performSearch(activity, query, false, searchType);
                mSearchView.close(false);
                mSearchView.setText("");
            }
        });
        mSearchView.setAdapter(searchAdapter);
    }

    private static void startRetaielerVoucherActivity(Activity activity, Retailer retailer) {
        Intent intent = new Intent(activity, RetailerVouchersActivity.class);
        intent.putExtra(Constants.EXTRAS_RETAILER_ID, retailer.getId());
        activity.startActivity(intent);
    }

}
