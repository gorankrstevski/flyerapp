package com.cuponation.android.model;

/**
 * Created by goran on 2/10/17.
 */

public class Category {

    public Category(String name, String id, String logo) {
        this.name = name;
        this.id = id;
        this.logo = logo;
    }

    private String name;
    private String id;
    private String logo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
