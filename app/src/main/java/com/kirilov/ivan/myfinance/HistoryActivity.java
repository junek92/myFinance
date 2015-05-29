package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ivan on 28-Apr-15.
 */
public class HistoryActivity extends AppCompatActivity {

    // used for navigation drawer
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    NavigationAdapter mNavigationAdapter;

    List<Long> usefulMonths;        // store 1st day of each month with transactions
    List<String> monthName;         // store strings in format MMM-yyyy
    List<Double> monthBalance;      // store each month balance in double
    HistoryMonthsAdapter mHistoryMonthsAdapter;
    private ListView mHistoryList;

    FinanceDbHelper financeDbHelper;
    Toolbar toolbar;
    Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        context = this;

        //fetch the custom toolbar - set it as default, change the title
        toolbar = (Toolbar) findViewById(R.id.history_app_bar);
        toolbar.setTitle("");
        TextView toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_text);
        toolbarText.setText(R.string.history_title);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // set the ListView and DrawerLayout for NavigationDrawer
        mDrawerList = (ListView) findViewById(R.id.history_navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.history_drawer_layout);
        mNavigationAdapter = new NavigationAdapter(this, getLayoutInflater());
        mDrawerList.setAdapter(mNavigationAdapter);

        setupDrawer();
        mDrawerList.setItemChecked(1, true);

        // listen for selections in our NavigationDrawer
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        onBackPressed();
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 1:
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 2:
                        Intent i = new Intent(context, PrefActivity.class);
                        startActivity(i);
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 3:
                        MainActivity.aboutDialog(context, mDrawerList, 1);
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    default:
                        Toast.makeText(HistoryActivity.this, "TODO MENU - " + id + " ; " + position, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        usefulMonths = new ArrayList<>();
        monthName = new ArrayList<>();
        monthBalance = new ArrayList<>();
        // find all months and pass them -- fetch and select only months with transactions
        getMonthsWithTransaction(getMonths());
            Log.d("HISTORY LISTS:", "usefulMonths: \n" + usefulMonths.toString());
            Log.d("HISTORY LISTS:", "monthName: \n" + monthName.toString());
            Log.d("HISTORY LISTS:", "monthBalance: \n" + monthBalance.toString());

        // reverse moths to order them in NOW->PAST
        Collections.reverse(usefulMonths);
        Collections.reverse(monthName);
        Collections.reverse(monthBalance);

        // create and populate list view
        mHistoryList = (ListView) findViewById(R.id.history_month_list);
        mHistoryMonthsAdapter = new HistoryMonthsAdapter(getApplicationContext(), getLayoutInflater());
        mHistoryList.setAdapter(mHistoryMonthsAdapter);
        mHistoryMonthsAdapter.updateData(usefulMonths, monthName, monthBalance);

        mHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ID = 1st day of month in ms
                Intent intent = new Intent(context, PieMonthlyActivity.class);
                intent.putExtra(MainActivity.KEY_PREF_DATE, id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        // called when activity is returned on top of stack
        super.onResume();
        mDrawerList.setItemChecked(1,true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();    // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();    // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public List<Long> getMonths(){
        Calendar calendar = Calendar.getInstance();
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);
        List<Transaction> transList = new ArrayList<>();

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

        // fetching FIRST and LAST month
        long minMonth = transList.get(0).getAtDate();
        long maxMonth = transList.get(0).getAtDate();
        for (int i = 0; i < transList.size(); i++){

            if (minMonth >= transList.get(i).getAtDate()){
                minMonth = transList.get(i).getAtDate();
            }

            if (maxMonth <= transList.get(i).getAtDate()){
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
        List<Long> firstDaysOfMonth = new ArrayList<>();
        long currentMonth = minMonth;
        do {
            firstDaysOfMonth.add(currentMonth);         // add current month in the array
            calendar.setTimeInMillis(currentMonth);     // set 1st of current month in calendar
                Log.d("HISTORY: ", "Added month 1st day: " + calendar.getTime());
            calendar.add(Calendar.MONTH, 1);            // add 1 month
            currentMonth = calendar.getTimeInMillis();  // set current date - 1st of +1 month
        }while (currentMonth <= maxMonth);
        firstDaysOfMonth.add(currentMonth);             // add 1st day of next month - to use it for BETWEEN clause

        return firstDaysOfMonth;
    }

    // method to select only month which have transactions
    public void getMonthsWithTransaction(List<Long> allMonths){
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        int transCount;

        // fetch all DEBIT categories
        List<Category> debitCategories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT);

        // fetch all CREDIT categories
        List<Category> creditCategories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_CREDIT);

        // exclude last DATE, because its artificially added to get proper time intervals
        for (int i = 0; i < allMonths.size() - 1; i++){
            transCount = financeDbHelper.getTransCountByMonth(allMonths.get(i), allMonths.get(i+1));
            if (transCount > 0){
                double debitSum = 0;
                double creditSum = 0;

                usefulMonths.add(allMonths.get(i));
                monthName.add(financeDbHelper.getTimeInString(allMonths.get(i), true));

                //fetch sum for each DEBIT category
                for (int j = 0; j < debitCategories.size(); j++){
                    debitSum = debitSum + financeDbHelper.sumTransByCategoryAndTime(debitCategories.get(j).getCatId(), allMonths.get(i), allMonths.get(i+1));
                }

                //fetch sum for each CREDIT category
                for (int k = 0; k < creditCategories.size(); k++){
                    creditSum = creditSum + financeDbHelper.sumTransByCategoryAndTime(creditCategories.get(k).getCatId(), allMonths.get(i), allMonths.get(i+1));
                }
                    Log.d("HISTORY Balance: ", "debit: " + debitSum + " credit: " + creditSum);
                monthBalance.add(creditSum + debitSum);
            }
        }



    }
}
