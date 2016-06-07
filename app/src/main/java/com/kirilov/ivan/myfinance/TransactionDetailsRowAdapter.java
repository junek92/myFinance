package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 19-May-15.
 */
public class TransactionDetailsRowAdapter extends BaseExpandableListAdapter {

    Context context;
    LayoutInflater mLayoutInflater;

    // KEY = cat_id
    Map<Long, ArrayList<Transaction>> incomeHashMap;
    Map<Long, Double> categorySumHashMap;
    ArrayList<Category> incomeAllCategories;
    ArrayList<Long> incomeUsefulCategoriesIds;

    // icon arrays
    private TypedArray incomeIconsTypedArray, expenseIconsTypedArray;

    public void calculateCatSum(Long catId, Double amount){
        categorySumHashMap.put(catId, categorySumHashMap.get(catId) + amount);
    }

    public void addIncomeCat(Category category){
        incomeAllCategories.add(category);
       // notifyDataSetChanged();
    }

    public void addUsefulCategory(Long catId){
       if (!incomeHashMap.containsKey(catId)){
           incomeHashMap.put(catId, new ArrayList<Transaction>());
           categorySumHashMap.put(catId, 0d);
           incomeUsefulCategoriesIds.add(catId);
           notifyDataSetChanged();
       }
    }

    public void addNewTransaction(Transaction transaction){
        incomeHashMap.get(transaction.getTrCat()).add(transaction);
        calculateCatSum(transaction.getTrCat(), transaction.getTrAmount());
        notifyDataSetChanged();
    }

    public void clearAllData(){
        incomeAllCategories.clear();
        incomeUsefulCategoriesIds.clear();
        incomeHashMap.clear();
        notifyDataSetChanged();
    }

    public TransactionDetailsRowAdapter(Context context, LayoutInflater layoutInflater){
        this.context = context;
        this.mLayoutInflater = layoutInflater;

        this.incomeHashMap = new HashMap<>();
        this.categorySumHashMap = new HashMap<>();
        this.incomeAllCategories = new ArrayList<>();
        this.incomeUsefulCategoriesIds = new ArrayList<>();

        this.incomeIconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryIncomeIcon);
        this.expenseIconsTypedArray = context.getResources().obtainTypedArray(R.array.categoryExpenseIcon);
    }

    public static class ViewHolderParent{
        public TextView mTextName;
        public TextView mTextSum;
        public ImageView mImageIcon;
    }

    public static class ViewHolderChild{
        public TextView mTextViewDate;
        public TextView mTextViewAmount;
        public TextView mTextViewDesc;
    }

    @Override
    public int getGroupCount() {
        // number of HashMap keys = number of categories to show = number of group views
        return incomeUsefulCategoriesIds.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
//        incomeHashMap.get(incomeUsefulCategoriesIds.get(groupPosition)).size();
//
//        long category = mCategories.get(groupPosition).getCatId();
//        List<Transaction> transactions = FinanceDbHelper.getInstance(context).getTransByCategoryAndTime(category, startTime, endTime);

        // find needed key in USEFULCATIDS and then get the ARRAYLIST.SIZE associated with this key
        return incomeHashMap.get(incomeUsefulCategoriesIds.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // when called it should return CATEGORY
        // first get the CATEGORY ID in incomeUsefulCategoriesIds

        for (int i = 0; i< incomeAllCategories.size(); i++){
            if (incomeAllCategories.get(i).getCatId() == incomeUsefulCategoriesIds.get(groupPosition)){
                return incomeAllCategories.get(i);
            }
        }

        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

//        long category = mCategories.get(groupPosition).getCatId();
//        List<Transaction> transactions = FinanceDbHelper.getInstance(context).getTransByCategoryAndTime(category, startTime, endTime);
//        return transactions.get(childPosition);

        return incomeHashMap.get(incomeUsefulCategoriesIds.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return incomeUsefulCategoriesIds.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return incomeHashMap.get(incomeUsefulCategoriesIds.get(groupPosition)).get(childPosition).getTrDate();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderParent viewHolderParent;
        Category category = (Category) getGroup(groupPosition);

        if (convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.detail_parent, parent, false);
            viewHolderParent = new ViewHolderParent();

            viewHolderParent.mTextName = (TextView) convertView.findViewById(R.id.parent_text);
            viewHolderParent.mTextSum = (TextView) convertView.findViewById(R.id.parent_sum);
            viewHolderParent.mImageIcon = (ImageView) convertView.findViewById(R.id.parent_icon);

            convertView.setTag(viewHolderParent);
        } else {
            viewHolderParent = (ViewHolderParent) convertView.getTag();
        }

        viewHolderParent.mTextName.setText(category.getCatName());
        viewHolderParent.mTextSum.setText(Utilities.getFormattedAmount(categorySumHashMap.get(category.getCatId()), true, context));

        if (category.getCatType() == Constants.EXPENSE_CATEGORY_TYPE){
            viewHolderParent.mTextName.setTextColor(context.getResources().getColor(R.color.accentColor));
            viewHolderParent.mTextSum.setTextColor(context.getResources().getColor(R.color.accentColor));
            viewHolderParent.mImageIcon.setImageDrawable(expenseIconsTypedArray.getDrawable((int) category.getCatIcon()));
        } else {
            viewHolderParent.mTextName.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
            viewHolderParent.mTextSum.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
            viewHolderParent.mImageIcon.setImageDrawable(incomeIconsTypedArray.getDrawable((int) category.getCatIcon()));
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderChild viewHolderChild;
        Transaction transaction = (Transaction) getChild(groupPosition, childPosition);

        if (convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.detail_child, parent, false);
            viewHolderChild = new ViewHolderChild();
            viewHolderChild.mTextViewDate = (TextView) convertView.findViewById(R.id.child_date);
            viewHolderChild.mTextViewDesc = (TextView) convertView.findViewById(R.id.child_desc);
            viewHolderChild.mTextViewAmount = (TextView) convertView.findViewById(R.id.child_amount);

            convertView.setTag(viewHolderChild);
        } else {
            viewHolderChild = (ViewHolderChild) convertView.getTag();
        }

        viewHolderChild.mTextViewDate.setText(Utilities.getDayMonthString(transaction.getTrDate()));
        viewHolderChild.mTextViewDesc.setText(transaction.getTrDesc());
        viewHolderChild.mTextViewAmount.setText(Utilities.getFormattedAmount(transaction.getTrAmount(), true, context));

        if (transaction.getTrAmount() > 0){
            viewHolderChild.mTextViewAmount.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
        } else {
            viewHolderChild.mTextViewAmount.setTextColor(context.getResources().getColor(R.color.accentColor));
        }

        return convertView;
    }
}
