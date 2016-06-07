package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.sqlite_db.FinanceContract;
import com.kirilov.ivan.myfinance.sqlite_db.FinanceDbHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends BaseActivity {

    public static String        KEY_DATE = "chosenDate";
    public static String        KEY_TRANS_ID = "transId";
    public static String        KEY_CAT_ID = "catId";

    private PieChart    pieChart;
    private TextView textViewBalance, textViewDate;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private Toolbar toolbar;

    private FinanceDbHelper financeDbHelper;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //on app first launch - do certain stuff
        setupPreferencesOnFirstUse();

        //fetch the custom toolbar - set it as default, change the title
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        setupNavigationDrawer();

        textViewDate = (TextView) findViewById(R.id.main_date);
        textViewBalance = (TextView) findViewById(R.id.main_textBalance);
        pieChart = (PieChart) findViewById(R.id.main_pieChart);
    }

    @Override
    protected void onResume() {
        super.onResume();

        navigationView.setCheckedItem(R.id.nav_main_current);

        // AsyncTask to - calculate balance, set current date
        new MainActivityAsyncTask().execute();

        setupPieChart(pieChart, false);

    }

    @Override
    protected void onDestroy() {
        // closes any open DB object
        financeDbHelper.close();
            Log.d("FINANCE DB: ", "DB closed");

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//---   Subclass to implement some background work - AsyncTask< PARAMS, PROGRESS, RESULT>
    private class MainActivityAsyncTask extends AsyncTask<Void, String, Void> {
        double endBalance;

        @Override
        protected Void doInBackground(Void... params) {
            endBalance = calcBalance(Calendar.getInstance().getTimeInMillis(), context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // set current date in TextView
            textViewDate.setText(FinanceDbHelper.getInstance(context).getTimeInString(Calendar.getInstance().getTimeInMillis(), true));

            // set calculated balance in Text View
            if (endBalance > 0)
                textViewBalance.setBackgroundColor(context.getResources().getColor(R.color.primaryColorDark));
            else
                textViewBalance.setBackgroundColor(context.getResources().getColor(R.color.accentColor));

            textViewBalance.setText(new PieValueFormatter(PreferenceManager.getDefaultSharedPreferences(context)
                                                                    .getString(Constants.KEY_PREF_CURRENCY,"BGN"))
                                                                    .getFormattedValue((float) endBalance, null, 0, null));

            super.onPostExecute(aVoid);
        }
    }
//---   End of subclass

    // returns array of SUMS for each DEBIT category, in parameter is time in ms to fetch the MONTH
    public float[] extractDataForPie(long timeInMs){
        financeDbHelper = FinanceDbHelper.getInstance(context);
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(timeInMs);
        // get today and clear time of day - 0h : 00m : 00s
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        // get start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = calendar.getTimeInMillis();
            Log.d("FINANCE DB:","Start of the month:       " + calendar.getTime());
            Log.d("FINANCE DB:", "... in milliseconds:      " + calendar.getTimeInMillis());

        // get start of the next month
        calendar.add(Calendar.MONTH, 1);
        long endOfMonth = calendar.getTimeInMillis();
            Log.d("FINANCE DB:","Start of the next month:  " + calendar.getTime());
            Log.d("FINANCE DB:","... in milliseconds:      " + calendar.getTimeInMillis());

        // fetch all DEBIT categories
        List<Category> debitCategories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT);
            Log.d("FINANCE DB:", " category number: "+debitCategories.size());
        float[] pieData = new float[debitCategories.size()];
            //Log.d("FINANCE DB:", " post FOR: \n" + pieData.toString());

        //fetch data for every category
        for (int i = 0; i < debitCategories.size(); i++){
            // get sum for each category and put it in  the array
            pieData[i] = -1 * financeDbHelper.sumTransByCategoryAndTime(debitCategories.get(i).getCatId(), startOfMonth, endOfMonth);
                //Log.d("FINANCE DB:", " after FOR: \n"+pieData.toString());
        }

        return pieData;
    }

    // method to create, customize and inflate with data the PieChart, in parameters is array of floats which contains Y values ( DATA )
    public void setupPieChart(PieChart mPieChart, boolean animateChart){
        financeDbHelper = FinanceDbHelper.getInstance(context);
        ArrayList<String> allCatNames = new ArrayList<>();
        ArrayList<Long> allCatIds = new ArrayList<>();
        ArrayList<String> notZeroCategories = new ArrayList<>();
        final ArrayList<Long> notZeroCategoriesID = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        PieDataSet pieDataSet;
        PieData pieData;

        float[] dataForPie = extractDataForPie(Calendar.getInstance().getTimeInMillis());       // represent Y values
        List<Category> categories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT);

        for (int i = 0; i < categories.size(); i++){
            allCatNames.add(categories.get(i).getCatName());
            allCatIds.add(categories.get(i).getCatId());
        }

        mPieChart.setDescription(null);
        mPieChart.setDrawHoleEnabled(true);         // create the hole inside the PieChart
        mPieChart.setDrawSliceText(false);
        mPieChart.setHoleRadius(40);
        mPieChart.setTransparentCircleRadius(44);

        if (animateChart == true) mPieChart.animateY(1500);

        int notZeroCategoryCounter = 0;
        // add data to PieChart - Y - % values : X - name of pie slices
        for (int i = 0; i < dataForPie.length; i++){
            if (dataForPie[i] != 0) {
                yVals.add(new Entry(dataForPie[i], notZeroCategoryCounter));
                notZeroCategories.add(allCatNames.get(i));
                notZeroCategoriesID.add(allCatIds.get(i));
                notZeroCategoryCounter++;
            }
        }

        // create  PieDataSet - to
        pieDataSet = new PieDataSet(yVals, null);
        pieDataSet.setSliceSpace(0.5f);

        // add colors for PieChart
        colors.add(context.getResources().getColor(R.color.myPieColor1));
        colors.add(context.getResources().getColor(R.color.myPieColor2));
        colors.add(context.getResources().getColor(R.color.myPieColor3));
        colors.add(context.getResources().getColor(R.color.myPieColor4));
        colors.add(context.getResources().getColor(R.color.myPieColor5));
        colors.add(context.getResources().getColor(R.color.myPieColor6));
        colors.add(context.getResources().getColor(R.color.myPieColor7));
        colors.add(context.getResources().getColor(R.color.myPieColor8));
        colors.add(context.getResources().getColor(R.color.myPieColor9));
        pieDataSet.setColors(colors);

        // instantiate the PieData
        if (pieDataSet.getEntryCount() <= 0){
                Log.d("FINANCE DB: ", "Entered <=0 ");
            notZeroCategories.add("No expenses");
            pieData = new PieData(notZeroCategories, pieDataSet);
            pieData.addEntry(new Entry(0.0f, 0), 0);
                Log.d("FINANCE DB: ", " x: " + pieData.getXValCount() + " | y: " + pieData.getYValCount() + " " + pieData.getYMax());
        }
        else{
                Log.d("FINANCE DB: ", "Entered >0 ");
            pieData = new PieData(notZeroCategories, pieDataSet);
        }

        pieData.setValueFormatter(new PieValueFormatter(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY,"BGN")));    // format the data - ### ### ##0.0 CURRENCY
        pieData.setValueTextColor(context.getResources().getColor(R.color.myWhite));
        pieData.setValueTextSize(16);

        //set data, undo highlights and refresh the PieChart
        mPieChart.setData(pieData);
        mPieChart.highlightValues(null);
        mPieChart.setRotationAngle(30f);
        mPieChart.setRotationEnabled(false);

        //set value onSelect listener
        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                // e - is the entry selected, dataSetIndex - the ID of the selected entry from DataSet
                // h - is the position highlighted
                if (e == null) return;
                Intent intent = new Intent(context.getApplicationContext(), FirebaseDetailsActivity.class);
                intent.putExtra(KEY_DATE, Calendar.getInstance().getTimeInMillis());
                intent.putExtra(KEY_CAT_ID, notZeroCategoriesID.get(e.getXIndex()));
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        // customize legend for PieChart
        Legend legend = mPieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.PIECHART_CENTER);
        legend.setYOffset(-30f);
        legend.setTypeface(Typeface.DEFAULT);
        legend.setTextSize(14);
        legend.setTextColor(context.getResources().getColor(R.color.myBlack));

        mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();
    }

    // method to calculate current BALANCE and inflate text view with it
    public double calcBalance(long timeInMs, Context context){
        financeDbHelper = FinanceDbHelper.getInstance(context);
        double debitSum = 0;
        double creditSum = 0;
        double endBalance;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMs);

        // get today and clear time of day - 0h : 00m : 00s
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        // get start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = calendar.getTimeInMillis();

        // get start of the next month
        calendar.add(Calendar.MONTH, 1);
        long endOfMonth = calendar.getTimeInMillis();

        // fetch all DEBIT categories
        List<Category> debitCategories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT);
            Log.d("FINANCE DB:", " DEBIT count: " + debitCategories.size());

        //fetch sum for each DEBIT category
        for (int i = 0; i < debitCategories.size(); i++){
            debitSum = debitSum + financeDbHelper.sumTransByCategoryAndTime(debitCategories.get(i).getCatId(), startOfMonth, endOfMonth);
        }

        // fetch all CREDIT categories
        List<Category> creditCategories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_CREDIT);
            Log.d("FINANCE DB:", " CREDIT count: " + creditCategories.size());

        //fetch sum for each CREDIT category
        for (int i = 0; i < creditCategories.size(); i++){
            creditSum = creditSum + financeDbHelper.sumTransByCategoryAndTime(creditCategories.get(i).getCatId(), startOfMonth, endOfMonth);
        }

        endBalance = creditSum + debitSum;

            Log.d("FINANCE DB:", "DEBIT: " + debitSum + " : " + "CREDIT: " + creditSum);

        return endBalance;
    }

    public void addDebit(View view){
        Intent intent = new Intent(context, AddExpenseActivity.class);
        intent.putExtra(KEY_DATE, 0L);
        startActivity(intent);
    }

    public void addCredit(View view){
        Intent intent = new Intent(context, AddIncomeActivity.class);
        intent.putExtra(KEY_DATE, 0L);
        startActivity(intent);
    }

    public void DetailsActivity(View view){
        Intent intent = new Intent(context, FirebaseDetailsActivity.class);
        intent.putExtra(KEY_DATE, Calendar.getInstance().getTimeInMillis());
        intent.putExtra(KEY_CAT_ID, -1L);
        startActivity(intent);
    }

    public static void aboutDialog (Context context){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(R.string.about_dialog);
        alertDialogBuilder.setTitle(R.string.about_dialog_title);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setupPreferencesOnFirstUse(){
        SharedPreferences prefs = getSharedPreferences(Constants.KEY_PREF_FIRST_LAUNCH, Context.MODE_PRIVATE);
        if(prefs.getBoolean(Constants.KEY_PREF_FIRST_LAUNCH, true))
        {
            //first launch
            long id;
            Toast.makeText(getApplicationContext(),"Your first launch", Toast.LENGTH_SHORT).show();

            financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
            id = financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT, "House"));
            Log.d("FINANCE DB: ", "House : "+id);
            id = financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT, "Bills"));
            Log.d("FINANCE DB: ", " Bills : " + id);
            id =financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT, "Food"));
            Log.d("FINANCE DB: ", " Food : " + id);
            id =financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT, "Car"));
            Log.d("FINANCE DB: ", " Car : " + id);
            id =financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT, "Gifts"));
            Log.d("FINANCE DB: ", " Gifts : " + id);
            id =financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT, "Clothes"));
            Log.d("FINANCE DB: ", " Clothes : " + id);
            id =financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_CREDIT, "Salary"));
            Log.d("FINANCE DB: ", " Salary : " + id);
            id =financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_CREDIT, "Deposits"));
            Log.d("FINANCE DB: ", " Deposits : " + id);
            id =financeDbHelper.createCategory(new Category(FinanceContract.CategoriesEntry.CT_TYPE_CREDIT, "Savings"));
            Log.d("FINANCE DB: ", " Savings : " + id);

            id = financeDbHelper.createAcc(new Wallet("Cash"));
            Log.d("FINANCE DB: ", " Cash : " + id);
            id = financeDbHelper.createAcc(new Wallet("Debit card"));
            Log.d("FINANCE DB: ", " Debit Card : " + id);


            prefs.edit().putBoolean(Constants.KEY_PREF_FIRST_LAUNCH, false).apply();
        }
    }

    private void setupNavigationDrawer(){
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.main_nav_drawer);

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

                if (id == R.id.nav_main_current) {
                    Log.d("NAV","nav_main_current");
                } else if (id == R.id.nav_main_history) {
                    Intent historyActivity = new Intent(context, HistoryActivity.class);
                    startActivity(historyActivity);
                } else if (id == R.id.nav_main_analyze) {
                    Intent analyseIntent = new Intent(context, BarChartActivity.class);
                    startActivity(analyseIntent);
                } else if (id == R.id.nav_more_settings) {
                    Intent prefIntent = new Intent(context, SettingsActivity.class);
                    startActivity(prefIntent);
                } else if (id == R.id.nav_more_about) {
                    aboutDialog(context);
                } else if (id == R.id.nav_main_firebase) {
                    // Enter in firebase test activity
                    Intent firebaseIntent = new Intent(context, FirebaseMainActivity.class);
                    startActivity(firebaseIntent);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
}
