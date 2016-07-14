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
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.adapters.CardViewWalletAdapter;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Ivan on 02-Jun-16.
 */
public class MonthActivity extends BaseActivity {
    private ValueEventListener walletValueEventListener;
    private DatabaseReference refUserInfoUserWallets, refWalletsHistoryEmail;
    private Query queryUserInfoUserWallets;

    private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton fabExpense, fabIncome, fabTransfer;

    private ProgressBar progressBar;
    private TextView textViewDate;

    private RecyclerView recyclerView;
    private CardViewWalletAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Wallet> walletsArrayList;

    private long monthFirstDate;
    private long currentWallet;
    private int currentPosition;
    private Wallet walletToCheck;
    private boolean isFirstCheckReady;

    private Context context;
    private Toolbar toolbar;

    private RelativeLayout bcgDimLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month);

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

        toolbar = (Toolbar) findViewById(R.id.month_toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.month_progress_bar);
        progressBar.setVisibility(View.VISIBLE);


        textViewDate = (TextView) findViewById(R.id.month_current_date);
        textViewDate.setText(Utilities.getTimeInString(monthFirstDate, true));
        textViewDate.setVisibility(View.INVISIBLE);


        setupFloatingActionMenu();

        recyclerView = (RecyclerView) findViewById(R.id.month_recycler_view);
        recyclerView.setVisibility(View.INVISIBLE);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new CardViewWalletAdapter(context, recyclerView, monthFirstDate);
        recyclerView.setAdapter(mAdapter);



        // GET REFERENCE TO -> userInfo/EMAIL/userWallets -> to get basic info for all available user wallets
        refUserInfoUserWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/");
        queryUserInfoUserWallets = refUserInfoUserWallets.orderByKey();

        // GET REFERENCE TO -> userWalletsHistory/EMAIL/ -> to be able to check for any available history OR to add wallets in history when new month comes
        refWalletsHistoryEmail = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/");

        walletValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    mAdapter.changeWalletFromList(dataSnapshot.getValue(Wallet.class));
                    mAdapter.notifyDataSetChanged();

//                    textViewDate.setText(Utilities.getTimeInString(monthFirstDate, true));
//                    progressBar.setVisibility(View.GONE);

                } else {
                    mAdapter.removeWalletFromList2(Long.parseLong(dataSnapshot.getRef().getParent().getKey()));
                    mAdapter.notifyDataSetChanged();

                    // change visibility at the last wallet - 5
                    if (Long.parseLong(dataSnapshot.getRef().getParent().getKey()) == 5){
                        textViewDate.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

//                    Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "MONTH: dataSnapshot.hasChildren -> ELSE -> KEY " + dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // add 6 empty wallets
        Wallet emptyWallet0 = new Wallet(0L, "", "", 0L, 0L, 0L, 0d, 0d, 0d, 0d);
        mAdapter.addWalletToList(emptyWallet0);
        Wallet emptyWallet1 = new Wallet(1L, "", "", 0L, 0L, 0L, 0d, 0d, 0d, 0d);
        mAdapter.addWalletToList(emptyWallet1);
        Wallet emptyWallet2 = new Wallet(2L, "", "", 0L, 0L, 0L, 0d, 0d, 0d, 0d);
        mAdapter.addWalletToList(emptyWallet2);
        Wallet emptyWallet3 = new Wallet(3L, "", "", 0L, 0L, 0L, 0d, 0d, 0d, 0d);
        mAdapter.addWalletToList(emptyWallet3);
        Wallet emptyWallet4 = new Wallet(4L, "", "", 0L, 0L, 0L, 0d, 0d, 0d, 0d);
        mAdapter.addWalletToList(emptyWallet4);
        Wallet emptyWallet5 = new Wallet(5L, "", "", 0L, 0L, 0L, 0d, 0d, 0d, 0d);
        mAdapter.addWalletToList(emptyWallet5);

        monthActivityFetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mAdapter.clearWalletsData();
//
//        isFirstCheckReady = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (walletValueEventListener != null){
            for (int i = 0; i<=6; i++){
                refWalletsHistoryEmail
                        .child(Integer.toString(i))
                        .child(String.valueOf(monthFirstDate))
                        .removeEventListener(walletValueEventListener);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
//        mAdapter.clearWalletsData();
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

    private void monthActivityFetchData(){
        // maximum number of wallets is 5 + TOTAL BALANCE
        for (int i = 0; i<=6; i++){
            //create new reference in WALLETHISTORY node, to check if there is any information about this wallet at given moment in time
            Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "MONTH: AddListener FOR:I -> " + i);

            refWalletsHistoryEmail
                    .child(Integer.toString(i))
                    .child(String.valueOf(monthFirstDate))
                    .addValueEventListener(walletValueEventListener);
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.hasChildren()){//                                Wallet walletToAdd = new Wallet();
////
////                                // if there is information -> update it
////                                walletToCheck.setWalletBalance(dataSnapshot.getValue(Wallet.class).getWalletBalance());
////                                walletToCheck.setWalletIncome(dataSnapshot.getValue(Wallet.class).getWalletIncome());
////                                walletToCheck.setWalletExpenses(dataSnapshot.getValue(Wallet.class).getWalletExpenses());
////                                walletToCheck.setWalletCarryOver(dataSnapshot.getValue(Wallet.class).getWalletCarryOver());
////                                walletToCheck.setWalletLastTrans(dataSnapshot.getValue(Wallet.class).getWalletLastTrans());
////
////                                Log.d("SERIOUS DEBUG", " dataSnapshot.hasChildren -> THEN ->  "
////                                        + " ID: " + walletToCheck.getWalletId() + "\t"
////                                        + " Name: " + walletToCheck.getWalletName() + "\t"
////                                        + " Currency: " + walletToCheck.getWalletCurrency() + "\t"
////                                        + " Icon: " + walletToCheck.getWalletIcon() + "\t"
////                                        + " Created: " + walletToCheck.getWalletCreated() + "\t"
////                                        + " LastTrans: " + walletToCheck.getWalletLastTrans() + "\t"
////                                        + " Income: " + walletToCheck.getWalletIncome() + "\t"
////                                        + " Expense: " + walletToCheck.getWalletExpenses() + "\t"
////                                        + " Balance: " + walletToCheck.getWalletBalance() + "\t"
////                                        + " CarryOver: " + walletToCheck.getWalletCarryOver() + "\t");
//
//                                mAdapter.addWalletToList(dataSnapshot.getValue(Wallet.class));
//                                    Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "MONTH: AddWalletToList WALLET:NAME -> " + dataSnapshot.getValue(Wallet.class).getWalletName());
//                                mAdapter.notifyDataSetChanged();
//                                textViewDate.setText(Utilities.getTimeInString(monthFirstDate, true));
//                                progressBar.setVisibility(View.GONE);
//
//                            } else {
//                                //mAdapter.removeWalletFromList();
//                                Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "MONTH: dataSnapshot.hasChildren -> ELSE -> KEY " + dataSnapshot.getKey());
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
            Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "MONTH: END OF AddListener FOR:I -> " + i);
        }
    }

    private void setupFloatingActionMenu(){
        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.month_action_menu);
        fabExpense = (FloatingActionButton) findViewById(R.id.month_action_expense);
        fabIncome = (FloatingActionButton) findViewById(R.id.month_action_income);
        fabTransfer = (FloatingActionButton) findViewById(R.id.month_action_transfer);
        bcgDimLayout = (RelativeLayout) findViewById(R.id.month_background_dimmer);

        bcgDimLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingActionsMenu.isExpanded()){
                    floatingActionsMenu.collapse();
                } else {
                    Log.d(Constants.LOG_NAVIGATION, "MONTH background dim ELSE...");
                }
            }
        });

        floatingActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                bcgDimLayout.setVisibility(View.VISIBLE);
                Log.d(Constants.LOG_NAVIGATION, "MONTH ACTIVITY: floatingActionMenu -> EXPAND");
            }

            @Override
            public void onMenuCollapsed() {
                bcgDimLayout.setVisibility(View.GONE);
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

        Intent intent = new Intent(context, ExpenseActivity.class);
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

        Intent intent = new Intent(context, IncomeActivity.class);
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

        Intent intent = new Intent(context, TransferActivity.class);
        intent.putExtra(Constants.EXTRA_TRANSACTION, transaction);
        startActivity(intent);

    }

}
