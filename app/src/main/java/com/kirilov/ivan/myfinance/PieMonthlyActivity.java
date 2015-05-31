package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

/**
 * Created by Ivan on 18-May-15.
 */
public class PieMonthlyActivity extends AppCompatActivity {

    private long chosenDateInMs;

    // used for PieChart drawing
    private PieChart pieChart;
    private float[] dataForPie;          // y - represents % values

    // used to display current balance
    private TextView textViewBalance;

    FinanceDbHelper financeDbHelper;
    Context context;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_monthly);
        context = this;

        //fetch the custom toolbar - set it as default, change the title
        toolbar = (Toolbar) findViewById(R.id.pie_monthly_app_bar);
        toolbar.setTitle("");
        TextView toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_text);
        toolbarText.setText(R.string.pie_monthly_title);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // extract INTENT to get chosen DATE
        if (savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if (extras == null){
                chosenDateInMs = 0;
            }else {
                chosenDateInMs = extras.getLong(MainActivity.KEY_PREF_DATE);
            }
        } else {
            chosenDateInMs = savedInstanceState.getLong(MainActivity.KEY_PREF_DATE);
        }
            Log.d("TEST TIME", " " + chosenDateInMs);

        // set chosen month in TextView
        TextView textViewDate = (TextView) findViewById(R.id.pie_monthly_date);
        textViewDate.setText(FinanceDbHelper.getInstance(this).getTimeInString(chosenDateInMs, true));

        // set current balance in TextView
        textViewBalance = (TextView) findViewById(R.id.pie_monthly_textBalance);
        calcBalance(textViewBalance, chosenDateInMs);

        // create, configure the PieChart
        pieChart = (PieChart) findViewById(R.id.pie_monthly_pieChart);
        dataForPie = extractDataForPie(chosenDateInMs);
        setupPieChart(dataForPie, pieChart, true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // calculate balance
        calcBalance(textViewBalance, chosenDateInMs);

        // clear the highlights in PieChart and fill it with data
        dataForPie = extractDataForPie(chosenDateInMs);
        setupPieChart(dataForPie, pieChart, false);
        pieChart.highlightValues(null);
        pieChart.invalidate();
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

    // method to create, customize and inflate with data the PieChart, in parameters is array of floats wich contains Y values ( DATA )
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
        financeDbHelper = FinanceDbHelper.getInstance(context);
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

        // create  PieDataSet
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
                Intent intent = new Intent(context, TransDetailsActivity.class);
                intent.putExtra(MainActivity.KEY_PREF_DATE, chosenDateInMs);
                intent.putExtra(MainActivity.KEY_PREF_CAT_ID, notZeroCategoriesID.get(e.getXIndex()));
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
        intent.putExtra(MainActivity.KEY_PREF_DATE, chosenDateInMs+1000l);
        startActivity(intent);
    }

    public void addCredit(View view){
        Intent intent = new Intent(context, AddCreditActivity.class);
        intent.putExtra(MainActivity.KEY_PREF_DATE, chosenDateInMs+1000l);
        startActivity(intent);
    }
    public void DetailActivity (View view) {
        Intent intent = new Intent(context, TransDetailsActivity.class);
        intent.putExtra(MainActivity.KEY_PREF_DATE, chosenDateInMs);
        intent.putExtra(MainActivity.KEY_PREF_CAT_ID, -1l);
        startActivity(intent);
    }
}

