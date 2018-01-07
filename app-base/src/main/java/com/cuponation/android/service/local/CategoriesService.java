package com.cuponation.android.service.local;

import com.cuponation.android.R;
import com.cuponation.android.app.CouponingApplication;
import com.cuponation.android.model.Category;
import com.cuponation.android.util.CountryUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goran on 10/7/16.
 */

public class CategoriesService {

    private static Map<String, Category> categoryMap = null;
    private static List<Category> categoryList = null;

    private static List<Category> filterCategoryList = null;

    private static CategoriesService me = null;

    private CategoriesService(){
        super();
        initialize();
    }

    public static CategoriesService getInstance(){
        if(me == null){
            me = new CategoriesService();
        }
        return me;
    }

    public Category getCategory(String categoryName){
        for(Category category : categoryList){
            if(categoryName.equals(category.getName())){
                return category;
            }
        }
        return null;
    }

    public String getCategoryName(String categoryId) {
        if(categoryMap.get(categoryId)!=null) {
            return categoryMap.get(categoryId).getName();
        }else{
            return null;
        }
    }

    public String getCategoryLogo(String categoryId) {
        if(categoryMap.get(categoryId)!=null) {
            return categoryMap.get(categoryId).getLogo();
        }else{
            return null;
        }
    }

    private static void initialize(){
        categoryMap = new HashMap<>();
        categoryList = new ArrayList<>();
        filterCategoryList = new ArrayList<>();
        String countryCodeSelected = CountryUtil.getCountrySelection().getCountryCode();
        List<String> categoryNames,categoryIds, categoryLogos, filterCategoryNames = null, filterCategoryIds = null;
        if(countryCodeSelected.equals("uk")) {
            categoryNames = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_uk));
            categoryIds = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_uk_ids));
            categoryLogos = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_uk_logos));
            filterCategoryNames = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_uk_filter));
            filterCategoryIds = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_ids_uk_filter));
        }else if(countryCodeSelected.equals("es")){
            categoryNames = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_es));
            categoryIds = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_es_ids));
            categoryLogos = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_es_logos));
            filterCategoryNames = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_es_filter));
            filterCategoryIds = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_ids_es_filter));
        }else {
            categoryNames = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_fr));
            categoryIds = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_fr_ids));
            categoryLogos = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_fr_logos));
            filterCategoryNames = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_fr_filter));
            filterCategoryIds = Arrays.asList(CouponingApplication.getContext().getResources().getStringArray(R.array.categories_ids_fr_filter));
        }
        for (int i = 0; i < categoryNames.size(); i++) {
            categoryList.add(new Category(categoryNames.get(i), categoryIds.get(i), categoryLogos.get(i)));
        }
        if(filterCategoryNames!=null && filterCategoryIds!=null) {
            for (int i = 0; i < filterCategoryNames.size(); i++) {
                filterCategoryList.add(new Category(filterCategoryNames.get(i), filterCategoryIds.get(i), null));
            }
        }

        for (Category category : categoryList) {
            categoryMap.put(category.getId(), category);
        }

    }

    public List<Category> getCategories() {
        return categoryList;
    }

    public List<Category> getFilterCategories() {
        return filterCategoryList;
    }

}
