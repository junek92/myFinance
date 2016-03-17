package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ivan on 30-May-15.
 */
public class AnalyseActivity extends AppCompatActivity{

    // used for time recognition and selection
    private long chosenDateInMs;
    private long chosenCatId;

    private List<Long> allYearsInMs;
    private List<String> allYearsInStrings;
    private float[] dataToDisplay;

    Context context;
    Toolbar toolbar;
    FinanceDbHelper financeDbHelper;
    Spinner spinnerCategory, spinnerYear;
    BarChart barChart;
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse);
        context = this;

//        //fetch the custom toolbar - set it as default, change the title
//        toolbar = (Toolbar) findViewById(R.id.analyse_app_bar);
//        toolbar.setTitle("");
//        //TextView toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_text);
//        //toolbarText.setText(R.string.analyse_title);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);

        inflateCategorySpinner();
        inflateYearSpinner();

        mTextView = (TextView) findViewById(R.id.analyse_date);

        dataToDisplay = new float[12];
        setupBarChart(dataToDisplay, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    // inflating spinner for categories and managing onSelect
    public void inflateCategorySpinner() {
        spinnerCategory = (Spinner) findViewById(R.id.analyse_category_spinner);
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT " + FinanceContract.CategoriesEntry.CAT_ID + " AS _id, "
                + FinanceContract.CategoriesEntry.CAT_NAME + ", "
                + FinanceContract.CategoriesEntry.CAT_TYPE + " FROM "
                + FinanceContract.CategoriesEntry.TABLE_NAME;

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        // A list of column names representing the data to bind to the UI
        String[] columnsToDisplay = new String[]{FinanceContract.CategoriesEntry.CAT_NAME};
        // The views that should display column in the "columnsToDisplay" parameter
        int[] listOfViews = new int[]{R.id.spinner_row};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.row_spinner, cursor, columnsToDisplay, listOfViews, 0);
        //simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(simpleCursorAdapter);
        //cursor.close();
    }

    // inflating spinner for years and managing onSelect
    public void inflateYearSpinner(){
        Calendar calendar = Calendar.getInstance();
        spinnerYear = (Spinner) findViewById(R.id.analyse_year_spinner);
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT " + FinanceContract.TransactionEntry.TR_ID + ", "
                + FinanceContract.TransactionEntry.TR_DATE + " FROM "
                + FinanceContract.TransactionEntry.TABLE_NAME;

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery,null);

        List<Transaction> transList = new ArrayList<>();

        if (cursor.moveToFirst()){
            do {
                Transaction transaction = new Transaction();
                transaction.setTrId(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ID)));
                transaction.setAtDate(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DATE)));
                transList.add(transaction);
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (transList.isEmpty()){
            Toast toast = Toast.makeText(context, "No data to analyse! Please enter some transactions first!", Toast.LENGTH_SHORT);
            TextView toastView = (TextView) toast.getView().findViewById(android.R.id.message);
            if (toastView != null) toastView.setGravity(Gravity.CENTER);
            toast.show();
            finish();
        } else {
            // fetching FIRST and LAST month
            long minTime = transList.get(0).getAtDate();
            long maxTime = transList.get(0).getAtDate();
            for (int i = 0; i < transList.size(); i++) {

                if (minTime >= transList.get(i).getAtDate()) {
                    minTime = transList.get(i).getAtDate();
                }

                if (maxTime <= transList.get(i).getAtDate()) {
                    maxTime = transList.get(i).getAtDate();
                }
            }
            Log.d("ANALYSE: ", "FirstMonth: " + minTime + "\n Last month: " + maxTime);

            calendar.setTimeInMillis(minTime);
            // get today and clear time of day - 0h : 00m : 00s
            calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
            calendar.clear(Calendar.MINUTE);
            calendar.clear(Calendar.SECOND);
            calendar.clear(Calendar.MILLISECOND);
            // get start of the month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            // get start of the year
            calendar.set(Calendar.MONTH, 0);
            // set minTime to be 01.01.minYear
            minTime = calendar.getTimeInMillis();

            calendar.setTimeInMillis(maxTime);
            // get today and clear time of day - 0h : 00m : 00s
            calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
            calendar.clear(Calendar.MINUTE);
            calendar.clear(Calendar.SECOND);
            calendar.clear(Calendar.MILLISECOND);
            // get start of the month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            // get start of the year
            calendar.set(Calendar.MONTH, 0);
            // set minTime to be 01.01.maxYear
            maxTime = calendar.getTimeInMillis();

            // get all first days in years between minTime and maxTime
            allYearsInMs = new ArrayList<>();
            allYearsInStrings = new ArrayList<>();

            long currentYear = minTime;
            do {
                allYearsInMs.add(currentYear);                                          // add current year in the array
                allYearsInStrings.add(financeDbHelper.getYearInString(currentYear));    // add current year string

                calendar.setTimeInMillis(currentYear);                                  // set 1st of current month in calendar
                Log.d("ANALYSE: ", "Added year 1st day: " + calendar.getTime());
                calendar.add(Calendar.YEAR, 1);                                         // add 1 year
                currentYear = calendar.getTimeInMillis();                               // set current date - 1st of +1 month
            } while (currentYear <= maxTime);

            Log.d("ANALYSE: ", "YEARS MS: " + allYearsInMs.toString() + "\n");
            Log.d("ANALYSE: ", "YEARS STRING: " + allYearsInStrings.toString() + "\n");

            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(context, R.layout.row_spinner, allYearsInStrings);
            //yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerYear.setAdapter(yearAdapter);
        }
    }

    public void fetchData(View view){
        if (chosenDateInMs == allYearsInMs.get(spinnerYear.getSelectedItemPosition()) && chosenCatId == spinnerCategory.getSelectedItemId()){
            Log.d("ANALYZE: ", "ANTI SPAM");
        } else {
            Calendar calendar = Calendar.getInstance();
            financeDbHelper = FinanceDbHelper.getInstance(context);
            long chosenYear = allYearsInMs.get(spinnerYear.getSelectedItemPosition());      // currently 01.01.XXXX

            long beginTime = chosenYear;
            calendar.setTimeInMillis(chosenYear);
            calendar.add(Calendar.MONTH, 1);
            long endTime = calendar.getTimeInMillis();
            dataToDisplay[0] = financeDbHelper.sumTransByCategoryAndTime(spinnerCategory.getSelectedItemId(), beginTime, endTime);

            for (int i = 0; i < 11; i++) {
                beginTime = endTime;
                calendar.setTimeInMillis(beginTime);
                calendar.add(Calendar.MONTH, 1);
                endTime = calendar.getTimeInMillis();
                dataToDisplay[i + 1] = financeDbHelper.sumTransByCategoryAndTime(spinnerCategory.getSelectedItemId(), beginTime, endTime);
                Log.d("ANALYSE: ", "Set: " + dataToDisplay[i + 1]);
            }
            chosenDateInMs = chosenYear;
            chosenCatId = spinnerCategory.getSelectedItemId();
            mTextView.setText(FinanceDbHelper.getInstance(context).getCategory(spinnerCategory.getSelectedItemId()).getCatName() + " - " + allYearsInStrings.get(spinnerYear.getSelectedItemPosition()));
            setupBarChart(dataToDisplay, false);
        }
    }

    public void setupBarChart(float[] inputData, boolean initialUse){
        ArrayList<BarEntry> yVals = new ArrayList<>();
        final ArrayList<String> xMonths = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        xMonths.add("Jan");
        xMonths.add("Feb");
        xMonths.add("Mar");
        xMonths.add("Apr");
        xMonths.add("May");
        xMonths.add("Jun");
        xMonths.add("Jul");
        xMonths.add("Aug");
        xMonths.add("Sep");
        xMonths.add("Oct");
        xMonths.add("Nov");
        xMonths.add("Dec");

        for (int i = 0; i < 12; i++){
            if (inputData[i] < 0) inputData[i] = -1 * inputData[i];
            yVals.add(new BarEntry(inputData[i], i));
                Log.d("ANALYZE: ", " Number: " + inputData[i]);
        }

        barChart = (BarChart) findViewById(R.id.analyse_bar_chart);
        barChart.setNoDataText(getResources().getString(R.string.analyse_no_data));
        barChart.setDescription("");
        if (initialUse == false){
            barChart.setDrawGridBackground(false);
            barChart.setBackgroundColor(context.getResources().getColor(R.color.myWhite));
            barChart.setDrawValueAboveBar(true);
            barChart.setPinchZoom(false);
            barChart.setScaleEnabled(false);
            barChart.setScaleXEnabled(false);
            barChart.setPinchZoom(false);
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.animateY(2500);

            //  disable both Y axis + legend
            YAxis rightAxis = barChart.getAxisRight();
            rightAxis.setEnabled(false);
            rightAxis.setDrawGridLines(false);
            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setEnabled(false);
            Legend legend = barChart.getLegend();
            legend.setEnabled(false);

            //format X axle
            XAxis bottomAxis = barChart.getXAxis();
            bottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            bottomAxis.setLabelsToSkip(0);

            // create BarDataSet and fill it with data + colors
            BarDataSet barDataSet = new BarDataSet(yVals, null);

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
            colors.add(context.getResources().getColor(R.color.myPieColor10));
            colors.add(context.getResources().getColor(R.color.myPieColor11));
            colors.add(context.getResources().getColor(R.color.myPieColor12));
            barDataSet.setColors(colors);

            BarData barData = new BarData(xMonths, barDataSet);

            barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    if (e == null) return;
                    if (e.getVal() > 0f){
                        // fetch Jan 1st XXXX year
                        long monthToPass = allYearsInMs.get(spinnerYear.getSelectedItemPosition());     // 01.01.XXXX
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(monthToPass);
                        // get selected month
                        calendar.set(Calendar.MONTH, e.getXIndex());
                        // pass selected month
                        monthToPass = calendar.getTimeInMillis();
                            Log.d("ANALYSE: ", monthToPass + " : " +calendar.getTime().toString() );

                        Intent intent = new Intent(context, TransDetailsActivity.class);
                        intent.putExtra(MainActivity.KEY_PREF_DATE, monthToPass);
                        intent.putExtra(MainActivity.KEY_PREF_CAT_ID, spinnerCategory.getSelectedItemId());
                        startActivity(intent);
                    }
                    //Toast.makeText(context, "YEAR: " + allYearsInMs.get(spinnerYear.getSelectedItemPosition()) + " MONTH: " + xMonths.get(e.getXIndex()) + " CATEGORY ID: " + spinnerCategory.getSelectedItemId() + " $$: " + e.getVal(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNothingSelected() {
                }
            });
            barChart.setData(barData);
            barChart.invalidate();
        }
        barChart.invalidate();
    }

}
