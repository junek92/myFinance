package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.junk_yard.TestFirebaseDb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Ivan on 02-Jun-16.
 */
public class FirebaseHistoryActivity extends BaseActivity {

    private DatabaseReference refWalletsHistoryEmailZero;
    private ValueEventListener historyListener;
    private Query historyQuerry;

    private RecyclerView recyclerView;
    private WalletsHistoryRowAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar progressBar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private ArrayList<String> walletsMonth;
    private ArrayList<Double> walletsBalance;

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity_history);
        context = this;

        walletsMonth = new ArrayList<>();
        walletsBalance = new ArrayList<>();

        //fetch the custom toolbar - set it as default, change the title
        toolbar = (Toolbar) findViewById(R.id.firebase_history_toolbar);
        setSupportActionBar(toolbar);

        setupNavigationDrawer();

        progressBar = (ProgressBar) findViewById(R.id.firebase_history_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = (RecyclerView) findViewById(R.id.firebase_history_recycle_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new WalletsHistoryRowAdapter(context, recyclerView, walletsMonth, walletsBalance);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setVisibility(View.INVISIBLE);



    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_main_history);

        fetchAvailableHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (historyListener != null){
            historyQuerry.removeEventListener(historyListener);
                Log.d(Constants.LOG_FIREBASE_LISTENERS, "HISTORY: historyListener -> REMOVED");
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void fetchAvailableHistory(){
        refWalletsHistoryEmailZero = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/0/");

        historyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, "HISTORY: historyListener -> TRIGGERED");

                if (dataSnapshot.hasChildren()){
                    walletsBalance.clear();
                    walletsMonth.clear();

                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        walletsBalance.add(child.getValue(Wallet.class).getWalletBalance());
                        walletsMonth.add(child.getKey());
                    }
                    Collections.reverse(walletsBalance);
                    Collections.reverse(walletsMonth);

                    mAdapter.replaceCurrentData(walletsMonth, walletsBalance);

                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    FirebaseHistoryActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error. " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        historyQuerry = refWalletsHistoryEmailZero.orderByKey();
        historyQuerry.addValueEventListener(historyListener);
            Log.d(Constants.LOG_FIREBASE_LISTENERS, "HISTORY: historyListener -> ADDED");
    }

    private void setupNavigationDrawer(){

        //---       NEW NAVIGATION TEST
        drawerLayout = (DrawerLayout) findViewById(R.id.firebase_history_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.firebase_history_nav_drawer);
        //get the header of the navigation drawer
        View view = navigationView.getHeaderView(0);
        //find the text view in the header and set the text to current email in use
        TextView usedEmail = (TextView) view.findViewById(R.id.nav_header_email);
        usedEmail.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, "").replace(',','.'));
        navigationView.setCheckedItem(R.id.nav_main_history);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                switch (id){
                    case R.id.nav_main_current:
                        FirebaseHistoryActivity.this.finish();
                        break;
                    case R.id.nav_main_history:
                        break;
                    case R.id.nav_main_analyze:
                        Intent analyseIntent = new Intent(context, BarChartActivity.class);
                        startActivity(analyseIntent);
                        break;
                    case R.id.nav_main_firebase:
                        Intent testIntent  = new Intent(context, TestFirebaseDb.class);
                        startActivity(testIntent);
                        break;
                    case R.id.nav_edit_wallets:
                        Intent walletsIntent = new Intent(context, FirebaseWalletsActivity.class);
                        startActivity(walletsIntent);
                        break;
                    case R.id.nav_edit_categories:
                        Intent catIntent = new Intent(context, FirebaseCategoriesActivity.class);
                        startActivity(catIntent);
                        break;
                    case R.id.nav_more_settings:
                        Intent prefIntent = new Intent(context, SettingsActivity.class);
                        startActivity(prefIntent);
                        break;
                    case R.id.nav_more_about:
                        MainActivity.aboutDialog(context);
                        break;

                    default:
                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

}
