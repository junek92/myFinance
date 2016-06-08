package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;

import java.util.ArrayList;

/**
 * Created by Ivan on 03-Jun-16.
 */
public class GridImageNameAdapter extends RecyclerView.Adapter<GridImageNameAdapter.GridHolder>{
    private Context mContext;
    private RecyclerView mRecyclerView;
    // 0 is for INCOME, 1 is for EXPENSE, 3 is for WALLETS
    private int adapterType;
    private int positronToHighlight;

    private TypedArray iconsTypedArray;
    private ArrayList<Wallet> walletsArrayList;
    private ArrayList<Category> categoryArrayList;

    public GridImageNameAdapter(Context context, RecyclerView recyclerView, int adapterType, int initialHighLight) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.adapterType = adapterType;
        this.positronToHighlight = initialHighLight;

        if (adapterType == Constants.INCOME_CATEGORY_TYPE){
            //income
            this.iconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryIncomeIcon);
            this.categoryArrayList = new ArrayList<>();
        } else if (adapterType == Constants.EXPENSE_CATEGORY_TYPE){
            //expense
            this.iconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryExpenseIcon);
            this.categoryArrayList = new ArrayList<>();

        } else {
            // wallets
            this.iconsTypedArray = context.getResources().obtainTypedArray(R.array.walletsIcon);
            this.walletsArrayList = new ArrayList<>();
        }
    }

    public void addWalletToList(Wallet wallet){
        walletsArrayList.add(wallet);
    }

    public void addCategoryToList(Category category){
        categoryArrayList.add(category);
    }

    @Override
    public GridHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        final CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item_icon_name, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mRecyclerView.getChildAdapterPosition(v);
                if (adapterType == 3){
                    //it's a wallet
                    FirebaseIncomeActivity.walletId = walletsArrayList.get(pos).getWalletId();
                    FirebaseExpenseActivity.walletId = walletsArrayList.get(pos).getWalletId();
                } else {
                    //it's a category
                    FirebaseAnalyzeActivity.categoryId = categoryArrayList.get(pos).getCatId();
                    FirebaseIncomeActivity.categoryId = categoryArrayList.get(pos).getCatId();
                    FirebaseExpenseActivity.categoryId = categoryArrayList.get(pos).getCatId();
                }

                positronToHighlight = pos;
                Log.d("HIGHLIGHT ", ""+ positronToHighlight);
                notifyDataSetChanged();
            }
        });

        // set the view's size, margins, paddings and layout parameters - if any
        GridHolder rowHolder = new GridHolder(view);

        return rowHolder;
    }

    @Override
    public void onBindViewHolder(GridHolder holder, int position) {
        // Replace the contents of a view (invoked by the layout manager)
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (adapterType == 3){
            // it's a wallet
            holder.textViewName.setText(walletsArrayList.get(position).getWalletName());
            holder.imageViewWalletIcon.setImageDrawable(iconsTypedArray.getDrawable((int) walletsArrayList.get(position).getWalletIcon()));
        } else {
            // it's a category
            holder.textViewName.setText(categoryArrayList.get(position).getCatName());
            holder.imageViewWalletIcon.setImageDrawable(iconsTypedArray.getDrawable((int) categoryArrayList.get(position).getCatIcon()));
        }

        if (positronToHighlight == position){
            holder.linearLayout.setSelected(true);
//            holder.cardView.setSelected(true);
        } else {
            holder.linearLayout.setSelected(false);
//            holder.cardView.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        if (adapterType == 3){
            return walletsArrayList.size();
        } else {
            return  categoryArrayList.size();
        }
    }

    public static class GridHolder extends RecyclerView.ViewHolder{
        TextView textViewName;
        ImageView imageViewWalletIcon;
        LinearLayout linearLayout;
        CardView cardView;

        public GridHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.grid_name);
            this.imageViewWalletIcon = (ImageView) itemView.findViewById(R.id.grid_icon);
            this.linearLayout = (LinearLayout) itemView.findViewById(R.id.grid_layout);
            this.cardView = (CardView) itemView.findViewById(R.id.grid_card);
        }
    }

}
