package com.kirilov.ivan.myfinance;

/**
 * Created by Ivan on 26-Apr-15.
 */
public class Category {
    private long catId;
    private int catType;
    private String catName;

    //  --- Constructors ---
    public Category(){  }

    public Category (int type, String name){
        this.catType = type;
        this.catName = name;
    }

    public Category(long id, int type, String name){
        this.catId = id;
        this.catType = type;
        this.catName = name;
    }

    //  --- Getters ---
    public long getCatId() {
        return catId;
    }

    public int getCatType() {
        return catType;
    }

    public String getCatName() {
        return catName;
    }

    //  --- Setters ---
    public void setCatId(long catId) {
        this.catId = catId;
    }

    public void setCatType(int catType) {
        this.catType = catType;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }
}
