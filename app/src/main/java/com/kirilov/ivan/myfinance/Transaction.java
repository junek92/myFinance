package com.kirilov.ivan.myfinance;

import java.util.Calendar;

/**
 * Created by Ivan on 26-Apr-15.
 */
public class Transaction {
    private long trId;
    private long atDate;
    private int accUsed;
    private int catUsed;
    private double amountSpent;
    private String currUsed;
    private String descAdded;

    //  --- Constructors ---
    public Transaction(){   }

    public Transaction(int acc, int cat, double amount, String currency, String descr){
        this.atDate = Calendar.getInstance().getTimeInMillis();
        this.accUsed = acc;
        this.catUsed = cat;
        this.amountSpent = amount;
        this.currUsed = currency;
        this.descAdded = descr;
    }

    public Transaction(long trid,long created, int acc, int cat, double amount, String currency, String descr){
        this.trId = trid;
        this.atDate = created;
        this.accUsed = acc;
        this.catUsed = cat;
        this.amountSpent = amount;
        this.currUsed = currency;
        this.descAdded = descr;
    }

    //  --- setters ---
    public void setTrId(long trId) {
        this.trId = trId;
    }

    public void setDescAdded(String descAdded) {
        this.descAdded = descAdded;
    }

    public void setAtDate(long atDate) {
        this.atDate = atDate;
    }

    public void setAccUsed(int accUsed) {
        this.accUsed = accUsed;
    }

    public void setCatUsed(int catUsed) {
        this.catUsed = catUsed;
    }

    public void setAmountSpent(double amountSpent) {
        this.amountSpent = amountSpent;
    }

    public void setCurrUsed(String currUsed) {
        this.currUsed = currUsed;
    }

    //  --- Getters ---
    public long getTrId() {
        return trId;
    }

    public long getAtDate() {
        return this.atDate;
    }

    public int getAccUsed() {
        return this.accUsed;
    }

    public int getCatUsed() {
        return this.catUsed;
    }

    public double getAmountSpent() {
        return this.amountSpent;
    }

    public String getCurrUsed() {
        return this.currUsed;
    }

    public String getDescAdded() {
        return this.descAdded;
    }




}
