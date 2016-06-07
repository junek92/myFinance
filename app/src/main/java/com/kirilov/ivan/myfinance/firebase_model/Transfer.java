package com.kirilov.ivan.myfinance.firebase_model;

/**
 * Created by Ivan on 07-Jun-16.
 */
public class Transfer {
    private long transferTime;
    private long fromWalletId;
    private long toWalletId;

    public Transfer() {
    }

    public Transfer(long transferTime, long fromWalletId, long toWalletId) {
        this.transferTime = transferTime;
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
    }

    public long getTransferTime() {
        return transferTime;
    }

    public long getFromWalletId() {
        return fromWalletId;
    }

    public long getToWalletId() {
        return toWalletId;
    }

    public void setTransferTime(long transferTime) {
        this.transferTime = transferTime;
    }

    public void setToWalletId(long toWalletId) {
        this.toWalletId = toWalletId;
    }

    public void setFromWalletId(long fromWalletId) {
        this.fromWalletId = fromWalletId;
    }
}
