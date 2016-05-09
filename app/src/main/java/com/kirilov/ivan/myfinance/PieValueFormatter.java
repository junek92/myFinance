package com.kirilov.ivan.myfinance;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Created by Ivan on 16-May-15.
 */
public class PieValueFormatter implements ValueFormatter {

    private DecimalFormat mFormat;
    private String currency;

    public PieValueFormatter(String myCurrency){
        mFormat = new DecimalFormat("###,###,##0.00");
        currency = myCurrency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

//    @Override
//    public String getFormattedValue(float value) {
//        return mFormat.format(value) +" "+currency;
//    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value) +" "+currency;
    }
}
