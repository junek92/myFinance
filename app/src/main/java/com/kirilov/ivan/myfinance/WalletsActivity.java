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

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.kirilov.ivan.myfinance.adapters.CardViewEditWalletAdapter;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.myExtras.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Ivan on 01-Jun-16.
 */
public class WalletsActivity extends BaseActivity {

    private DatabaseReference refUserInfoUserWallets;
    private Query queryUserInfoUserWallets;
    private ChildEventListener childEventListenerUserInfoUserWallets;

    private RecyclerView recyclerView;
    private CardViewEditWalletAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar progressBar;
    private AddFloatingActionButton addWalletFlAcBtn;

    private Toolbar toolbar;
    private Context context;
    private ArrayList<Wallet> walletsArrayList;

    private long maxWalletId;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallets);

        context = this;
        walletsArrayList = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.wallets_toolbar);
        //toolbar.setTitle("Wallet Buddy");
        setSupportActionBar(toolbar);

//        progressBar = (ProgressBar) findViewById(R.id.firebase_wallets_progress_bar);
//        progressBar.setVisibility(View.VISIBLE);

        addWalletFlAcBtn = (AddFloatingActionButton) findViewById(R.id.wallets_action_btn);

        recyclerView = (RecyclerView) findViewById(R.id.wallets_recycle_view);

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new CardViewEditWalletAdapter(context, recyclerView, walletsArrayList);

        recyclerView.setAdapter(mAdapter);

        // GET REFERENCE TO -> userInfo/EMAIL/userWallets -> to get basic info for all available user wallets
        refUserInfoUserWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/");
        queryUserInfoUserWallets = refUserInfoUserWallets.orderByKey();

        childEventListenerUserInfoUserWallets = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS,"WALLETS: dataSnapshot.getValue(Wallet.class).toString() -> " + dataSnapshot.getValue(Wallet.class).getWalletLastTrans());

                // all needed data for current month is stored at userInfo/EMAIL/userWallets location
                // for each wallet -> add to ArrayList
                // BUT EXCLUDE "Total Balance" wallet with ID=0
                if (dataSnapshot.getValue(Wallet.class).getWalletId() != 0L){
                    if (maxWalletId <= dataSnapshot.getValue(Wallet.class).getWalletId()) {
                        maxWalletId = dataSnapshot.getValue(Wallet.class).getWalletId();
                    }
                    mAdapter.addWalletToList(dataSnapshot.getValue(Wallet.class));
                    mAdapter.notifyDataSetChanged();
//                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue(Wallet.class).getWalletId() != 0L) {
                    mAdapter.changeWalletFromList(dataSnapshot.getValue(Wallet.class));
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Wallet.class).getWalletId() != 0L){
                    mAdapter.removeWalletFromList((int) dataSnapshot.getValue(Wallet.class).getWalletId());
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        queryUserInfoUserWallets.addChildEventListener(childEventListenerUserInfoUserWallets);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (maxWalletId < 5){
            addWalletFlAcBtn.setVisibility(View.VISIBLE);
        } else {
            addWalletFlAcBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (queryUserInfoUserWallets != null){
                Log.d(Constants.LOG_FIREBASE_LISTENERS, "WALLETS_ACTIVITY: childEventListenerUserInfoUserWallets -> REMOVED");
            queryUserInfoUserWallets.removeEventListener(childEventListenerUserInfoUserWallets);
        }
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

    public void btnAddNewWallet(View view){
        Wallet extraWallet = new Wallet(maxWalletId + 1L, "", PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD"), 1L, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(),
                Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis(), 0d, 0d, 0d, 0d);

        Intent intent = new Intent(this, CreateWalletActivity.class);
        intent.putExtra(Constants.EXTRA_WALLET, extraWallet);
        startActivity(intent);
    }
}
