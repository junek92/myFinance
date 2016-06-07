package com.kirilov.ivan.myfinance.firebase_model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ivan on 26-Apr-15.
 */
public class Wallet implements Parcelable {
    private long walletId;
    private String walletName;
    private String walletCurrency;
    private long walletIcon;
    private long walletCreated;
    private long walletLastTrans;
    private double walletIncome;
    private double walletExpenses;
    private double walletBalance;
    private double walletCarryOver;


    //  --- Constructors ---
    public Wallet(){   }

    public Wallet(String name){
        this.walletName = name;
    }

    public Wallet(long walletId, String walletName, String walletCurrency, long walletIcon, long walletCreated,
                  long walletLastTrans, double walletIncome, double walletExpenses, double walletBalance, double walletCarryOver) {
        this.walletId = walletId;
        this.walletName = walletName;
        this.walletCurrency = walletCurrency;
        this.walletIcon = walletIcon;
        this.walletCreated = walletCreated;
        this.walletLastTrans = walletLastTrans;
        this.walletIncome = walletIncome;
        this.walletExpenses = walletExpenses;
        this.walletBalance = walletBalance;
        this.walletCarryOver = walletCarryOver;
    }


    //  --- Getters ---
    public long getWalletId() {
        return walletId;
    }

    public String getWalletName() {
        return walletName;
    }

    public String getWalletCurrency() {
        return walletCurrency;
    }

    public long getWalletIcon() {
        return walletIcon;
    }

    public long getWalletCreated() {
        return walletCreated;
    }

    public long getWalletLastTrans() {
        return walletLastTrans;
    }

    public double getWalletIncome() {
        return walletIncome;
    }

    public double getWalletExpenses() {
        return walletExpenses;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public double getWalletCarryOver() {
        return walletCarryOver;
    }

    //  --- Setters ---
    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public void setWalletCurrency(String walletCurrency) {
        this.walletCurrency = walletCurrency;
    }

    public void setWalletIcon(long walletIcon) {
        this.walletIcon = walletIcon;
    }

    public void setWalletCreated(long walletCreated) {
        this.walletCreated = walletCreated;
    }

    public void setWalletLastTrans(long walletLastTrans) {
        this.walletLastTrans = walletLastTrans;
    }

    public void setWalletIncome(double walletIncome) {
        this.walletIncome = walletIncome;
    }

    public void setWalletExpenses(double walletExpenses) {
        this.walletExpenses = walletExpenses;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public void setWalletCarryOver(double walletCarryOver) {
        this.walletCarryOver = walletCarryOver;
    }

    protected Wallet(Parcel in) {
        walletId = in.readLong();
        walletName = in.readString();
        walletCurrency = in.readString();
        walletIcon = in.readLong();
        walletCreated = in.readLong();
        walletLastTrans = in.readLong();
        walletIncome = in.readDouble();
        walletExpenses = in.readDouble();
        walletBalance = in.readDouble();
        walletCarryOver = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(walletId);
        dest.writeString(walletName);
        dest.writeString(walletCurrency);
        dest.writeLong(walletIcon);
        dest.writeLong(walletCreated);
        dest.writeLong(walletLastTrans);
        dest.writeDouble(walletIncome);
        dest.writeDouble(walletExpenses);
        dest.writeDouble(walletBalance);
        dest.writeDouble(walletCarryOver);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Wallet> CREATOR = new Parcelable.Creator<Wallet>() {
        @Override
        public Wallet createFromParcel(Parcel in) {
            return new Wallet(in);
        }

        @Override
        public Wallet[] newArray(int size) {
            return new Wallet[size];
        }
    };
}


