package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ivan on 28-Apr-15.
 */
public class HistoryActivity extends AppCompatActivity {

    private ListView mHistoryList;
    private ProgressBar progressBar;

    List<Long> usefulMonths;        // store 1st day of each month with transactions
    List<String> monthName;         // store strings in format MMM-yyyy
    List<Double> monthBalance;      // store each month balance in double

    HistoryMonthsAdapter mHistoryMonthsAdapter;

    FinanceDbHelper financeDbHelper;
    Toolbar toolbar;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        context = this;

        usefulMonths = new ArrayList<>();
        monthName = new ArrayList<>();
        monthBalance = new ArrayList<>();

        //fetch the custom toolbar - set it as default, change the title
        toolbar = (Toolbar) findViewById(R.id.history_toolbar);
        setSupportActionBar(toolbar);

        //---       NEW NAVIGATION TEST
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.history_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.history_nav_drawer);
        navigationView.setCheckedItem(R.id.nav_history);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                if (id == R.id.nav_this_month) {
                    HistoryActivity.this.finish();
                } else if (id == R.id.nav_history) {

                } else if (id == R.id.nav_analyze) {
                    Intent analyseIntent = new Intent(context, AnalyseActivity.class);
                    startActivity(analyseIntent);
                } else if (id == R.id.nav_settings) {
                    Intent prefIntent = new Intent(context, SettingsActivity.class);
                    startActivity(prefIntent);
                } else if (id == R.id.nav_about) {
                    MainActivity.aboutDialog(context);
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.history_drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.history_progress_bar);
        mHistoryList = (ListView) findViewById(R.id.history_month_list);
        mHistoryMonthsAdapter = new HistoryMonthsAdapter(getApplicationContext(), getLayoutInflater());
    }

    @Override
    protected void onResume() {
        // called when activity is returned on top of stack
        super.onResume();

        // call AsyncTask to fetch all needed data from DB
        new HistoryActivityAsyncTask().execute();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.history_nav_drawer);
        navigationView.setCheckedItem(R.id.nav_history);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.history_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // subclass to implement some background work - AsyncTask< PARAMS, PROGRESS, RESULT>
    private class HistoryActivityAsyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            usefulMonths.clear();
            monthName.clear();
            monthBalance.clear();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getMonthsWithTransaction(getMonths());
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);

            // find all months and pass them -- fetch and select only months with transactions
            //getMonthsWithTransaction(getMonths());
            Log.d("HISTORY LISTS:", "usefulMonths: \n" + usefulMonths.toString());
            Log.d("HISTORY LISTS:", "monthName: \n" + monthName.toString());
            Log.d("HISTORY LISTS:", "monthBalance: \n" + monthBalance.toString());

            // reverse moths to order them in NOW->PAST
            Collections.reverse(usefulMonths);
            Collections.reverse(monthName);
            Collections.reverse(monthBalance);

            // create and populate list view


            mHistoryMonthsAdapter.updateData(usefulMonths, monthName, monthBalance);
            mHistoryList.setAdapter(mHistoryMonthsAdapter);

            mHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // ID = 1st day of month in ms
                    Intent intent = new Intent(context, PieMonthlyActivity.class);
                    intent.putExtra(MainActivity.KEY_PREF_DATE, id);
                    startActivity(intent);
                }
            });

            super.onPostExecute(aVoid);
        }
    }

    // method to fetch all months from 1st transaction
    public List<Long> getMonths(){
        Calendar calendar = Calendar.getInstance();
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);
        List<Transaction> transList = new ArrayList<>();
        List<Long> firstDaysOfMonth = new ArrayList<>();

        if (cursor.moveToFirst()){
            do {
                Transaction transaction = new Transaction();
                transaction.setTrId(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ID)));
                transaction.setAtDate(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DATE)));
                transaction.setAccUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ACCOUNT)));
                transaction.setCatUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CATEGORY)));
                transaction.setAmountSpent(cursor.getDouble(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_AMOUNT)));
                transaction.setCurrUsed(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CURRENCY)));
                transaction.setDescAdded(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DESCR)));
                transList.add(transaction);
            }while (cursor.moveToNext());
        }
        cursor.close();

        if (transList.isEmpty()){
            //TODO: implement toast in AsyncTask-> onPostExecute
//            Toast toast = Toast.makeText(context, "No history to display!", Toast.LENGTH_SHORT);
//            TextView toastView = (TextView) toast.getView().findViewById(android.R.id.message);
//            if (toastView != null) toastView.setGravity(Gravity.CENTER);
//            toast.show();
            finish();
        } else {
            // fetching FIRST and LAST month
            long minMonth = transList.get(0).getAtDate();
            long maxMonth = transList.get(0).getAtDate();
            for (int i = 0; i < transList.size(); i++) {

                if (minMonth >= transList.get(i).getAtDate()) {
                    minMonth = transList.get(i).getAtDate();
                }

                if (maxMonth <= transList.get(i).getAtDate()) {
                    maxMonth = transList.get(i).getAtDate();
                }
            }
            Log.d("HISTORY: ", "FirstMonth: " + minMonth + "\n Last month: " + maxMonth);

            // set time to be day in FIRST month
            calendar.setTimeInMillis(minMonth);

            // get today and clear time of day - 0h : 00m : 00s
            calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
            calendar.clear(Calendar.MINUTE);
            calendar.clear(Calendar.SECOND);
            calendar.clear(Calendar.MILLISECOND);

            // get start of the month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            // set time to be 1st day of FIRST month
            minMonth = calendar.getTimeInMillis();
            Log.d("HISTORY: ", "FirstMonth 1st day: " + calendar.getTime());

            // set time to be day in LAST month
            calendar.setTimeInMillis(maxMonth);

            // get today and clear time of day - 0h : 00m : 00s
            calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
            calendar.clear(Calendar.MINUTE);
            calendar.clear(Calendar.SECOND);
            calendar.clear(Calendar.MILLISECOND);

            // get start of the month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            // set time to be 1st day of FIRST month
            maxMonth = calendar.getTimeInMillis();
            Log.d("HISTORY: ", "LastMonth 1st day: " + calendar.getTime());
            Log.d("HISTORY: ", "FirstMonth 1st day: " + minMonth + " Last month 1st day: " + maxMonth);

            // get all 1st days in each month between FIRST month and LAST month and put them in array list

            long currentMonth = minMonth;
            do {
                firstDaysOfMonth.add(currentMonth);         // add current month in the array
                calendar.setTimeInMillis(currentMonth);     // set 1st of current month in calendar
                Log.d("HISTORY: ", "Added month 1st day: " + calendar.getTime());
                calendar.add(Calendar.MONTH, 1);            // add 1 month
                currentMonth = calendar.getTimeInMillis();  // set current date - 1st of +1 month
            } while (currentMonth <= maxMonth);

            // add 1st day of next month - use it in BETWEEN clause
            firstDaysOfMonth.add(currentMonth);
        }

        return firstDaysOfMonth;
    }

    // method to select only months which have transactions
    public void getMonthsWithTransaction(List<Long> allMonths){
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        int transCount;
        // used to properly iterate all months, 2 means that you will skip CURRENT month + artificially added month
        int monthOffset = 2;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        if (allMonths.size() != 0) {
            // check if your artificially added month is CURRENT - it could happen if you still don't have transactions
            if (calendar.getTimeInMillis() == allMonths.get(allMonths.size() - 1)) {
                // to properly fill history activity change offset to 1
                monthOffset = 1;
            }
        }
        // fetch all DEBIT categories
        List<Category> debitCategories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT);

        // fetch all CREDIT categories
        List<Category> creditCategories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_CREDIT);

        if (allMonths.size() == 2){
            //TODO: implement toast in AsyncTask-> onPostExecute
//            Toast toast = Toast.makeText(context, "No history to display!", Toast.LENGTH_SHORT);
//            TextView toastView = (TextView) toast.getView().findViewById(android.R.id.message);
//            if (toastView != null) toastView.setGravity(Gravity.CENTER);
//            toast.show();
            finish();
        } else {
            // exclude last DATE, because its artificially added to get proper time intervals (only when we need current month to be displayed in history)
            for (int i = 0; i < allMonths.size() - monthOffset; i++) {
                transCount = financeDbHelper.getTransCountByMonth(allMonths.get(i), allMonths.get(i + 1));
                if (transCount > 0) {
                    double debitSum = 0;
                    double creditSum = 0;

                    usefulMonths.add(allMonths.get(i));
                    monthName.add(financeDbHelper.getTimeInString(allMonths.get(i), true));

                    //fetch sum for each DEBIT category
                    for (int j = 0; j < debitCategories.size(); j++) {
                        debitSum = debitSum + financeDbHelper.sumTransByCategoryAndTime(debitCategories.get(j).getCatId(), allMonths.get(i), allMonths.get(i + 1));
                    }

                    //fetch sum for each CREDIT category
                    for (int k = 0; k < creditCategories.size(); k++) {
                        creditSum = creditSum + financeDbHelper.sumTransByCategoryAndTime(creditCategories.get(k).getCatId(), allMonths.get(i), allMonths.get(i + 1));
                    }
                    Log.d("HISTORY Balance: ", "debit: " + debitSum + " credit: " + creditSum);
                    monthBalance.add(creditSum + debitSum);
                }
            }
        }
    }
}
