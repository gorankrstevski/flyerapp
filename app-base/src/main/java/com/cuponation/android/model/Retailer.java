package com.cuponation.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cuponation.android.util.CountryUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 7/12/16.
 */

public class Retailer implements Parcelable {

    public Retailer(Retailer retailer){
        this.id = retailer.id;
        this.name = retailer.name;
        this.domain = retailer.domain;
        this.retailerLogo = retailer.retailerLogo;
        this.vouchers = retailer.vouchers;
    }

    @SerializedName("id_merchant")
    private String id;
    private String name;
    @SerializedName("merchant_url")
    private String domain;
    @SerializedName("mobile_image_thumb")
    private String retailerLogo;
    private List<Voucher> vouchers;
    @SerializedName("category_ids")
    private List<String> categoryIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Voucher> getVouchers() {
        return vouchers;
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
    }

    public String getRetailerLogo() {
        if (retailerLogo != null) {
            return CountryUtil.getCdnImageUrl(retailerLogo.replace("_2.jpg", "_5.jpg").replace("_2.png", "_5.png").replace("_2.gif", "_5.gif"));
        } else {
            return retailerLogo;
        }
    }

    public void setRetailerLogo(String retailerLogo) {
        this.retailerLogo = retailerLogo;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    protected Retailer(Parcel in) {
        id = in.readString();
        name = in.readString();
        domain = in.readString();
        retailerLogo = in.readString();
        if (in.readByte() == 0x01) {
            vouchers = new ArrayList<Voucher>();
            in.readList(vouchers, Voucher.class.getClassLoader());
        } else {
            vouchers = null;
        }
        if (in.readByte() == 0x01) {
            categoryIds = new ArrayList<String>();
            in.readList(categoryIds, String.class.getClassLoader());
        } else {
            categoryIds = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(domain);
        dest.writeString(retailerLogo);
        if (vouchers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(vouchers);
        }
        if (categoryIds == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(categoryIds);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Retailer> CREATOR = new Parcelable.Creator<Retailer>() {
        @Override
        public Retailer createFromParcel(Parcel in) {
            return new Retailer(in);
        }

        @Override
        public Retailer[] newArray(int size) {
            return new Retailer[size];
        }
    };

}