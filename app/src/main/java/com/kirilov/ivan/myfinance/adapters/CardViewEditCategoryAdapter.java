package com.kirilov.ivan.myfinance.adapters;

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

import com.kirilov.ivan.myfinance.CreateCategoryActivity;
import com.kirilov.ivan.myfinance.R;
import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.myExtras.Constants;

import java.util.ArrayList;

/**
 * Created by Ivan on 04-Jun-16.
 */
public class CardViewEditCategoryAdapter extends RecyclerView.Adapter<CardViewEditCategoryAdapter.RowHolder>{
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ArrayList<Category> categoryArrayList;
    private TypedArray categoryIncomeIconsTypedArray, categoryExpenseIconsTypedArray;


    public CardViewEditCategoryAdapter(Context context, RecyclerView recyclerView) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;

        this.categoryArrayList = new ArrayList<>();

        this.categoryIncomeIconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryIncomeIcon);
        this.categoryExpenseIconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryExpenseIcon);
    }

    public void addCategoryToList(Category category){
        categoryArrayList.add(category);
    }

    public void clearTheList(){
        categoryArrayList.clear();
        notifyDataSetChanged();
    }

    public void removeCategoryFromList(int id){
        int i=0;

        while ((int) categoryArrayList.get(i).getCatId() != id){
            if (i < categoryArrayList.size()){
                i++;
            } else {
                return;
            }
        }

        categoryArrayList.remove(i);
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_edit_icon_name, parent, false);

        // set the view's size, margins, paddings and layout parameters - if any
        RowHolder rowHolder = new RowHolder(view);

        return rowHolder;
    }

    @Override
    public void onBindViewHolder(RowHolder holder, final int position) {
        // Replace the contents of a view (invoked by the layout manager)
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textViewName.setText(categoryArrayList.get(position).getCatName());

        if (categoryArrayList.get(position).getCatType() == Constants.INCOME_CATEGORY_TYPE){
            holder.imageViewWalletIcon.setImageDrawable(categoryIncomeIconsTypedArray.getDrawable((int) categoryArrayList.get(position).getCatIcon()));
        } else {
            holder.imageViewWalletIcon.setImageDrawable(categoryExpenseIconsTypedArray.getDrawable((int) categoryArrayList.get(position).getCatIcon()));
        }

        holder.textViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CreateCategoryActivity.class);
                intent.putExtra(Constants.EXTRA_CATEGORY, categoryArrayList.get(position));
                mContext.startActivity(intent);

//                Toast.makeText(mContext, "EDIT pos: " + position + " ID: " + categoryArrayList.get(position).getCatId(), Toast.LENGTH_LONG ).show();
            }
        });
        // TODO: 13-Jul-16 Implement delete category
//        holder.textViewDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(mContext, "DELETE pos: " + position + " ID: " + categoryArrayList.get(position).getCatId(), Toast.LENGTH_LONG ).show();
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    public static class RowHolder extends RecyclerView.ViewHolder{
        TextView textViewName;
        ImageView imageViewWalletIcon;
        TextView textViewEdit;
//        TextView textViewDelete;

        public RowHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.row_wallet_name);
            this.imageViewWalletIcon = (ImageView) itemView.findViewById(R.id.row_wallet_image);
            this.textViewEdit = (TextView) itemView.findViewById(R.id.row_wallet_edit);
//            this.textViewDelete = (TextView) itemView.findViewById(R.id.row_wallet_delete);
        }
    }
}

