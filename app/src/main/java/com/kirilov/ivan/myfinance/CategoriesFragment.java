package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.kirilov.ivan.myfinance.firebase_model.Category;

import java.util.ArrayList;

/**
 * Created by Ivan on 04-Jun-16.
 */
public class CategoriesFragment extends Fragment {

    ArrayList<Category> categoryArrayList;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    CategoryEditRowAdapter mAdapter;

    Context context;

    public CategoriesFragment(){
        // Required empty public constructor

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mAdapter = new CategoryEditRowAdapter(context, recyclerView);

//        Bundle bundle = getArguments();
//        categoryArrayList = bundle.getParcelableArrayList("CAT_ARRAY");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_category_page, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_recycle_view);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);


        recyclerView.setAdapter(mAdapter);

        return view;
    }

    public void addData(Category category){
        mAdapter.addCategoryToList(category);
        // specify an adapter
        mAdapter.notifyDataSetChanged();
    }

    public void clearData(){
        mAdapter.clearTheList();
    }
}
