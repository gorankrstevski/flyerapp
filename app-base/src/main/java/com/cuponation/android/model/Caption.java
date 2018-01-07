package com.cuponation.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Goran Krstevski (goran.krstevski@cuponation.com) on 25.12.15.
 */
public class Caption implements Parcelable {

    private String title;
    private String text;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    protected Caption(Parcel in) {
        title = in.readString();
        text = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(text);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Caption> CREATOR = new Parcelable.Creator<Caption>() {
        @Override
        public Caption createFromParcel(Parcel in) {
            return new Caption(in);
        }

        @Override
        public Caption[] newArray(int size) {
            return new Caption[size];
        }
    };
}
