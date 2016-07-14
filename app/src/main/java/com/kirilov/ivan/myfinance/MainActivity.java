package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.adapters.CardViewWalletAdapter;
import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Ivan on 19-May-16.
 */
public class MainActivity extends BaseActivity {

    private DatabaseReference refUserInfoUserWallets, refUserInfoUserCurrentMonth, refWalletsHistoryEmail;
    private Query queryUserInfoUserWallets;
    private ChildEventListener childEventListenerUserInfoUserWallets;
    private ValueEventListener historyValueEventListener;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton fabExpense, fabIncome, fabTransfer;

    private ProgressBar progressBar;
    private TextView textViewDate;

    private RecyclerView recyclerView;
    private CardViewWalletAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Toolbar toolbar;
    private Context context;
    private ArrayList<Wallet> walletsArrayList;

    public static long beginningOfMonth;
    private long userCurrentMonth;

    private boolean isThereHistory;

    private RelativeLayout backgorundView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        walletsArrayList = new ArrayList<>();
        beginningOfMonth = Utilities.calculateBeggingOfCurrentMonth();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        //toolbar.setTitle("Wallet Buddy");
        setSupportActionBar(toolbar);

        setupNavDrawer();
        setupFloatingActionMenu();

        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        textViewDate = (TextView) findViewById(R.id.main_current_date);
        textViewDate.setVisibility(View.INVISIBLE);


        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new CardViewWalletAdapter(context, recyclerView, beginningOfMonth);
        recyclerView.setAdapter(mAdapter);

        // GET REFERENCE TO -> userInfo/EMAIL/userWallets -> to get basic info for all available user wallets
        refUserInfoUserWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/");

        queryUserInfoUserWallets = refUserInfoUserWallets.orderByKey();

        // GET REFERENCE TO -> userWalletsHistory/EMAIL/ -> to be able to check for any available history OR to add wallets in history when new month comes
        refWalletsHistoryEmail = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/");


        addDefaultWallets();

        childEventListenerUserInfoUserWallets = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "MAIN: AddChild WALLET: NAME -> " + dataSnapshot.getValue(Wallet.class).getWalletName()
                            + "\t dataSnapshot:KEY -> " + dataSnapshot.getKey());

                // all needed data for current month is stored at userInfo/EMAIL/userWallets location
                // for each wallet -> add to ArrayList
                mAdapter.addWalletToList(dataSnapshot.getValue(Wallet.class));
                mAdapter.notifyDataSetChanged();

                textViewDate.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mAdapter.changeWalletFromList(dataSnapshot.getValue(Wallet.class));
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mAdapter.removeWalletFromList((int) dataSnapshot.getValue(Wallet.class).getWalletId());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // The listener is added here because if it's added in ONSTART -> for each adding the mAdapter is improperly increased with the new CHILDREN
        queryUserInfoUserWallets.addChildEventListener(childEventListenerUserInfoUserWallets);
            Log.d(Constants.LOG_FIREBASE_LISTENERS, "MAIN_ACTIVITY: childEventListenerUserInfoUserWallets -> ADDED");

//        walletsValueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Hash Map to store all <KEY VALUE> pairs
//                HashMap<String, Wallet> allWalletsHashMap = new HashMap<>();
//
//                // enhanced FOR loop - for (int current : int [] ) { whats_the_integer = current }
//                for (DataSnapshot singleDataSnapshot : dataSnapshot.getChildren()) {
//                    allWalletsHashMap.put(singleDataSnapshot.getKey(), singleDataSnapshot.getValue(Wallet.class));
//                }
//
//                int order = 0;
//                // google how to read HashMap with enhanced for :/
//                for (Map.Entry<String, Wallet> walletEntry : allWalletsHashMap.entrySet()){
//                    walletEntry.getKey();
//                    walletEntry.getValue().toString();
//                    order++;
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(context, "Error." + databaseError.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        };

        availableHistoryCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_main_current);

        mAdapter.notifyDataSetChanged();

        beginningOfMonth = Utilities.calculateBeggingOfCurrentMonth();
        textViewDate.setText(Utilities.getTimeInString(beginningOfMonth, true));

        newMonthCheck();

        getScreenDimension();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (childEventListenerUserInfoUserWallets != null){
                Log.d(Constants.LOG_FIREBASE_LISTENERS, "MAIN_ACTIVITY: childEventListenerUserInfoUserWallets -> REMOVED");
            queryUserInfoUserWallets.removeEventListener(childEventListenerUserInfoUserWallets);
        }

        if (historyValueEventListener != null){
            Log.d(Constants.LOG_FIREBASE_LISTENERS, "MAIN_ACTIVITY: historyValueEventListener -> REMOVED");
            refWalletsHistoryEmail.child("0").removeEventListener(historyValueEventListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (floatingActionsMenu.isExpanded()){
            floatingActionsMenu.collapse();
        } else {
            super.onBackPressed();
        }
    }

    private void setupNavDrawer(){
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // when navigation drawer is opened -> close the floating actions menu if its open
                if (floatingActionsMenu.isExpanded()){
                    floatingActionsMenu.collapse();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                navigationView.setCheckedItem(R.id.nav_main_current);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.main_nav_view);

        //get the header of the navigation drawer
        View view = navigationView.getHeaderView(0);

        //find the text view in the header and set the text to current email in use
        TextView usedEmail = (TextView) view.findViewById(R.id.nav_header_email);
        usedEmail.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, "").replace(',','.'));

        //set current menu item as checked
        navigationView.setCheckedItem(R.id.nav_main_current);

        //add item selected listener to handle navigation
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                switch (id){
                    case R.id.nav_main_current:
                        break;
                    case R.id.nav_main_history:
                        if (isThereHistory){
                            Intent historyActivity = new Intent(context, HistoryActivity.class);
                            startActivity(historyActivity);
                        } else {
                            Toast.makeText(context, "History is empty!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.nav_main_analyze:
                        Intent analyseIntent = new Intent(context, AnalyzeActivity.class);
                        startActivity(analyseIntent);
                        break;
//                    case R.id.nav_main_firebase:
//                        Intent testIntent  = new Intent(context, TestFirebaseDb.class);
//                        startActivity(testIntent);
//                        break;
                    case R.id.nav_edit_wallets:
                        Intent walletsIntent = new Intent(context, WalletsActivity.class);
                        startActivity(walletsIntent);
                        break;
                    case R.id.nav_edit_categories:
                        Intent catIntent = new Intent(context, CategoriesActivity.class);
                        startActivity(catIntent);
                        break;
                    case R.id.nav_more_settings:
                        Intent prefIntent = new Intent(context, SettingsActivity.class);
                        startActivity(prefIntent);
                        break;
                    case R.id.nav_more_about:
                        aboutDialog(context);
                        break;

                    default:
                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    private void setupFloatingActionMenu(){
        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.main_action_menu);
        fabExpense = (FloatingActionButton) findViewById(R.id.main_action_expense);
        fabIncome = (FloatingActionButton) findViewById(R.id.main_action_income);
        fabTransfer = (FloatingActionButton) findViewById(R.id.main_action_transfer);
        backgorundView = (RelativeLayout) findViewById(R.id.main_background_dimmer);

        backgorundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingActionsMenu.isExpanded()){
                    floatingActionsMenu.collapse();
                } else {
                    Log.d(Constants.LOG_NAVIGATION, "DIM VIEW ELSE...");
                }
            }
        });

        floatingActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                backgorundView.setVisibility(View.VISIBLE);
                Log.d(Constants.LOG_NAVIGATION, "MAIN_ACTIVITY: floatingActionMenu -> EXPAND");
            }

            @Override
            public void onMenuCollapsed() {
                backgorundView.setVisibility(View.GONE);
                Log.d(Constants.LOG_NAVIGATION, "MAIN_ACTIVITY: floatingActionMenu -> COLLAPSE");
            }
        });

        fabExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.LOG_NAVIGATION,"MAIN_ACTIVITY: fabExpense -> CLICKED");
                floatingActionsMenu.collapse();
                addExpense();
            }
        });

        fabIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.LOG_NAVIGATION,"MAIN_ACTIVITY: fabIncome -> CLICKED");
                floatingActionsMenu.collapse();
                addIncome();
            }
        });

        fabTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.LOG_NAVIGATION,"MAIN_ACTIVITY: fabTransfer -> CLICKED");
                floatingActionsMenu.collapse();
                addTransfer();
            }
        });
    }

    public void addExpense(){
        Transaction transaction = new Transaction(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),
                Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),
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
        Transaction transaction = new Transaction(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),
                                                    Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),
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
        Transaction transaction = new Transaction(
                Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),
                Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),
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

    /*
    *   addDefaultWallets -> called on every APP start up, and checks:
    *   1. if PREFERENCE for ADDING_DEF_WALLETS is true THEN continue with other checks, ELSE do not do anything
    *       TRUE is set -> when the user is NOT LOGGED IN or on LOG OUT
    *       FALSE is set -> after EVERY check for default wallets
    *   2. if there are children at userInfo/EMAIL/userWallets THEN it's OLD acc and does not need default wallets ELSE continue
    *       - add default wallets at userInfo/EMAIL/userWallets
    *       - add default wallets at userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_THIS_MONTH/
    * */

    private void addDefaultWallets(){
        final SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Log.d(Constants.LOG_PREFERENCES, "MAIN_ACTIVITY: addDefaultWallets BEGIN -> value = " + preferenceManager.getBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true));

        if(preferenceManager.getBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true)){
            // FIRST CHECK FOR ANY AVAILABLE WALLETS -> IF THERE ARE NO WALLETS CREATE DEFAULT ONES
            refUserInfoUserWallets.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // if there is some INFO (children) at userInfo/EMAIL/userWallets => it's OLD acc and does not need default wallets, ELSE add them
                    if (!dataSnapshot.hasChildren()){
                        // Create default wallets
                        Wallet totalBalance = new Wallet(0L, "Total Balance", preferenceManager.getString(Constants.KEY_PREF_CURRENCY, "USD"),
                                0L, -1L, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(), 0d, 0d, 0d, 0d);

                        Wallet cash = new Wallet(1L, "Cash", preferenceManager.getString(Constants.KEY_PREF_CURRENCY, "USD"),
                                1L, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(), Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(), 0d, 0d, 0d, 0d);

                        Wallet card = new Wallet(2L, "Card", preferenceManager.getString(Constants.KEY_PREF_CURRENCY, "USD"),
                                2L, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(), Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(), 0d, 0d, 0d, 0d);

                        // --- Add wallets at userInfo/EMAIL/userWallets
                        //Add TOTAL BALANCE wallet
                        refUserInfoUserWallets.child(String.valueOf(totalBalance.getWalletId())).setValue(totalBalance);
                        //Add CASH wallet
                        refUserInfoUserWallets.child(String.valueOf(cash.getWalletId())).setValue(cash);
                        //Add CARD wallet
                        refUserInfoUserWallets.child(String.valueOf(card.getWalletId())).setValue(card);

                            Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "Added WALLETS at userInfo/EMAIL/userWallets");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(Constants.LOG_APP_ERROR, "MainActivity:addDefaultWallets #1-> onCancelled: " + databaseError.getMessage());
                }
            });

            // SECOND CHECK FOR ANY AVAILABLE INCOME CATEGORIES -> IF THERE ARE NO CATEGORIES CREATE DEFAULT ONES
            final DatabaseReference refIncomeCategories = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                    + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                    + Constants.FIREBASE_LOCATION_USER_CATEGORIES_INCOME + "/");
            refIncomeCategories.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChildren()){
                        Category transferFrom = new Category(0L, Constants.INCOME_CATEGORY_TYPE, "Transfer From", 0L, 0L);
                        Category salary = new Category(1L, Constants.INCOME_CATEGORY_TYPE, "Salary", 1L, 0L);
                        Category deposits = new Category(2L, Constants.INCOME_CATEGORY_TYPE, "Deposits", 2L, 0L);
                        Category savings = new Category(3L, Constants.INCOME_CATEGORY_TYPE, "Savings", 3L, 0L);

                        refIncomeCategories.child("0").setValue(transferFrom);
                        refIncomeCategories.child("1").setValue(salary);
                        refIncomeCategories.child("2").setValue(deposits);
                        refIncomeCategories.child("3").setValue(savings);

                            Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "Added INCOME categories at userCat/EMAIL/incomeCat");

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(Constants.LOG_APP_ERROR, "MainActivity:addDefaultWallets #2-> onCancelled: " + databaseError.getMessage());
                }
            });

            // THIRD CHECK FOR ANY AVAILABLE EXPENSE CATEGORIES -> IF THERE ARE NO CATEGORIES CREATE DEFAULT ONES
            final DatabaseReference refExpenseCategories = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                    + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                    + Constants.FIREBASE_LOCATION_USER_CATEGORIES_EXPENSE + "/");
            refExpenseCategories.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChildren()){
                        Category transferTo = new Category(0L, Constants.EXPENSE_CATEGORY_TYPE, "Transfer To", 0L, 0L);
                        Category home = new Category(1L, Constants.EXPENSE_CATEGORY_TYPE, "Home", 1L, 0L);
                        Category food = new Category(2L, Constants.EXPENSE_CATEGORY_TYPE, "Food", 2L, 0L);
                        Category bills = new Category(3L, Constants.EXPENSE_CATEGORY_TYPE, "Bills", 3L, 0L);
                        Category clothes = new Category(4L, Constants.EXPENSE_CATEGORY_TYPE, "Clothes", 4L, 0L);
                        Category transport = new Category(5L, Constants.EXPENSE_CATEGORY_TYPE, "Transport", 5L, 0L);
                        Category car = new Category(6L, Constants.EXPENSE_CATEGORY_TYPE, "Car", 6L, 0L);
                        Category enterteinment = new Category(7L, Constants.EXPENSE_CATEGORY_TYPE, "Entertainment", 7L, 0L);
                        Category other = new Category(8L, Constants.EXPENSE_CATEGORY_TYPE, "Other", 8L, 0L);

                        refExpenseCategories.child("0").setValue(transferTo);
                        refExpenseCategories.child("1").setValue(home);
                        refExpenseCategories.child("2").setValue(food);
                        refExpenseCategories.child("3").setValue(bills);
                        refExpenseCategories.child("4").setValue(clothes);
                        refExpenseCategories.child("5").setValue(transport);
                        refExpenseCategories.child("6").setValue(car);
                        refExpenseCategories.child("7").setValue(enterteinment);
                        refExpenseCategories.child("8").setValue(other);

                            Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "Added EXPENSE categories at userCat/EMAIL/expenseCat");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(Constants.LOG_APP_ERROR, "MainActivity:addDefaultWallets #3-> onCancelled: " + databaseError.getMessage());
                }
            });


            preferenceManager.edit().putBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, false).apply();
                Log.d(Constants.LOG_PREFERENCES, "MAIN_ACTIVITY: addDefaultWallets END -> value = " + preferenceManager.getBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true));
        }
    }

    public static void aboutDialog (Context context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder
                .setTitle("About")
                .setIcon(R.drawable.nav_icon_about)
                .setMessage(R.string.about_dialog)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        // Make the TextView clickable. Must be called after show()
        ((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

    }

    public void newMonthCheck(){
        refUserInfoUserCurrentMonth = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_CURRENT_MONTH);

        refUserInfoUserCurrentMonth.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Long.class) != beginningOfMonth){

                    userCurrentMonth = dataSnapshot.getValue(Long.class);
                        Log.d("BEGINOFMONTH ", " Its a NEW MONTH ");

                    refUserInfoUserWallets.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Wallet dummyWallet = new Wallet();

                            for (DataSnapshot children : dataSnapshot.getChildren()){
                                dummyWallet.setWalletId(children.getValue(Wallet.class).getWalletId());
                                dummyWallet.setWalletName(children.getValue(Wallet.class).getWalletName());
                                dummyWallet.setWalletCurrency(children.getValue(Wallet.class).getWalletCurrency());
                                dummyWallet.setWalletIcon(children.getValue(Wallet.class).getWalletIcon());
                                dummyWallet.setWalletCreated(children.getValue(Wallet.class).getWalletCreated());
                                dummyWallet.setWalletLastTrans(children.getValue(Wallet.class).getWalletLastTrans());
                                dummyWallet.setWalletIncome(0d);
                                dummyWallet.setWalletExpenses(0d);
                                dummyWallet.setWalletBalance(0d);
                                // new wallet CARRY OVER = OLD BALANCE + OLD CARRY OVER
                                dummyWallet.setWalletCarryOver(children.getValue(Wallet.class).getWalletBalance() + children.getValue(Wallet.class).getWalletCarryOver());

                                // should replace userInfo/EMAIL/ with new wallet information
                                refUserInfoUserWallets.child(Long.toString(dummyWallet.getWalletId())).setValue(dummyWallet);

                                // also should create new TIMESTAMP in userWalletsHistory/EMAIL/WALLETID/OLD MONTH TIMESTAMP
                                refWalletsHistoryEmail.child(Long.toString(dummyWallet.getWalletId())).child(Long.toString(userCurrentMonth)).setValue(children.getValue(Wallet.class));

                                // change the current month TIMESTAMP with the new one
                                refUserInfoUserCurrentMonth.setValue(beginningOfMonth);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Log.d("BEGINOFMONTH ", " Its the OLD MONTH :-( ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void availableHistoryCheck(){
        historyValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    isThereHistory = true;
                        Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, " There is available history: " + dataSnapshot.getChildrenCount());
                } else {
                    isThereHistory = false;
                    Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, " There is NO available history!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        refWalletsHistoryEmail.child("0").addValueEventListener(historyValueEventListener);
        Log.d(Constants.LOG_FIREBASE_LISTENERS, "MAIN_ACTIVITY: historyValueEventListener -> ADDED");
    }

    private void getScreenDimension(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dpi = dm.densityDpi;
        float dens = dm.density;
        float dmxdpi = dm.xdpi;
        float dmydpi = dm.ydpi;

        double wi = (double)width / (double)dpi;
        double hi = (double)height / (double)dpi;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        double screenInches = Math.sqrt(x+y);

        String[] screenInformation = new String[7];
        screenInformation[0] = String.valueOf(width) + " px";
        screenInformation[1] = String.valueOf(height) + " px" ;
        screenInformation[2] = String.format("%.2f", screenInches) + " inches" ;
        screenInformation[3] = String.valueOf(dpi) + " DPI";
        screenInformation[4] = String.valueOf(dens) + " logical density";
        screenInformation[5] = String.valueOf(dmxdpi) + " xDPI";
        screenInformation[6] = String.valueOf(dmydpi) + " yDPI";


        for (String string : screenInformation){
            Log.d("TESTING", " -> " + string);
        }
    }



}
