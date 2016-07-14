package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.fragments.DetailsFragment;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Ivan on 19-May-15.
 */
public class DetailsActivity extends BaseActivity {
    //HashMap<Category, List<Transaction>> usedCategoriesMap;

    ViewPager detailsViewPager;
    TabLayout detailsTabLayout;

    List<Category> allCategories, usefulCat;
    List<Transaction> allTrans;

    DetailsFragment incomeFragment, expenseFragment;


    private AddFloatingActionButton fabAddTransaction;

    Context context;
    Toolbar toolbar;

    DatabaseReference refIncomeCatInfo, refIncomeCatTransactions,
            refExpenseCatInfo, refExpenseCatTransactions,
            refWalletData;

    ValueEventListener listenerWalletData;

    private AppCompatTextView textViewCreated, textViewIncome, textViewExpense, textViewBalance;

    private long beginOfMonthInMs;
    private Wallet extraWallet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        context = this;

        // extract intent extras to get WALLET and DATE
        Intent intent = getIntent();
        if (!(intent.hasExtra(Constants.EXTRA_WALLET) && intent.hasExtra(Constants.EXTRA_DATE))){
                Log.d(Constants.LOG_APP_ERROR, " DETAILS ACTIVITY: NO EXTRA WALLET/DATE");
            finish();
            return;
        }

        beginOfMonthInMs = intent.getLongExtra(Constants.EXTRA_DATE, 0L);
        extraWallet = intent.getParcelableExtra(Constants.EXTRA_WALLET);

        textViewCreated = (AppCompatTextView) findViewById(R.id.details_wallet_created);
        textViewIncome = (AppCompatTextView) findViewById(R.id.details_wallet_income);
        textViewExpense = (AppCompatTextView) findViewById(R.id.details_wallet_expense);
        textViewBalance = (AppCompatTextView) findViewById(R.id.details_wallet_balance);

        allCategories = new ArrayList<>();
        usefulCat = new ArrayList<>();
        allTrans = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.details_toolbar);
        toolbar.setTitle(extraWallet.getWalletName());
        setSupportActionBar(toolbar);

        detailsViewPager = (ViewPager) findViewById(R.id.details_viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        incomeFragment = new DetailsFragment();
        incomeFragment.addTextCaption("NO INCOME", getResources().getColor(R.color.primaryColorDark));
        adapter.addFragment(incomeFragment, "INCOME");



        expenseFragment = new DetailsFragment();
        expenseFragment.addTextCaption("NO EXPENSES", getResources().getColor(R.color.accentColorDark));
        adapter.addFragment(expenseFragment, "EXPENSE");

        detailsViewPager.setAdapter(adapter);

        fabAddTransaction = (AddFloatingActionButton) findViewById(R.id.details_action_btn);

        detailsTabLayout = (TabLayout) findViewById(R.id.details_tab_layout);
        detailsTabLayout.setupWithViewPager(detailsViewPager);

        detailsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    // position is 0 = INCOME
                    fabAddTransaction.setColorNormal(getResources().getColor(R.color.primaryColor));
                    fabAddTransaction.setColorPressed(getResources().getColor(R.color.primaryColorDark));
                } else {
                    // position is 1 = EXPENSE
                    fabAddTransaction.setColorNormal(getResources().getColor(R.color.accentColor));
                    fabAddTransaction.setColorPressed(getResources().getColor(R.color.accentColorDark));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        inflateWalletData();

//        incomeExpListView = (ExpandableListView) findViewById(R.id.details_expand_income);
//        expenseExpListView = (ExpandableListView) findViewById(R.id.details_expand_expense);
//
//        incomeAdapter = new DetailsExpandListAdapter(context, getLayoutInflater());
//        expenseAdapter = new DetailsExpandListAdapter(context, getLayoutInflater());
//
//        incomeExpListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                Intent intent = new Intent(context, IncomeActivity.class);
//                intent.putExtra(Constants.EXTRA_TRANSACTION, (Transaction) incomeAdapter.getChild(groupPosition, childPosition));
//                startActivity(intent);
//                return false;
//            }
//        });
//
//        expenseExpListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                Intent intent = new Intent(context, ExpenseActivity.class);
//                intent.putExtra(Constants.EXTRA_TRANSACTION, (Transaction) expenseAdapter.getChild(groupPosition, childPosition));
//                startActivity(intent);
//                return false;
//            }
//        });
//
//        incomeExpListView.setAdapter(incomeAdapter);
//        expenseExpListView.setAdapter(expenseAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        inflateTransactionsData();
    }

    @Override
    protected void onDestroy() {
        if (listenerWalletData != null){
            refWalletData.removeEventListener(listenerWalletData);
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        incomeFragment.clearAllData();
        expenseFragment.clearAllData();
    }

    public void inflateTransactionsData(){
        // get all INCOME categories
        refIncomeCatInfo = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_CATEGORIES_INCOME + "/");

        // userTrans/EMAIL/WALLET_ID/INCOME_TRANSACTIONS
        refIncomeCatTransactions = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + extraWallet.getWalletId() + "/"
                + Constants.INCOME_TRANSACTION_TYPE + "/");

        // get all EXPENSE categories
        refExpenseCatInfo = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_CATEGORIES_EXPENSE + "/");

        // userTrans/EMAIL/WALLET_ID/EXPENSE_TRANSACTIONS
        refExpenseCatTransactions = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + extraWallet.getWalletId() + "/"
                + Constants.EXPENSE_TRANSACTION_TYPE + "/");

        refIncomeCatInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()){
                        incomeFragment.addIncomeCat(dataSnapshotChild.getValue(Category.class));

                        Query query = refIncomeCatTransactions.child(dataSnapshotChild.getKey()).child(Long.toString(beginOfMonthInMs)).orderByKey();
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()){
                                    for (DataSnapshot child : dataSnapshot.getChildren()){
                                        incomeFragment.addUsefulCategory(child.getValue(Transaction.class).getTrCat());
                                        incomeFragment.addNewTransaction(child.getValue(Transaction.class));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refExpenseCatInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                        expenseFragment.addIncomeCat(dataSnapshotChild.getValue(Category.class));

                        Query query = refExpenseCatTransactions.child(dataSnapshotChild.getKey()).child(Long.toString(beginOfMonthInMs)).orderByKey();
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()){
                                    for (DataSnapshot child : dataSnapshot.getChildren()){
                                        expenseFragment.addUsefulCategory(child.getValue(Transaction.class).getTrCat());
                                        expenseFragment.addNewTransaction(child.getValue(Transaction.class));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//
//        incomeExpListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//                return false;
//            }
//        });

//        if (chosenCat != -1) {
//            int groupToExpand = 0;
//            for (int i = 0; i < usefulCategories.size(); i++) {
//                if (chosenCat == usefulCategories.get(i).getCatId()) {
//                    groupToExpand = i;
//                    break;
//                }
//            }
//            try {
//                incomeExpListView.expandGroup(groupToExpand);
//            } catch (IndexOutOfBoundsException e){
//                Log.d("EXCEPTION:", "IndexOutOfBounts at DetailsActivity");
//            }
//
//        }
    }

    public void inflateWalletData(){
        if (beginOfMonthInMs != Utilities.calculateBeggingOfCurrentMonth()){
            // HISTORY node
            refWalletData = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                    + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                    + extraWallet.getWalletId() + "/"
                    + beginOfMonthInMs + "/");
        } else {
            // CURRENT mode
            refWalletData = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                    + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                    + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
                    + extraWallet.getWalletId() + "/");
        }

        listenerWalletData = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                    if (dummyWallet.getWalletIncome() == 0d && dummyWallet.getWalletExpenses() == 0d){
                        finish();
                    } else {
                        if (dummyWallet.getWalletCreated() == -1L){
                            textViewCreated.setText("");
                        } else {
                            textViewCreated.setText(String.format(getResources().getString(R.string.details_wallet_created), Utilities.getTimeInString(dummyWallet.getWalletCreated(), false)));
                        }
                        textViewIncome.setText(Utilities.getFormattedAmount(dummyWallet.getWalletIncome(), false, context));
                        textViewExpense.setText(Utilities.getFormattedAmount(dummyWallet.getWalletExpenses(), false, context));
                        if (dummyWallet.getWalletBalance() < 0d){
                            textViewBalance.setTextColor(getResources().getColor(R.color.accentColor));
                        } else {
                            textViewBalance.setTextColor(getResources().getColor(R.color.myWhite));
                        }
                        textViewBalance.setText(Utilities.getFormattedAmount(dummyWallet.getWalletBalance(), false, context));
                    }
                } else {

                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        refWalletData.addValueEventListener(listenerWalletData);
    }

    public void btnAddNewTransaction(View view){
        long walletId = 1L;
        long dateOfTransaction;

        if (extraWallet.getWalletId() != 0L){
            walletId = extraWallet.getWalletId();
        }

//        if (beginOfMonthInMs != Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis()){
//            dateOfTransaction = beginOfMonthInMs;
//        } else {
            dateOfTransaction = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis();
//        }

//        Calendar.getInstance();


        switch (detailsTabLayout.getSelectedTabPosition()){
            case 0:
                //its the income tab
                Transaction incomeTransaction = new Transaction(dateOfTransaction,
                        dateOfTransaction,
                        walletId,
                        Constants.INCOME_TRANSACTION_TYPE,
                        1L,
                        0d,
                        PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD"),
                        "" );

                Intent incomeIntent = new Intent(context, IncomeActivity.class);
                incomeIntent.putExtra(Constants.EXTRA_TRANSACTION, incomeTransaction);
                startActivity(incomeIntent);
                break;
            case 1:
                //its the expense tab
                Transaction expenseTransaction = new Transaction(dateOfTransaction,
                        dateOfTransaction,
                        walletId,
                        Constants.EXPENSE_TRANSACTION_TYPE,
                        1L,
                        0d,
                        PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD"),
                        "" );

                Intent expenseIntent = new Intent(context, ExpenseActivity.class);
                expenseIntent.putExtra(Constants.EXTRA_TRANSACTION, expenseTransaction);
                startActivity(expenseIntent);
                break;
            default:
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                return;
        }
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
