package com.kirilov.ivan.myfinance.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kirilov.ivan.myfinance.DetailsActivity;
import com.kirilov.ivan.myfinance.R;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.ArrayList;

/**
 * Created by Ivan on 30-May-16.
 */
public class CardViewWalletAdapter extends RecyclerView.Adapter<CardViewWalletAdapter.CardViewHolder> {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ArrayList<Wallet> walletsArrayList;
    private TypedArray walletIconsTypedArray;

    private long monthBegin;

    public CardViewWalletAdapter(Context context, RecyclerView recyclerView, long monthBegin) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;

        this.walletsArrayList = new ArrayList<>();
        this.walletIconsTypedArray = context.getResources().obtainTypedArray(R.array.walletsIcon);

        this.monthBegin = monthBegin;
    }

    public void addWalletToList(Wallet wallet){
        walletsArrayList.add(wallet);
    }

    public void changeWalletFromList(Wallet newWallet){
        long id = newWallet.getWalletId();
        int i = 0;

        if (walletsArrayList.size() == 0){
            return;
        }

        while (walletsArrayList.get(i).getWalletId() != id){
            if (i < walletsArrayList.size()){
                i++;
            } else {
                return;
            }
        }

        walletsArrayList.get(i).setWalletName(newWallet.getWalletName());
        walletsArrayList.get(i).setWalletCurrency(newWallet.getWalletCurrency());
        walletsArrayList.get(i).setWalletIcon(newWallet.getWalletIcon());
        walletsArrayList.get(i).setWalletCreated(newWallet.getWalletCreated());
        walletsArrayList.get(i).setWalletLastTrans(newWallet.getWalletLastTrans());
        walletsArrayList.get(i).setWalletIncome(newWallet.getWalletIncome());
        walletsArrayList.get(i).setWalletExpenses(newWallet.getWalletExpenses());
        walletsArrayList.get(i).setWalletBalance(newWallet.getWalletBalance());
    }

    public void removeWalletFromList(long id){
        int i=0;

        if (id >= walletsArrayList.size()){
            Log.d("TESTING", "removeWalletFromList ID -> " + id);
            return;
        }

        while (walletsArrayList.get(i).getWalletId() != id){
            if (i < walletsArrayList.size()){
                i++;
            } else {
                return;
            }
        }

        walletsArrayList.remove(i);
    }

    public void removeWalletFromList2(long id){
       try {
           for (int i = 0; i < walletsArrayList.size(); i++){
               if (walletsArrayList.get(i).getWalletId() == id){
                   walletsArrayList.remove(i);
               }
           }
       } catch (IndexOutOfBoundsException e){
           Log.d("TESTING", "IndexOutOfBounds ID:" + id );
       }


    }

    public void clearWalletsData(){
        walletsArrayList.clear();
    }

//    public boolean isWalletAlreadyAdded(Wallet walletToSearch){
//        long id = walletToSearch.getWalletId();
//        int i = 0;
//
//        // if the ArrayList is empty => wallet is not added => false
//        if (walletsArrayList.size() == 0){
//            return false;
//        }
//
//        while (walletsArrayList)
//
//    }

    public static class CardViewHolder extends RecyclerView.ViewHolder{
        TextView textViewName;
        TextView textViewCurrency;
        TextView textViewCreated;
        TextView textViewLastTrans;
        TextView textViewIncome;
        TextView textViewExpenses;
        TextView textViewBalance;
        ImageView imageViewWalletIcon;

        public CardViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.card_view_wallet_name);
            this.textViewCurrency = (TextView) itemView.findViewById(R.id.card_view_wallet_currency);
            this.textViewCreated = (TextView) itemView.findViewById(R.id.card_view_wallet_created);
            this.textViewLastTrans = (TextView) itemView.findViewById(R.id.card_view_wallet_last_transaction);
            this.textViewIncome = (TextView) itemView.findViewById(R.id.card_view_wallet_income_amount);
            this.textViewExpenses = (TextView) itemView.findViewById(R.id.card_view_wallet_expense_amount);
            this.textViewBalance = (TextView) itemView.findViewById(R.id.card_view_wallet_balance);
            this.imageViewWalletIcon = (ImageView) itemView.findViewById(R.id.card_view_wallet_icon);
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_wallet_all, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mRecyclerView.getChildAdapterPosition(v);

                if (walletsArrayList.get(pos).getWalletIncome() == 0d && walletsArrayList.get(pos).getWalletExpenses() == 0d){
                    Toast.makeText(mContext, "No details to show.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(mContext, DetailsActivity.class);
                    intent.putExtra(Constants.EXTRA_WALLET, walletsArrayList.get(pos));
                    intent.putExtra(Constants.EXTRA_DATE, monthBegin);
                    mContext.startActivity(intent);
                }
            }
        });
        // set the view's size, margins, paddings and layout parameters - if any
        CardViewHolder cardViewHolder = new CardViewHolder(view);

        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        // Replace the contents of a view (invoked by the layout manager)
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.textViewName.setText(walletsArrayList.get(position).getWalletName());
        holder.textViewCurrency.setText(PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.KEY_PREF_CURRENCY, "USD"));
        if (walletsArrayList.get(position).getWalletCreated() != -1L){
            holder.textViewCreated.setText(String.format(mContext.getResources().getString(R.string.main_screen_wallet_created), Utilities.getTimeInString(walletsArrayList.get(position).getWalletCreated(), false)));
        }
        holder.textViewLastTrans.setText(String.format(mContext.getResources().getString(R.string.main_screen_wallet_since), Utilities.getTimeInString(walletsArrayList.get(position).getWalletLastTrans(), false)));
        holder.textViewIncome.setText(Utilities.getFormattedAmount(walletsArrayList.get(position).getWalletIncome(), false, mContext));
        holder.textViewExpenses.setText(Utilities.getFormattedAmount(walletsArrayList.get(position).getWalletExpenses(), false, mContext));
        holder.textViewBalance.setText(Utilities.getFormattedAmount(walletsArrayList.get(position).getWalletBalance(), false, mContext));
        holder.imageViewWalletIcon.setImageDrawable(walletIconsTypedArray.getDrawable((int) walletsArrayList.get(position).getWalletIcon()));

        if (walletsArrayList.get(position).getWalletBalance() >= 0d){
            holder.textViewBalance.setTextColor(mContext.getResources().getColor(R.color.primaryColorDark));
        } else {
            holder.textViewBalance.setTextColor(mContext.getResources().getColor(R.color.accentColor));
        }
    }

    @Override
    public int getItemCount() {
        return walletsArrayList.size();
    }





}
