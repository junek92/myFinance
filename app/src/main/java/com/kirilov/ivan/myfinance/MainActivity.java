package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String  KEY_PREFS_FIRST_LAUNCH = "first_launch";
    public static String        KEY_PREF_CURRENCY = "used_currency";
    public static String        KEY_PREF_DATE = "chosenDate";
    public static String        KEY_PREF_TRANS_ID = "transId";
    public static String        KEY_PREF_CAT_ID = "catId";

    // used for navigation drawer
    private ListView                mDrawerList;
    private NavigationAdapter       mNavigationAdapter;
    private ActionBarDrawerToggle   mDrawerToggle;
    private DrawerLayout            mDrawerLayout;

    // used for PieChart drawing
    private PieChart    pieChart;
    private float[]     dataForPie;          // y - represents % values

    // used to display current balance
    private TextView textViewBalance;

    FinanceDbHelper financeDbHelper;
    Context         context;
//    Toolbar         toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //on app first launch - do certain stuff
        firstLaunch();

        //fetch the custom toolbar - set it as default, change the title
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
//        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);


        //---       NEW NAVIGATION TEST
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.main_nav_drawer);
        navigationView.setCheckedItem(R.id.nav_this_month);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

//                if (id == R.id.nav_this_month) {
//
//                } else
                if (id == R.id.nav_history) {
                    Intent historyActivity = new Intent(context, HistoryActivity.class);
                    startActivity(historyActivity);
                } else if (id == R.id.nav_analyze) {
                    Intent analyseIntent = new Intent(context, AnalyseActivity.class);
                    startActivity(analyseIntent);
                } else if (id == R.id.nav_settings) {
                    Intent prefIntent = new Intent(context, SettingsActivity.class);
                    startActivity(prefIntent);
                } else if (id == R.id.nav_about) {
                    aboutDialog(context);
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });



        // set current date in TextView
        TextView textViewDate = (TextView) findViewById(R.id.main_date);
        textViewDate.setText(FinanceDbHelper.getInstance(this).getTimeInString(Calendar.getInstance().getTimeInMillis(), true));

        // set current balance in TextView
        textViewBalance = (TextView) findViewById(R.id.main_textBalance);
        calcBalance(textViewBalance, Calendar.getInstance().getTimeInMillis());

        // create, configure the PieChart
        pieChart = (PieChart) findViewById(R.id.main_pieChart);
//        dataForPie = extractDataForPie(Calendar.getInstance().getTimeInMillis());
//        setupPieChart(dataForPie, pieChart, true);
    }

    @Override
    protected void onDestroy() {
        // closes any open DB object
        financeDbHelper.close();
        Log.d("FINANCE DB: ", "DB closed");

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final NavigationView navigationView = (NavigationView) findViewById(R.id.main_nav_drawer);
        navigationView.setCheckedItem(R.id.nav_this_month);

        // calculate balance
        calcBalance(textViewBalance, Calendar.getInstance().getTimeInMillis());

        // clear the highlights in PieChart and fill it with data

        dataForPie = extractDataForPie(Calendar.getInstance().getTimeInMillis());
        setupPieChart(dataForPie, pieChart, false);
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // returns array of SUMS for each DEBIT category, in parameter is time in ms to fetch the MONTH
    public float[] extractDataForPie(long timeInMs){
        financeDbHelper = FinanceDbHelper.getInstance(this);
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
    public void setupPieChart(float[] dataValues, PieChart mPieChart, boolean firstDraw){
        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<String> allCategories = new ArrayList<>();
        ArrayList<String> notZeroCategories = new ArrayList<>();
        ArrayList<Long> allCategoriesID = new ArrayList<>();
        final ArrayList<Long> notZeroCategoriesID = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        PieDataSet pieDataSet;
        PieData pieData;

        mPieChart.setDescription(null);
        mPieChart.setDrawHoleEnabled(true);         // create the hole inside the PieChart
        mPieChart.setHoleColorTransparent(true);
        mPieChart.setDrawSliceText(false);
        mPieChart.setHoleRadius(40);
        mPieChart.setTransparentCircleRadius(43);

        if (firstDraw == true) mPieChart.animateY(2500);

        // fetch all names of the categories for X
        financeDbHelper = FinanceDbHelper.getInstance(this);
        SQLiteDatabase sqLiteDatabase = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT " + FinanceContract.CategoriesEntry.CAT_ID + " AS _id, "
                + FinanceContract.CategoriesEntry.CAT_NAME + ", "
                + FinanceContract.CategoriesEntry.CAT_TYPE + " FROM "
                + FinanceContract.CategoriesEntry.TABLE_NAME + " WHERE "
                + FinanceContract.CategoriesEntry.CAT_TYPE + " = "
                + FinanceContract.CategoriesEntry.CT_TYPE_DEBIT;

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()){
            do{
                allCategories.add(cursor.getString(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_NAME)));
                allCategoriesID.add(cursor.getLong(cursor.getColumnIndex("_id")));
            }while (cursor.moveToNext());
        }

        int notZeroCategoryCounter = 0;
        // add data to PieChart - Y - % values : X - name of pie slices
        for (int i = 0; i < dataValues.length; i++){
            if (dataValues[i] != 0) {
                yVals.add(new Entry(dataValues[i], notZeroCategoryCounter));
                notZeroCategories.add(allCategories.get(i));
                notZeroCategoriesID.add(allCategoriesID.get(i));
                notZeroCategoryCounter++;
            }
        }

        // create  PieDataSet - to
        pieDataSet = new PieDataSet(yVals, null);
        pieDataSet.setSliceSpace(0.1f);

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

        pieData.setValueFormatter(new PieValueFormatter(PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.KEY_PREF_CURRENCY,"BGN")));    // format the data - ### ### ##0.0 CURRENCY
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
                Intent intent = new Intent(context.getApplicationContext(), TransDetailsActivity.class);
                intent.putExtra(KEY_PREF_DATE, Calendar.getInstance().getTimeInMillis());
                intent.putExtra(KEY_PREF_CAT_ID, notZeroCategoriesID.get(e.getXIndex()));
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        // customize legend for PieChart
        Legend legend = mPieChart.getLegend();
        legend.setPosition(Legend.LegendPosition.PIECHART_CENTER);
        legend.setTextSize(14);
        legend.setXEntrySpace(7);
        legend.setYEntrySpace(5);
        legend.setTypeface(Typeface.DEFAULT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextColor(context.getResources().getColor(R.color.myBlack));

        Log.d("FINANCE DB: ", "ENTRYs: \n" + pieData.getDataSet().toString());
        //mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();
    }

    // method to calculate current BALANCE and inflate text view with it
    public void calcBalance(TextView textView, long timeInMs){
        financeDbHelper = FinanceDbHelper.getInstance(this);
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

            Log.d("FINANCE DB:", "DEBIT: " + debitSum + " : " + "CREDIT: " + creditSum);
        String rawText = new PieValueFormatter(PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.KEY_PREF_CURRENCY,"BGN")).getFormattedValue((float) (endBalance = creditSum + debitSum));
        if (endBalance > 0)
            textView.setBackgroundColor(context.getResources().getColor(R.color.primaryColorDark));
        else
            textView.setBackgroundColor(context.getResources().getColor(R.color.accentColor));

        textView.setText(rawText);
    }

    public void addDebit(View view){
        Intent intent = new Intent(context, AddDebitActivity.class);
        intent.putExtra(KEY_PREF_DATE, 0l);
        startActivity(intent);
    }

    public void addCredit(View view){
        Intent intent = new Intent(context, AddCreditActivity.class);
        intent.putExtra(KEY_PREF_DATE, 0l);
        startActivity(intent);
    }

    public void DetailsActivity(View view){
        Intent intent = new Intent(context, TransDetailsActivity.class);
        intent.putExtra(KEY_PREF_DATE, Calendar.getInstance().getTimeInMillis());
        intent.putExtra(KEY_PREF_CAT_ID, -1l);
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

    public void firstLaunch(){
        SharedPreferences prefs = getSharedPreferences(KEY_PREFS_FIRST_LAUNCH, Context.MODE_PRIVATE);
        if(prefs.getBoolean(KEY_PREFS_FIRST_LAUNCH, true))
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

            id = financeDbHelper.createAcc(new Account("Cash"));
            Log.d("FINANCE DB: ", " Cash : " + id);
            id = financeDbHelper.createAcc(new Account("Debit card"));
            Log.d("FINANCE DB: ", " Debit Card : " + id);


            prefs.edit().putBoolean(KEY_PREFS_FIRST_LAUNCH, false).apply();
        }
    }
}
