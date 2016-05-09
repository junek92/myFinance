package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ivan on 19-May-15.
 */
public class TransDetailsAdapter extends BaseExpandableListAdapter {

    Context context;
    LayoutInflater mLayoutInflater;

    long startTime;
    long endTime;
    List<Category> mCategories;

    public TransDetailsAdapter(Context context, LayoutInflater layoutInflater, List<Category> categories, long startMonth, long endMonth){
        this.context = context;
        this.mLayoutInflater = layoutInflater;
        this.mCategories = categories;
        this.startTime = startMonth;
        this.endTime = endMonth;
    }

    public static class ViewHolderParent{
        public TextView mTextView;
    }

    public static class ViewHolderChild{
        public TextView mTextViewDate;
        public TextView mTextViewAmount;
        public TextView mTextViewDesc;
    }
    @Override
    public int getGroupCount() {
        return mCategories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        long category = mCategories.get(groupPosition).getCatId();
        List<Transaction> transactions = FinanceDbHelper.getInstance(context).getTransByCategoryAndTime(category, startTime, endTime);
        return transactions.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mCategories.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        long category = mCategories.get(groupPosition).getCatId();
        List<Transaction> transactions = FinanceDbHelper.getInstance(context).getTransByCategoryAndTime(category, startTime, endTime);
        return transactions.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mCategories.get(groupPosition).getCatId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        long category = mCategories.get(groupPosition).getCatId();
        List<Transaction> transactions = FinanceDbHelper.getInstance(context).getTransByCategoryAndTime(category, startTime, endTime);

        return transactions.get(childPosition).getTrId();
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
            viewHolderParent.mTextView = (TextView) convertView.findViewById(R.id.parent_text);
            convertView.setTag(viewHolderParent);
        } else {
            viewHolderParent = (ViewHolderParent) convertView.getTag();
        }

        viewHolderParent.mTextView.setText(category.getCatName());
        if (category.getCatType() == FinanceContract.CategoriesEntry.CT_TYPE_DEBIT){
            viewHolderParent.mTextView.setTextColor(context.getResources().getColor(R.color.accentColor));
        } else {
            viewHolderParent.mTextView.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
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

        viewHolderChild.mTextViewDate.setText(FinanceDbHelper.getInstance(context).getDayMonthString(transaction.getAtDate()));
        viewHolderChild.mTextViewDesc.setText(transaction.getDescAdded());
        viewHolderChild.mTextViewAmount.setText(new PieValueFormatter(PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.KEY_PREF_CURRENCY,"BGN")).getFormattedValue((float) transaction.getAmountSpent(), null, 0, null));

        if (transaction.getAmountSpent() > 0){
            viewHolderChild.mTextViewAmount.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
        } else {
            viewHolderChild.mTextViewAmount.setTextColor(context.getResources().getColor(R.color.accentColor));
        }

        return convertView;
    }


}
