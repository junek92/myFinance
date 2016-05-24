package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ivan on 18-May-15.
 */
public class PieMonthlyActivity extends AppCompatActivity {

    private long chosenDateInMs;

    private PieChart pieChart;
    private TextView textViewBalance, textViewDate;

    FinanceDbHelper financeDbHelper;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_monthly);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.pie_monthly_toolbar);
        setSupportActionBar(toolbar);

        // extract INTENT to get CHOSEN DATE
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

        textViewDate = (TextView) findViewById(R.id.pie_monthly_date);
        textViewBalance = (TextView) findViewById(R.id.pie_monthly_textBalance);
        pieChart = (PieChart) findViewById(R.id.pie_monthly_pieChart);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // AsyncTask to - calculate balance, set current date
        new PieActivityAsyncTask().execute();

        setupPieChart(pieChart, false);
    }

//---   Subclass to implement some background work - AsyncTask< PARAMS, PROGRESS, RESULT>
    private class PieActivityAsyncTask extends AsyncTask<Void, String, Void> {
    double endBalance;
    MainActivity mainActivity;

    @Override
    protected void onPreExecute() {
        mainActivity = new MainActivity();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        endBalance = mainActivity.calcBalance(chosenDateInMs, context);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // set chosen month in TextView
        textViewDate.setText(FinanceDbHelper.getInstance(context).getTimeInString(chosenDateInMs, true));

        // set calculated balance in Text View
        if (endBalance > 0)
            textViewBalance.setBackgroundColor(context.getResources().getColor(R.color.primaryColorDark));
        else
            textViewBalance.setBackgroundColor(context.getResources().getColor(R.color.accentColor));

        textViewBalance.setText(new PieValueFormatter(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(MainActivity.KEY_PREF_CURRENCY,"BGN"))
                .getFormattedValue((float) endBalance, null, 0, null));

        super.onPostExecute(aVoid);
    }
}

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

    // method to create, customize and inflate with data the PieChart, in parameters is array of floats wich contains Y values ( DATA )
    public void setupPieChart(PieChart mPieChart, boolean firstDraw){
        financeDbHelper = FinanceDbHelper.getInstance(context);
        ArrayList<String> allCatNames = new ArrayList<>();
        ArrayList<Long> allCatIds = new ArrayList<>();
        ArrayList<String> notZeroCategories = new ArrayList<>();
        final ArrayList<Long> notZeroCategoriesID = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        PieDataSet pieDataSet;
        PieData pieData;

        float[] dataForPie = extractDataForPie(chosenDateInMs);     // represent Y values
        List<Category> categories = financeDbHelper.getAllCategoriesByType(FinanceContract.CategoriesEntry.CT_TYPE_DEBIT);

        for (int i = 0; i < categories.size(); i++){
            allCatNames.add(categories.get(i).getCatName());
            allCatIds.add(categories.get(i).getCatId());
        }

        mPieChart.setDescription(null);
        mPieChart.setDrawHoleEnabled(true);         // create the hole inside the PieChart
        mPieChart.setDrawSliceText(false);
        mPieChart.setHoleRadius(40);
        mPieChart.setTransparentCircleRadius(43);

        if (firstDraw == true) mPieChart.animateY(1500);

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
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.PIECHART_CENTER);
        legend.setYOffset(-30f);
        legend.setTypeface(Typeface.DEFAULT);
        legend.setTextSize(14);
        legend.setTextColor(context.getResources().getColor(R.color.myBlack));

        mPieChart.notifyDataSetChanged();
        mPieChart.invalidate();
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

