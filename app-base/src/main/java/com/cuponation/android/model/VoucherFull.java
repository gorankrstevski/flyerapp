package com.cuponation.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goran on 11/24/17.
 */

public class VoucherFull extends Voucher implements Parcelable {


    public VoucherFull () {
        super();
    }

    @SerializedName("image")
    private String image;
    private List<Condition> conditions;
    @SerializedName("isPublished")
    private boolean isPublished;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    protected VoucherFull(Parcel in) {
        super(in);
        image = in.readString();
        if (in.readByte() == 0x01) {
            conditions = new ArrayList<Condition>();
            in.readList(conditions, Condition.class.getClassLoader());
        } else {
            conditions = null;
        }
        isPublished = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(image);
        if (conditions == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(conditions);
        }
        dest.writeByte((byte) (isPublished ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VoucherFull> CREATOR = new Parcelable.Creator<VoucherFull>() {
        @Override
        public VoucherFull createFromParcel(Parcel in) {
            return new VoucherFull(in);
        }

        @Override
        public VoucherFull[] newArray(int size) {
            return new VoucherFull[size];
        }
    };
}
