package com.kirilov.ivan.myfinance.firebase_model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by Ivan on 26-Apr-15.
 */
public class Transaction implements Parcelable {
    private long trId;
    private long trDate;
    private long trWallet;
    private long trType;
    private long trCat;
    private double trAmount;
    private String trCurrency;
    private String trDesc;

    //  --- Constructors ---
    public Transaction(){   }

    public Transaction(long acc, long cat, double amount, String currency, String descr){
        this.trDate = Calendar.getInstance().getTimeInMillis();
        this.trWallet = acc;
        this.trCat = cat;
        this.trAmount = amount;
        this.trCurrency = currency;
        this.trDesc = descr;
    }

    public Transaction(long transactionId,long transactionDate, long transactionWallet, long transactionType, long transactionCat, double transactionAmount, String transactionCurrency, String transactionDescr){
        this.trId = transactionId;
        this.trDate = transactionDate;
        this.trWallet = transactionWallet;
        this.trType = transactionType;
        this.trCat = transactionCat;
        this.trAmount = transactionAmount;
        this.trCurrency = transactionCurrency;
        this.trDesc = transactionDescr;
    }

    //  --- setters ---
    public void setTrId(long trId) {
        this.trId = trId;
    }

    public void setTrDesc(String trDesc) {
        this.trDesc = trDesc;
    }

    public void setTrDate(long trDate) {
        this.trDate = trDate;
    }

    public void setTrWallet(long trWallet) {
        this.trWallet = trWallet;
    }

    public long getTrType() {
        return trType;
    }

    public void setTrCat(long trCat) {
        this.trCat = trCat;
    }

    public void setTrAmount(double trAmount) {
        this.trAmount = trAmount;
    }

    public void setTrCurrency(String trCurrency) {
        this.trCurrency = trCurrency;
    }

    //  --- Getters ---
    public long getTrId() {
        return trId;
    }

    public long getTrDate() {
        return this.trDate;
    }

    public long getTrWallet() {
        return this.trWallet;
    }

    public void setTrType(long trType) {
        this.trType = trType;
    }

    public long getTrCat() {
        return this.trCat;
    }

    public double getTrAmount() {
        return this.trAmount;
    }

    public String getTrCurrency() {
        return this.trCurrency;
    }

    public String getTrDesc() {
        return this.trDesc;
    }

    protected Transaction(Parcel in) {
        trId = in.readLong();
        trDate = in.readLong();
        trWallet = in.readLong();
        trType = in.readLong();
        trCat = in.readLong();
        trAmount = in.readDouble();
        trCurrency = in.readString();
        trDesc = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(trId);
        dest.writeLong(trDate);
        dest.writeLong(trWallet);
        dest.writeLong(trType);
        dest.writeLong(trCat);
        dest.writeDouble(trAmount);
        dest.writeString(trCurrency);
        dest.writeString(trDesc);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
}
