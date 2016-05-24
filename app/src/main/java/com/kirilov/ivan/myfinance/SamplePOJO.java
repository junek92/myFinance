package com.kirilov.ivan.myfinance;

/**
 * Created by Ivan on 22-May-16.
 */
public class SamplePOJO{
    // attributes
    private long trId;
    private long atDate;
    private double accUsed;
    private double catUsed;
    private double amountSpent;
    private String currUsed;
    private String descAdded;


    // empty public constructor is required for DataSnapshot.getValue(SamplePOJO.class)
    public SamplePOJO(){

    }

    public SamplePOJO(long trId, long atDate, double accUsed, double catUsed, double amountSpent, String currUsed, String descAdded) {
        this.trId = trId;
        this.atDate = atDate;
        this.accUsed = accUsed;
        this.catUsed = catUsed;
        this.amountSpent = amountSpent;
        this.currUsed = currUsed;
        this.descAdded = descAdded;
    }

    public String toString(){
        return "\t TRID: " + Long.toString(this.trId) + "\n\t DATE: " + Long.toString(this.atDate)
                + "\n\t ACC: " + Double.toString(this.accUsed) + "\n\t CAT: " + Double.toString(this.catUsed)
                + "\n\t AMOUNT: " + Double.toString(this.amountSpent) + "\n\t CUR: " + this.currUsed + "\n\t DESC: " + this.descAdded;
    }

    public long getTrId() {
        return trId;
    }

    public void setTrId(long trId) {
        this.trId = trId;
    }

    public long getAtDate() {
        return atDate;
    }

    public void setAtDate(long atDate) {
        this.atDate = atDate;
    }

    public double getAccUsed() {
        return accUsed;
    }

    public void setAccUsed(int accUsed) {
        this.accUsed = accUsed;
    }

    public double getCatUsed() {
        return catUsed;
    }

    public void setCatUsed(int catUsed) {
        this.catUsed = catUsed;
    }

    public double getAmountSpent() {
        return amountSpent;
    }

    public void setAmountSpent(double amountSpent) {
        this.amountSpent = amountSpent;
    }

    public String getCurrUsed() {
        return currUsed;
    }

    public void setCurrUsed(String currUsed) {
        this.currUsed = currUsed;
    }

    public String getDescAdded() {
        return descAdded;
    }

    public void setDescAdded(String descAdded) {
        this.descAdded = descAdded;
    }
}
