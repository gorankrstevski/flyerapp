package com.cuponation.android.service.local;

import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;

/**
 * Created by goran on 9/19/17.
 */

public class SuggestedRetailersService {

    private static final int LEADING_RETAILERS_PER_CATEGORY = 3;

    private static String VISITED_RETAILERS_CATEGORY_REGISTER = "visited_retaielrs_category_reg";
    private static String LIKED_RETAILERS_CATEGORY_REGISTER = "liked_retaielrs_category_reg";

    private static SuggestedRetailersService me;

    private SuggestedRetailersService(){
        super();
    }

    public static SuggestedRetailersService getInstance(){
        if(me == null){
            me = new SuggestedRetailersService();
        }

        return me;
    }

    public Map<String, Long> getCategoryVisitsMap(){
        return Paper.book().read(VISITED_RETAILERS_CATEGORY_REGISTER, new HashMap<String, Long>());
    }

    public void addCategoryVisits(List<VoucherFull> voucherList){
        Map<String, Long> categoryVisitsMap = getCategoryVisitsMap();

        for(VoucherFull voucher : voucherList) {
            Long count = categoryVisitsMap.get(voucher.getCategory());
            if(count == null) {
                count = 0l;
            }
            count++;
            categoryVisitsMap.put(voucher.getCategory(), count);

        }

        Paper.book().write(VISITED_RETAILERS_CATEGORY_REGISTER, categoryVisitsMap);
    }

    public Map<String, Long> getCategoryLikedMap(){
        return Paper.book().read(LIKED_RETAILERS_CATEGORY_REGISTER, new HashMap<String, Long>());
    }

    public void addCategoryLiked(List<Retailer> retailers){
        // always rewrite results, cause list of liked retailers could be changed
        Map<String, Long> categoryVisitsMap = new HashMap<>();

        for(Retailer retailer : retailers) {
            List<Voucher> voucherList = retailer.getVouchers();
            for (Voucher voucher : voucherList) {
                Long count = categoryVisitsMap.get(voucher.getCategory());
                if (count == null) {
                    count = 0l;
                }
                count++;
                categoryVisitsMap.put(voucher.getCategory(), count);

            }
        }

        Paper.book().write(LIKED_RETAILERS_CATEGORY_REGISTER, categoryVisitsMap);
    }

    public List<String> getUserCategoryPreference(){

        Map<String, Long> aggregatedCategoryMap = getCategoryLikedMap();
        Map<String, Long> visitedCategoryMap = getCategoryVisitsMap();

        for(String categoryName : visitedCategoryMap.keySet()){
            Long count = aggregatedCategoryMap.get(categoryName);

            if(count==null){
                count = 0l;
            }
            aggregatedCategoryMap.put(categoryName, count + visitedCategoryMap.get(categoryName));
        }

        List<CategoryCount> resultList = new ArrayList<>();
        for(String categoryName : aggregatedCategoryMap.keySet()){
            aggregatedCategoryMap.get(categoryName);

            CategoryCount categoryCount = new CategoryCount(categoryName, aggregatedCategoryMap.get(categoryName));
            resultList.add(categoryCount);
        }

        Collections.sort(resultList, new Comparator<CategoryCount>() {
            @Override
            public int compare(CategoryCount o1, CategoryCount o2) {
                return o1.getCount() > o2.getCount() ? -1 : 1;
            }
        });

        List<String> categories = new ArrayList<>();

        int size = Math.min(resultList.size(), LEADING_RETAILERS_PER_CATEGORY);

        for(CategoryCount categoryCount : resultList.subList(0, size)){
            categories.add(categoryCount.getCategory());
        }

        return categories;
    }

    private class CategoryCount {

        public CategoryCount(String category, long count) {
            this.category = category;
            this.count = count;
        }

        private String category;
        private long count;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
