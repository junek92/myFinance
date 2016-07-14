package com.kirilov.ivan.myfinance.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kirilov.ivan.myfinance.ExpenseActivity;
import com.kirilov.ivan.myfinance.IncomeActivity;
import com.kirilov.ivan.myfinance.R;
import com.kirilov.ivan.myfinance.adapters.DetailsExpandListAdapter;
import com.kirilov.ivan.myfinance.TransferActivity;
import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.myExtras.Constants;

/**
 * Created by Ivan on 04-Jun-16.
 */
public class DetailsFragment extends Fragment {

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;


    Context context;

    TextView noDataTextView;
    ExpandableListView expandableListView;
    DetailsExpandListAdapter mAdapter;

    String textViewText;
    int textViewColor;

    public DetailsFragment(){
        // Required empty public constructor

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        mAdapter = new DetailsExpandListAdapter(context, getActivity().getLayoutInflater());



//        Bundle bundle = getArguments();
//        categoryArrayList = bundle.getParcelableArrayList("CAT_ARRAY");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_details_page, container, false);

        expandableListView = (ExpandableListView) view.findViewById(R.id.fragment_expandable_view);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Transaction transaction = (Transaction) mAdapter.getChild(groupPosition, childPosition);

                if (transaction.getTrCat() == 0L){
                    Intent intent = new Intent(context, TransferActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANSACTION, transaction);
                    startActivity(intent);
                } else if (transaction.getTrType() == Constants.INCOME_TRANSACTION_TYPE){
                    Intent intent = new Intent(context, IncomeActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANSACTION, transaction);
                    startActivity(intent);
                } else if (transaction.getTrType() == Constants.EXPENSE_TRANSACTION_TYPE){
                    Intent intent = new Intent(context, ExpenseActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANSACTION, transaction);
                    startActivity(intent);
                }
                return false;
            }
        });

        expandableListView.setAdapter(mAdapter);
        expandableListView.setVisibility(View.GONE);

        noDataTextView = (TextView) view.findViewById(R.id.fragment_details_no_data);
        noDataTextView.setText(textViewText);
        noDataTextView.setTextColor(textViewColor);
        noDataTextView.setVisibility(View.VISIBLE);

        return view;
    }

    public void addIncomeCat(Category category){
        mAdapter.addIncomeCat(category);
    }

    public void addUsefulCategory(Long catId){
        mAdapter.addUsefulCategory(catId);

        if (expandableListView.getVisibility() == View.GONE){
            expandableListView.setVisibility(View.VISIBLE);
        }

        if (noDataTextView.getVisibility() == View.VISIBLE){
            noDataTextView.setVisibility(View.GONE);
        }
    }

    public void addNewTransaction(Transaction transaction){
        mAdapter.addNewTransaction(transaction);
    }

    public void clearAllData(){
        mAdapter.clearAllData();

        if (expandableListView.getVisibility() == View.VISIBLE){
            expandableListView.setVisibility(View.GONE);
        }

        if (noDataTextView.getVisibility() == View.GONE){
            noDataTextView.setVisibility(View.VISIBLE);
        }
    }

    public void addTextCaption(String string, int color){
        this.textViewText = string;
        this.textViewColor = color;
    }
}
