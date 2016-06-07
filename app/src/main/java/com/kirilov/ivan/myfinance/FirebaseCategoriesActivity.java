package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.firebase_model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 03-Jun-16.
 */
public class FirebaseCategoriesActivity extends BaseActivity {
    ArrayList<Category> incomeArrayList, expenseArrayList;

    long maxIncomeId, maxExpenseId;

    AddFloatingActionButton actionBtn;

    ViewPager viewPager;
    TabLayout tabLayout;

    CategoriesFragment categoriesFragment, expenseFragment;

    Context context;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity_categories);

        context = this;
        incomeArrayList = new ArrayList<>();
        expenseArrayList = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.firebase_categories_toolbar);
        setSupportActionBar(toolbar);

        actionBtn = (AddFloatingActionButton) findViewById(R.id.firebase_categories_action);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.firebase_categories_viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        categoriesFragment = new CategoriesFragment();
        adapter.addFragment(categoriesFragment, "Income");

        expenseFragment = new CategoriesFragment();
        adapter.addFragment(expenseFragment, "Expense");

        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.firebase_categories_tab_layout);
        tabLayout.setupWithViewPager(viewPager);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    // position is 0 = INCOME
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.primaryColorDark));
                    }
                    toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.primaryColor));

                    actionBtn.setColorNormal(getResources().getColor(R.color.primaryColor));
                    actionBtn.setColorPressed(getResources().getColor(R.color.primaryColorDark));
                } else {
                    // position is 1 = EXPENSE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.accentColorDark));
                    }
                    toolbar.setBackgroundColor(getResources().getColor(R.color.accentColor));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.accentColor));

                    actionBtn.setColorNormal(getResources().getColor(R.color.accentColor));
                    actionBtn.setColorPressed(getResources().getColor(R.color.accentColorDark));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCategories();
    }

    @Override
    protected void onStop() {
        super.onStop();

        categoriesFragment.clearData();
        expenseFragment.clearData();
    }

    private void fetchCategories(){

        // FETCH INCOME CATEGORIES
        DatabaseReference referenceIncome = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_CATEGORIES_INCOME + "/");

        Query incomeQuery = referenceIncome.orderByKey();
        incomeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        if (child.getValue(Category.class).getCatId() != 0L){
                            categoriesFragment.addData(child.getValue(Category.class));
                            if (maxIncomeId <= Long.valueOf(child.getKey())){
                                maxIncomeId = Long.valueOf(child.getKey());
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });

        // FETCH EXPENSE CATEGORIES
        DatabaseReference referenceExpense = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_CATEGORIES_EXPENSE + "/");

        Query expenseQuery = referenceExpense.orderByKey();
        expenseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        if (child.getValue(Category.class).getCatId() != 0L){
                            expenseFragment.addData(child.getValue(Category.class));
                            if (maxExpenseId <= Long.valueOf(child.getKey())){
                                maxExpenseId = Long.valueOf(child.getKey());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void btnAddNewCategory(View view){
        Category category = new Category();

        switch (tabLayout.getSelectedTabPosition()){
            case 0:
                //its the income tab
                category.setCatId(maxIncomeId + 1);
                category.setCatType(Constants.INCOME_CATEGORY_TYPE);

                category.setCatName("");
                category.setCatIcon(1L);
                category.setCatColor(0L);
                break;
            case 1:
                //its the expense tab
                category.setCatId(maxExpenseId + 1);
                category.setCatType(Constants.EXPENSE_CATEGORY_TYPE);

                category.setCatName("");
                category.setCatIcon(1L);
                category.setCatColor(0L);
                break;
            default:
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                return;
        }
            Log.d("FRAGMENT", " CATEGORY PRE INTENT: -> \n" + "ID: " + category.getCatId()
                    + "\nTYPE: " + category.getCatType());

        Intent intent = new Intent(context, FirebaseCreateCategoryActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY, category);
        startActivity(intent);

            Log.d("FRAGMENT", " Ko " + viewPager.getAdapter().getPageTitle(tabLayout.getSelectedTabPosition()));
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
