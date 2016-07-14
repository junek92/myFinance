package com.kirilov.ivan.myfinance.junk_yard;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.BaseActivity;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.MainActivity;
import com.kirilov.ivan.myfinance.R;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Ivan on 02-Jun-16.
 */
public class TestFirebaseDb extends BaseActivity {
    Button btn1, btn2, btn3, btn4, btn5, btnRemove;
    EditText month, balance, editTextCategory;

    private DatabaseReference userInfoEmailWallets, userTransEmail, userCatEmail;
    public static final String LOG = "TESTING FBDB: ";

    Context context;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_firebase_db);
        context = this;

        btn1 = (Button) findViewById(R.id.button_1);
        btn2 = (Button) findViewById(R.id.button_2);
        btn3 = (Button) findViewById(R.id.button_3);
        btn4 = (Button) findViewById(R.id.button_4);
        btn5 = (Button) findViewById(R.id.button_5);
        btnRemove = (Button) findViewById(R.id.button_6);

        month = (EditText) findViewById(R.id.month);
        balance = (EditText) findViewById(R.id.balance);
        editTextCategory = (EditText) findViewById(R.id.test_category);




        userInfoEmailWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/");


        userCatEmail = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/");

        userTransEmail = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/");
    }

    public void removeListeners(View view){

    }

    public void doSomething1(View view){
        Query query = userInfoEmailWallets.orderByKey();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    Log.d(LOG, " Button1: -> DUMMY WALLET -> "
                            + " ID: " + dummyWallet.getWalletId() + "\t"
                            + " Name: " + dummyWallet.getWalletName() + "\t"
                            + " Currency: " + dummyWallet.getWalletCurrency() + "\t"
                            + " Icon: " + dummyWallet.getWalletIcon() + "\t"
                            + " Created: " + dummyWallet.getWalletCreated() + "\t"
                            + " LastTrans: " + dummyWallet.getWalletLastTrans() + "\t"
                            + " Income: " + dummyWallet.getWalletIncome() + "\t"
                            + " Expense: " + dummyWallet.getWalletExpenses() + "\t"
                            + " Balance: " + dummyWallet.getWalletBalance() + "\t"
                            + " CarryOver: " + dummyWallet.getWalletCarryOver() + "\t");
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void doSomething2(View view){

        DatabaseReference refUserInfoUserCurrentMonth = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_CURRENT_MONTH);

        refUserInfoUserCurrentMonth.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Long.class) != MainActivity.beginningOfMonth){
                    Log.d(LOG, " IT'S NEW MONTH - >>>>>>>");
                    Log.d(LOG, " BUTTON 2: VALUE -> " + dataSnapshot.getValue() + " : " + MainActivity.beginningOfMonth);
                } else {
                    Log.d(LOG, " IT'S OLD MONTH :<<<<");
                    Log.d(LOG, " BUTTON 2: VALUE -> " + dataSnapshot.getValue() + " : " + MainActivity.beginningOfMonth);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void doSomething3(View view){
        // Add some dummy months in history
        DatabaseReference refWalletsHistoryEmail = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                                                        + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/");
        // calculate needed month
        Calendar calendar = Calendar.getInstance();
        // set the time to be - 0h : 00m : 00s . 250ms
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.set(Calendar.MILLISECOND, 250);
        // set the date to be - 1st of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, Integer.valueOf(month.getText().toString()));

        Wallet wallet = new Wallet(0L, "Some name", "USD", 0L, Calendar.getInstance().getTimeInMillis(), Calendar.getInstance().getTimeInMillis(),
                0d, 0d, 0d, 0d);

        refWalletsHistoryEmail.child("0").child(Long.toString(calendar.getTimeInMillis())).setValue(wallet);
        refWalletsHistoryEmail.child("1").child(Long.toString(calendar.getTimeInMillis())).setValue(wallet);
    }

    public void doSomething4(View view){
        // calculate needed month
        Calendar calendar = Calendar.getInstance();
        // set the time to be - 0h : 00m : 00s . 250ms
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.set(Calendar.MILLISECOND, 250);
        // set the date to be - 1st of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
       // calendar.set(Calendar.MONTH, Integer.valueOf(month.getText().toString()));

        // get reference to -> userTrans/EMAIL/WALLET_ID/TRANS_TYPE
        final DatabaseReference refWalletsHistoryWalletId = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + 0 + "/"
                + 0 + "/");

        final Query query = refWalletsHistoryWalletId.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    Log.d(LOG, dataSnapshot.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void doSomething5(View view){
        final Map<Integer, Float> monthsValuesMap = new HashMap<>();

        // clear all old data!
        monthsValuesMap.clear();
        // put all months with ZERO values for sum
        for (int i = 0; i < 12; i++ ){
            monthsValuesMap.put(i, 0F);
        }

        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        // get today and clear time of day - 0h : 00m : 00s
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.set(Calendar.MILLISECOND, 250);
        // get start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // get start of the year
        calendar.set(Calendar.MONTH, 0);

        long startOfYearInMs = calendar.getTimeInMillis();

        // get last month in the year
        calendar.set(Calendar.MONTH, 11);

        long endOfYearInMs = calendar.getTimeInMillis();

        DatabaseReference reference = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + "0" + "/"
                + Constants.EXPENSE_TRANSACTION_TYPE + "/"
                + editTextCategory.getText().toString() + "/");

        Query query = reference.orderByKey().startAt(Long.toString(startOfYearInMs)).endAt(Long.toString(endOfYearInMs));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOG, " dataSnapshot.getValue ->" + dataSnapshot.getValue());

                for (DataSnapshot child : dataSnapshot.getChildren()){
                    Float sumOfChilds = 0f;
                    Log.d(LOG, " child.getValue ->" + child.getValue());

                    for (DataSnapshot childsChild : child.getChildren() ){
                        Log.d(LOG, " childsChild.getValue ->" + childsChild.getValue());
                        sumOfChilds = sumOfChilds + (float) childsChild.getValue(Transaction.class).getTrAmount();
                    }

                    calendar.setTimeInMillis(Long.valueOf(child.getKey()));
                    monthsValuesMap.put( calendar.get(Calendar.MONTH), sumOfChilds);
                }

                Log.d(LOG, "\n\n MAP FOR \n");

                for (Map.Entry<Integer,Float> entry : monthsValuesMap.entrySet()) {

                    Log.d(LOG, " MAP KEY:    ->   " + entry.getKey());
                    Log.d(LOG, " MAP VALUE:  ->   " + entry.getValue());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
