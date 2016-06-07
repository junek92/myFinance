package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Ivan on 02-Jun-16.
 */
public class FirebaseMonthActivity extends BaseActivity {
    private DatabaseReference refUserInfoUserWallets, refWalletsHistoryEmail;
    private Query queryUserInfoUserWallets;

    private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton fabExpense, fabIncome, fabTransfer;

    private ProgressBar progressBar;
    private TextView textViewDate;

    private RecyclerView recyclerView;
    private WalletsCardViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Wallet> walletsArrayList;

    private long monthFirstDate;
    private long currentWallet;
    private int currentPosition;
    private Wallet walletToCheck;
    private boolean isFirstCheckReady;

    private Context context;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity_month);

        context = this;
        walletsArrayList = new ArrayList<>();
        walletToCheck = new Wallet();

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.EXTRA_MONTH_BEGIN)){
            monthFirstDate = Long.valueOf(intent.getStringExtra(Constants.EXTRA_MONTH_BEGIN));
        } else {
            finish();
                Log.d(Constants.LOG_NAVIGATION, "FIREBASE MONTH: -> NO EXTRA IN THE INTENT !!");
        }

        toolbar = (Toolbar) findViewById(R.id.firebase_month_toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.firebase_month_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        textViewDate = (TextView) findViewById(R.id.firebase_month_current_date);

        setupFloatingActionMenu();

        recyclerView = (RecyclerView) findViewById(R.id.firebase_month_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new WalletsCardViewAdapter(context, recyclerView, walletsArrayList, monthFirstDate);
        recyclerView.setAdapter(mAdapter);



        // GET REFERENCE TO -> userInfo/EMAIL/userWallets -> to get basic info for all available user wallets
        refUserInfoUserWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/");
        queryUserInfoUserWallets = refUserInfoUserWallets.orderByKey();

        // GET REFERENCE TO -> userWalletsHistory/EMAIL/ -> to be able to check for any available history OR to add wallets in history when new month comes
        refWalletsHistoryEmail = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/");



    }

    @Override
    protected void onResume() {
        super.onResume();

        isFirstCheckReady = false;
        monthActivityFetchData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.clearWalletsData();
    }

    private void monthActivityFetchData(){

//        queryUserInfoUserWallets.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                //get all current information for every available wallet
//                for (DataSnapshot child : dataSnapshot.getChildren()){
////
////                    currentWallet = child.getValue(Wallet.class).getWalletId();
////                    Log.d("SERIOUS DEBUG", " queryUserInfoUserWallets -> CURRENT WALLET -> " + currentWallet);
//
//                    //walletsArrayList.add(child.getValue(Wallet.class));
//                    walletToCheck = child.getValue(Wallet.class);
//                    mAdapter.addWalletToList(walletToCheck);
//
//
////                    walletsArrayList.add(walletToCheck);
//
//                    Log.d("SERIOUS DEBUG", " queryUserInfoUserWallets -> WALLET BEFORE CHECK -> "
//                            + " ID: " + walletToCheck.getWalletId() + "\t"
//                            + " Name: " + walletToCheck.getWalletName() + "\t"
//                            + " Currency: " + walletToCheck.getWalletCurrency() + "\t"
//                            + " Icon: " + walletToCheck.getWalletIcon() + "\t"
//                            + " Created: " + walletToCheck.getWalletCreated() + "\t"
//                            + " LastTrans: " + walletToCheck.getWalletLastTrans() + "\t"
//                            + " Income: " + walletToCheck.getWalletIncome() + "\t"
//                            + " Expense: " + walletToCheck.getWalletExpenses() + "\t"
//                            + " Balance: " + walletToCheck.getWalletBalance() + "\t"
//                            + " CarryOver: " + walletToCheck.getWalletCarryOver() + "\t");
//
//
//
//
//
//
//
//                }
//                Log.d("SERIOUS DEBUG", " queryUserInfoUserWallets -> WALLET TO CHECK SIZE -> " + walletsArrayList.size());
//
//                Log.d("SERIOUS DEBUG", " queryUserInfoUserWallets -> WALLET TO CHECK before -> " + isFirstCheckReady);
//
//                isFirstCheckReady = true;
//
//                Log.d("SERIOUS DEBUG", " queryUserInfoUserWallets -> WALLET TO CHECK after -> " + isFirstCheckReady);
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        for (int i = 0; i<=6; i++){
            //create new reference in WALLETHISTORY node, to check if there is any information about this wallet at given moment in time
            refWalletsHistoryEmail
                    .child(Integer.toString(i))
                    .child(String.valueOf(monthFirstDate))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()){
//                                Wallet walletToAdd = new Wallet();
//
//                                // if there is information -> update it
//                                walletToCheck.setWalletBalance(dataSnapshot.getValue(Wallet.class).getWalletBalance());
//                                walletToCheck.setWalletIncome(dataSnapshot.getValue(Wallet.class).getWalletIncome());
//                                walletToCheck.setWalletExpenses(dataSnapshot.getValue(Wallet.class).getWalletExpenses());
//                                walletToCheck.setWalletCarryOver(dataSnapshot.getValue(Wallet.class).getWalletCarryOver());
//                                walletToCheck.setWalletLastTrans(dataSnapshot.getValue(Wallet.class).getWalletLastTrans());
//
//                                Log.d("SERIOUS DEBUG", " dataSnapshot.hasChildren -> THEN ->  "
//                                        + " ID: " + walletToCheck.getWalletId() + "\t"
//                                        + " Name: " + walletToCheck.getWalletName() + "\t"
//                                        + " Currency: " + walletToCheck.getWalletCurrency() + "\t"
//                                        + " Icon: " + walletToCheck.getWalletIcon() + "\t"
//                                        + " Created: " + walletToCheck.getWalletCreated() + "\t"
//                                        + " LastTrans: " + walletToCheck.getWalletLastTrans() + "\t"
//                                        + " Income: " + walletToCheck.getWalletIncome() + "\t"
//                                        + " Expense: " + walletToCheck.getWalletExpenses() + "\t"
//                                        + " Balance: " + walletToCheck.getWalletBalance() + "\t"
//                                        + " CarryOver: " + walletToCheck.getWalletCarryOver() + "\t");

                                mAdapter.addWalletToList(dataSnapshot.getValue(Wallet.class));
                                mAdapter.notifyDataSetChanged();
                                textViewDate.setText(Utilities.getTimeInString(monthFirstDate, true));
                                progressBar.setVisibility(View.GONE);

                            } else {
                                //mAdapter.removeWalletFromList();
                                Log.d("SERIOUS DEBUG", "dataSnapshot.hasChildren -> ELSE -> KEY " + dataSnapshot.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        }
    }

    private void setupFloatingActionMenu(){
        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.firebase_month_actions);
        fabExpense = (FloatingActionButton) findViewById(R.id.firebase_month_action_expence);
        fabIncome = (FloatingActionButton) findViewById(R.id.firebase_month_action_income);
        fabTransfer = (FloatingActionButton) findViewById(R.id.firebase_month_action_transfer);

        floatingActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                Log.d(Constants.LOG_NAVIGATION, "MONTH ACTIVITY: floatingActionMenu -> EXPAND");
            }

            @Override
            public void onMenuCollapsed() {
                Log.d(Constants.LOG_NAVIGATION, "MONTH ACTIVITY: floatingActionMenu -> COLLAPSE");
            }
        });

        fabExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.LOG_NAVIGATION,"MONTH ACTIVITY: fabExpense -> CLICKED");
                floatingActionsMenu.collapse();
                addExpense();
            }
        });

        fabIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.LOG_NAVIGATION,"MONTH ACTIVITY: fabIncome -> CLICKED");
                floatingActionsMenu.collapse();
                addIncome();
            }
        });

        fabTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.LOG_NAVIGATION,"MONTH ACTIVITY: fabTransfer -> CLICKED");
                floatingActionsMenu.collapse();
                addTransfer();
            }
        });
    }

    public void addExpense(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(monthFirstDate);
        calendar.set(Calendar.HOUR, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MILLISECOND));

        Transaction transaction = new Transaction(calendar.getTimeInMillis(),
                calendar.getTimeInMillis(),
                1L,
                Constants.EXPENSE_TRANSACTION_TYPE,
                1L,
                0d,
                PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD"),
                "" );

        Intent intent = new Intent(context, FirebaseExpenseActivity.class);
        intent.putExtra(Constants.EXTRA_TRANSACTION, transaction);
        startActivity(intent);
    }

    public void addIncome(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(monthFirstDate);
        calendar.set(Calendar.HOUR, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MILLISECOND));

        Transaction transaction = new Transaction(calendar.getTimeInMillis(),
                calendar.getTimeInMillis(),
                1L,
                Constants.INCOME_TRANSACTION_TYPE,
                1L,
                0d,
                PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD"),
                "" );

        Intent intent = new Intent(context, FirebaseIncomeActivity.class);
        intent.putExtra(Constants.EXTRA_TRANSACTION, transaction);
        startActivity(intent);
    }

    public void addTransfer(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(monthFirstDate);
        calendar.set(Calendar.HOUR, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MILLISECOND));

        Transaction transaction = new Transaction(
                calendar.getTimeInMillis(),
                calendar.getTimeInMillis(),
                1L,
                Constants.INCOME_TRANSACTION_TYPE,
                0L,
                0d,
                PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD"),
                "" );

        Intent intent = new Intent(context, FirebaseTransferActivity.class);
        intent.putExtra(Constants.EXTRA_TRANSACTION, transaction);
        startActivity(intent);

    }

}
