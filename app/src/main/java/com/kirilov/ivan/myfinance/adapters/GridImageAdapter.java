package com.kirilov.ivan.myfinance.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kirilov.ivan.myfinance.CreateCategoryActivity;
import com.kirilov.ivan.myfinance.CreateWalletActivity;
import com.kirilov.ivan.myfinance.R;
import com.kirilov.ivan.myfinance.myExtras.Constants;

/**
 * Created by Ivan on 04-Jun-16.
 */
public class GridImageAdapter extends RecyclerView.Adapter<GridImageAdapter.ImageHolder> {
    private Context mContext;
    private RecyclerView mRecyclerView;
    private TypedArray incomeIconsTypedArray, expenseIconsTypedArray, walletsIconTypedArray;

    private int positronToHighlight;
    private long categoryType;

    public GridImageAdapter(Context context, RecyclerView recyclerView, int initialHighLight, long categoryType){
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.positronToHighlight = initialHighLight;
        this.categoryType = categoryType;

        this.incomeIconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryIncomeIcon);
        this.expenseIconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryExpenseIcon);
    }

    public GridImageAdapter(Context context, RecyclerView recyclerView, int initialHighLight){
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.positronToHighlight = initialHighLight;
        // this constructor is only used to create adapter for WALLETS icons
        this.categoryType = 3;

        this.walletsIconTypedArray = context.getResources().obtainTypedArray(R.array.walletsIcon);

    }


    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        final CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item_icon, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mRecyclerView.getChildAdapterPosition(v);

                if (categoryType == 3){
                    // wallets icons are needed
                    CreateWalletActivity.walletIconId = pos + 1;
                } else {
                    // category icons are needed
                    CreateCategoryActivity.categoryIconId = pos + 1;
                }

                positronToHighlight = pos;
                notifyDataSetChanged();
            }
        });

        // set the view's size, margins, paddings and layout parameters - if any
        ImageHolder imageHolder = new ImageHolder(view);

        return imageHolder;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        // Replace the contents of a view (invoked by the layout manager)
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        // add +1 to position, in order to skip the first empty drawable
        // to prevent index out of bounds error -> subtracted -1 from length

        if (categoryType == 3){
            // wallets icons are needed
            holder.imageViewCategoryIcon.setImageDrawable(walletsIconTypedArray.getDrawable(position + 1));
        } else if (categoryType == Constants.INCOME_CATEGORY_TYPE){
            // income icons are needed
            holder.imageViewCategoryIcon.setImageDrawable(incomeIconsTypedArray.getDrawable(position + 1));
        } else {
            // expense icons are needed
            holder.imageViewCategoryIcon.setImageDrawable(expenseIconsTypedArray.getDrawable(position + 1));
        }

        if (positronToHighlight == position){
            holder.linearLayout.setSelected(true);
        } else {
            holder.linearLayout.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        // subtracting 1, in order to be able to skip the FIRST icon in each array, because it's only for Transfer TO/FROM category
        // same for wallets

        if (categoryType == 3){
            // wallets icons are needed
            return walletsIconTypedArray.length() -1;
        } else if (categoryType == Constants.INCOME_CATEGORY_TYPE){
            // income icons are needed
            return incomeIconsTypedArray.length() - 1;
        } else {
            // expense icons are needed
            return expenseIconsTypedArray.length() - 1;
        }
    }

    public class ImageHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCategoryIcon;
        LinearLayout linearLayout;

        public ImageHolder(View itemView) {
            super(itemView);

            this.imageViewCategoryIcon = (ImageView) itemView.findViewById(R.id.grid_icon);
            this.linearLayout = (LinearLayout) itemView.findViewById(R.id.grid_layout);
        }
    }
}
