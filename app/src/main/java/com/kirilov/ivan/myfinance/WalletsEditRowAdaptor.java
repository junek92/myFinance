package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kirilov.ivan.myfinance.firebase_model.Wallet;

import java.util.ArrayList;

/**
 * Created by Ivan on 01-Jun-16.
 */
public class WalletsEditRowAdaptor extends RecyclerView.Adapter<WalletsEditRowAdaptor.RowHolder> {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ArrayList<Wallet> walletsArrayList;
    private TypedArray walletIconsTypedArray;

    public WalletsEditRowAdaptor(Context context, RecyclerView recyclerView, ArrayList<Wallet> walletsArrayList) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.walletsArrayList = walletsArrayList;
        this.walletIconsTypedArray = context.getResources().obtainTypedArray(R.array.walletsIcon);
    }

    public void addWalletToList(Wallet wallet){
        walletsArrayList.add(wallet);
    }

    public void changeWalletFromList(Wallet newWallet){
        long id = newWallet.getWalletId();
        int i = 0;

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

    public void removeWalletFromList(int id){
        int i=0;

        while ((int) walletsArrayList.get(i).getWalletId() != id){
            if (i < walletsArrayList.size()){
                i++;
            } else {
                return;
            }
        }

        walletsArrayList.remove(i);
    }

    public static class RowHolder extends RecyclerView.ViewHolder{
        TextView textViewName;
        ImageView imageViewWalletIcon;
        TextView textViewEdit;
        TextView textViewDelete;

        public RowHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.row_wallet_name);
            this.imageViewWalletIcon = (ImageView) itemView.findViewById(R.id.row_wallet_image);
            this.textViewEdit = (TextView) itemView.findViewById(R.id.row_wallet_edit);
            this.textViewDelete = (TextView) itemView.findViewById(R.id.row_wallet_delete);
        }
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_wallet_icon_name, parent, false);

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int pos = mRecyclerView.getChildAdapterPosition(v);
//                    Toast.makeText(mContext, "Clicked pos: " + pos + " ID: " + walletsArrayList.get(pos).getWalletId(), Toast.LENGTH_LONG ).show();
//            }
//        });

        // set the view's size, margins, paddings and layout parameters - if any
        RowHolder rowHolder = new RowHolder(view);

        return rowHolder;
    }

    @Override
    public void onBindViewHolder(RowHolder holder, final int position) {
        // Replace the contents of a view (invoked by the layout manager)
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textViewName.setText(walletsArrayList.get(position).getWalletName());
        holder.imageViewWalletIcon.setImageDrawable(walletIconsTypedArray.getDrawable((int) walletsArrayList.get(position).getWalletIcon()));

        holder.textViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FirebaseCreateWalletActivity.class);
                intent.putExtra(Constants.EXTRA_WALLET, walletsArrayList.get(position));
                mContext.startActivity(intent);
//                Toast.makeText(mContext, "EDIT pos: " + position + " ID: " + walletsArrayList.get(position).getWalletId(), Toast.LENGTH_LONG ).show();
            }
        });

        holder.textViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "DELETE pos: " + position + " ID: " + walletsArrayList.get(position).getWalletId(), Toast.LENGTH_LONG ).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return walletsArrayList.size();
    }
}