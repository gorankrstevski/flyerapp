package com.cuponation.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.cuponation.android.service.local.RetailerService;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by goran on 7/8/16.
 */

public class Voucher implements Parcelable {

    public static final String TYPE_CODE="code";
    public static final String TYPE_DEAL="deal";

    @SerializedName("caption_1")
    private String caption1;
    @SerializedName("caption_2")
    private String caption2;
    private String title;
    private String description;
    @SerializedName("affiliate_url")
    private String clickoutUrl;
    @SerializedName("code")
    private String code;

    @SerializedName("end_time")
    private String endDate;
    @SerializedName("last_update_time")
    private String lastUpdateTime;

    private String category;
    @SerializedName("affiliate_mode")
    private String type;
    @SerializedName("id_voucher")
    private String voucherId;
    @SerializedName("id_retailer")
    private String retailerId;
    @SerializedName("verified")
    private String verified;
    @SerializedName("mobile_app")
    private int appExclusive;

    // compatibility
    @SerializedName("retailer")
    private String retailerName;

    private ArrayList<Caption> captions;

    public Voucher(){
    }

    public String getShortTitle() {
        return caption1 + " " + caption2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClickoutUrl() {
        return clickoutUrl;
    }

    public void setClickoutUrl(String clickoutUrl) {
        this.clickoutUrl = clickoutUrl;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCaption1(String caption1) {
        this.caption1 = caption1;
    }

    public void setCaption2(String caption2) {
        this.caption2 = caption2;
    }

    public String getCaption1() {
        return this.caption1;
    }

    public String getCaption2() {
        return this.caption2;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public boolean isCode(){
        return Voucher.TYPE_CODE.equals(type);
    }

    protected Voucher(Parcel in) {
        caption1 = in.readString();
        caption2 = in.readString();
        title = in.readString();
        description = in.readString();
        clickoutUrl = in.readString();
        code = in.readString();
        endDate = in.readString();
        lastUpdateTime = in.readString();
        verified = in.readString();
        category = in.readString();
        type = in.readString();

        if (in.readByte() == 0x01) {
            captions = new ArrayList<Caption>();
            in.readList(captions, Caption.class.getClassLoader());
        } else {
            captions = null;
        }
        voucherId = in.readString();
        retailerId = in.readString();
        appExclusive = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caption1);
        dest.writeString(caption2);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(clickoutUrl);
        dest.writeString(code);
        dest.writeString(endDate);
        dest.writeString(lastUpdateTime);
        dest.writeString(verified);
        dest.writeString(category);
        dest.writeString(type);
        if (captions == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(captions);
        }
        dest.writeString(voucherId);
        dest.writeString(retailerId);
        dest.writeInt(appExclusive);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Voucher> CREATOR = new Parcelable.Creator<Voucher>() {
        @Override
        public Voucher createFromParcel(Parcel in) {
            return new Voucher(in);
        }

        @Override
        public Voucher[] newArray(int size) {
            return new Voucher[size];
        }
    };

    public ArrayList<Caption> getCaptions() {
        return captions;
    }

    public void setCaptions(ArrayList<Caption> captions) {
        this.captions = captions;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public int getAppExclusive() {
        return appExclusive;
    }

    public void setAppExclusive(int appExclusive) {
        this.appExclusive = appExclusive;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getRetailerName() {
        return RetailerService.getInstance().getRetailerName(retailerId);
    }

    public String getRetailerLogoUrl() {
        return RetailerService.getInstance().getRetailerLogo(retailerId);
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    // old DB field changed with retailer ID
    public String getCompatRetailerName() {
        return retailerName;
    }
}
