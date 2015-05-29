package com.kirilov.ivan.myfinance;

/**
 * Created by Ivan on 26-Apr-15.
 */
public class Account {
    private long accId;
    private String accName;

    //  --- Constructors ---
    public Account(){   }

    public Account(String name){
        this.accName = name;
    }

    public Account(long id, String name){
        this.accId = id;
        this.accName = name;
    }

    //  --- Getters ---
    public long getAccId() {
        return accId;
    }

    public String getAccName() {
        return accName;
    }

    //  --- Setters ---
    public void setAccId(long accId) {
        this.accId = accId;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }
}
