package com.kirilov.ivan.myfinance.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kirilov.ivan.myfinance.MonthActivity;
import com.kirilov.ivan.myfinance.R;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.ArrayList;

/**
 * Created by Ivan on 02-Jun-16.
 */
public class RowHistoryAdapter extends RecyclerView.Adapter<RowHistoryAdapter.RowHolder>{

    private Context mContext;
    private RecyclerView mRecyclerView;
    private ArrayList<String> walletsMonth;
    private ArrayList<Double> walletsBalance;

    public RowHistoryAdapter(Context context, RecyclerView recyclerView, ArrayList<String> walletsMonth, ArrayList<Double> walletsBalance) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.walletsMonth = walletsMonth;
        this.walletsBalance = walletsBalance;
    }

    public void replaceCurrentData(ArrayList<String> walletsMonth, ArrayList<Double> walletsBalance){
        this.walletsMonth = walletsMonth;
        this.walletsBalance = walletsBalance;
        notifyDataSetChanged();
    }


    public static class RowHolder extends RecyclerView.ViewHolder{
        TextView textViewMonth;
        TextView textViewBalance;

        public RowHolder(View itemView) {
            super(itemView);
            this.textViewMonth = (TextView) itemView.findViewById(R.id.history_row_month_name);
            this.textViewBalance = (TextView) itemView.findViewById(R.id.history_row_month_balance);
        }
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_history_months, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mRecyclerView.getChildAdapterPosition(v);
                Intent intent = new Intent(mContext, MonthActivity.class);
                intent.putExtra(Constants.EXTRA_MONTH_BEGIN, walletsMonth.get(pos));
                mContext.startActivity(intent);

                    Log.d(Constants.LOG_NAVIGATION, "HISTORY ON CLICK -> intent for month: " + walletsMonth.get(pos));
            }
        });

        // set the view's size, margins, paddings and layout parameters - if any
        RowHolder rowHolder = new RowHolder(view);

        return rowHolder;
    }

    @Override
    public void onBindViewHolder(RowHolder holder, int position) {
        holder.textViewMonth.setText(Utilities.getTimeInString(Long.valueOf(walletsMonth.get(position)), true));
        holder.textViewBalance.setText(Utilities.getFormattedAmount(walletsBalance.get(position), true, mContext));

        if (walletsBalance.get(position) >= 0){
            holder.textViewBalance.setTextColor(mContext.getResources().getColor(R.color.primaryColorDark));
        }
        else{
            holder.textViewBalance.setTextColor(mContext.getResources().getColor(R.color.accentColor));
        }
    }

    @Override
    public int getItemCount() {
        return walletsMonth.size();
    }
}
