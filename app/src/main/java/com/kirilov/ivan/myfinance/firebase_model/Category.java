package com.kirilov.ivan.myfinance.firebase_model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ivan on 26-Apr-15.
 */
public class Category implements Parcelable {
    private long catId;
    private long catType;
    private String catName;
    private long catIcon;
    private long catColor;

    //  --- Constructors ---
    public Category(){  }

    public Category (long type, String name){
        this.catType = type;
        this.catName = name;
    }

    public Category(long catId, long catType, String catName, long catIcon, long catColor) {
        this.catId = catId;
        this.catType = catType;
        this.catName = catName;
        this.catIcon = catIcon;
        this.catColor = catColor;
    }

    //  --- Getters ---
    public long getCatId() {
        return catId;
    }

    public long getCatType() {
        return catType;
    }

    public String getCatName() {
        return catName;
    }

    public long getCatIcon() {
        return catIcon;
    }

    public long getCatColor() {
        return catColor;
    }

    //  --- Setters ---
    public void setCatId(long catId) {
        this.catId = catId;
    }

    public void setCatType(long catType) {
        this.catType = catType;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public void setCatIcon(long catIcon) {
        this.catIcon = catIcon;
    }

    public void setCatColor(long catColor) {
        this.catColor = catColor;
    }

    //  --- PARCELABLE  ---
    protected Category(Parcel in) {
        catId = in.readLong();
        catType = in.readLong();
        catName = in.readString();
        catIcon = in.readLong();
        catColor = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(catId);
        dest.writeLong(catType);
        dest.writeString(catName);
        dest.writeLong(catIcon);
        dest.writeLong(catColor);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
