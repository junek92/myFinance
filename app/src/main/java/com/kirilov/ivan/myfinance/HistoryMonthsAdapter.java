package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 18-May-15.
 */
public class HistoryMonthsAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;

    List<Long> usefulMonths;        // store 1st day of each month with transactions
    List<String> monthName;         // store strings in format MMM-yyyy
    List<Double> monthBalance;      // store each month balance in double

    public HistoryMonthsAdapter(Context context, LayoutInflater layoutInflater){
        mContext = context;
        mLayoutInflater = layoutInflater;

        usefulMonths = new ArrayList<>();
        monthName = new ArrayList<>();
        monthBalance = new ArrayList<>();
    }

    private static class ViewHolder{
        // Used to pack the views in every row into single object
        public TextView mTextViewName;
        public TextView mTextViewBalance;
    }

    @Override
    public int getCount() {
        return usefulMonths.size();
    }

    @Override
    public Object getItem(int position) {
        return usefulMonths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return usefulMonths.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.row_history_months, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTextViewName = (TextView) convertView.findViewById(R.id.history_row_month_name);
            viewHolder.mTextViewBalance = (TextView) convertView.findViewById(R.id.history_row_month_balance);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        double mD = monthBalance.get(position);
        float mF = (float) mD;

        viewHolder.mTextViewName.setText(monthName.get(position));
        viewHolder.mTextViewBalance.setText(new PieValueFormatter(PreferenceManager.getDefaultSharedPreferences(mContext).getString(MainActivity.KEY_PREF_CURRENCY,"BGN")).getFormattedValue(mF, null, 0, null));

        if (mF > 0){
            viewHolder.mTextViewBalance.setTextColor(mContext.getResources().getColor(R.color.primaryColorDark));
        }
        else{
            viewHolder.mTextViewBalance.setTextColor(mContext.getResources().getColor(R.color.accentColor));
        }
        return convertView;
    }

    public void updateData(List<Long> months, List<String> names, List<Double> balance){
        // update the adapter's dataset
        usefulMonths = months;
        monthName = names;
        monthBalance = balance;
        notifyDataSetChanged();
    }
}
