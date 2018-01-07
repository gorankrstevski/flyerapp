package com.cuponation.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by goran on 6/13/17.
 */

public class Condition implements Parcelable {

    @SerializedName("id_caption")
    private long id;
    private String name;
    private String text;
    private int pos;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    protected Condition(Parcel in) {
        id = in.readLong();
        name = in.readString();
        text = in.readString();
        pos = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(text);
        dest.writeInt(pos);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Condition> CREATOR = new Parcelable.Creator<Condition>() {
        @Override
        public Condition createFromParcel(Parcel in) {
            return new Condition(in);
        }

        @Override
        public Condition[] newArray(int size) {
            return new Condition[size];
        }
    };
}
