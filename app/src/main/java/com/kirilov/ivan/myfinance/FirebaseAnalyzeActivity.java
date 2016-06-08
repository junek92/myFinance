package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.sqlite_db.FinanceDbHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Ivan on 30-May-15.
 */
public class FirebaseAnalyzeActivity extends BaseActivity{



    //--- REBUILD ANALYZE   ---
    private RecyclerView categoriesRecyclerView;
    private GridImageNameAdapter categoriesGridAdapter;
    private GridLayoutManager categoriesGridLayoutManager;

    private Button btnDraw;

    public static long categoryId;
    private long lastSelectedCategoryId;
    private Map<Integer, Float> monthsValuesMap;
    private Map<Integer, Long> monthBeginMap;

    private BarChart barChart;
    private TextView mTextView;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity_analyze);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.analyse_toolbar);
        setSupportActionBar(toolbar);

        mTextView = (TextView) findViewById(R.id.analyse_date);
        mTextView.setText(Utilities.getYearInString(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis()));

        categoryId = 1;
        lastSelectedCategoryId = 0;
        btnDraw = (Button) findViewById(R.id.analyze_button);
        btnDraw.setActivated(false);

        monthsValuesMap = new HashMap<>();
        monthBeginMap = new HashMap<>();

        setupCategoriesRecycleView();
    }

    @Override
    protected void onResume(){
        super.onResume();

        lastSelectedCategoryId = 0;
        setupBarChart(monthsValuesMap, true);

        barChart.highlightValues(null);
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

    public void setupCategoriesRecycleView(){
        categoriesRecyclerView = (RecyclerView) findViewById(R.id.analyze_category_recycle_view);

        categoriesRecyclerView.setHasFixedSize(true);

        categoriesGridLayoutManager = new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(categoriesGridLayoutManager);

        categoriesGridAdapter = new GridImageNameAdapter(context, categoriesRecyclerView, (int) Constants.EXPENSE_CATEGORY_TYPE, (int) (categoryId -1));
        categoriesRecyclerView.setAdapter(categoriesGridAdapter);

        DatabaseReference referenceCategories = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_CATEGORIES_EXPENSE + "/");

        Query catQuery = referenceCategories.orderByKey();
        catQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        if (child.getValue(Category.class).getCatId() != 0L){
                            categoriesGridAdapter.addCategoryToList(child.getValue(Category.class));
                            categoriesGridAdapter.notifyDataSetChanged();
                        }
                    }

                    btnDraw.setActivated(true);
                } else {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                    FirebaseAnalyzeActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                FirebaseAnalyzeActivity.this.finish();
            }
        });

    }

    public void fetchData(View view){
        // Prevent spamming the draw button
        if (lastSelectedCategoryId != categoryId){
            // If the user has chosen a new category, then save its value
            lastSelectedCategoryId = categoryId;
        } else {
            Log.d("ANALYZE ", " ANTI SPAM :(");
            // If it's the old category, then do not do anything
            return;
        }

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
                + categoryId + "/");

        Query query = reference.orderByKey().startAt(Long.toString(startOfYearInMs)).endAt(Long.toString(endOfYearInMs));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // for every month TIMESTAMP
                for (DataSnapshot timestamp : dataSnapshot.getChildren()){
                    Float sumOfChildes = 0f;

                    // for every TRANSACTION in this TIMESTAMP
                    for (DataSnapshot transaction : timestamp.getChildren() ){
                        // add the transactions VALUE to the sum
                        sumOfChildes = sumOfChildes + (float) transaction.getValue(Transaction.class).getTrAmount();
                    }

                    // set calendars time to be == TIMESTAMPs key (e.g. beginning of the month)
                    calendar.setTimeInMillis(Long.valueOf(timestamp.getKey()));
                    // put the sum of transactions amounts in the HashMap with KEY = MONTH of the year
                    monthsValuesMap.put( calendar.get(Calendar.MONTH), sumOfChildes);
                    monthBeginMap.put(calendar.get(Calendar.MONTH), calendar.getTimeInMillis());
                }
                // pass all months data to setupBarChart()
                setupBarChart(monthsValuesMap, false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setupBarChart(Map<Integer, Float> inputData, boolean dataRefreshOnly){
        final ArrayList<String> xMonths = new ArrayList<>();
        ArrayList<BarEntry> yVals = new ArrayList<>();
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

        //TODO: make it to use HashMap!
        if (!inputData.isEmpty()){
            Log.d("ANALYZE ", " !inputData.isEmpty() ");
            for (int i = 0; i < 12; i++){
                // make all values positive
                if (inputData.get(i) < 0) inputData.put(i, -1 * inputData.get(i));

                yVals.add(new BarEntry(inputData.get(i), i));

//                Log.d("ANALYZE: ", " Number: " + inputData[i]);
            }
        }

        barChart = (BarChart) findViewById(R.id.analyse_bar_chart);
        barChart.setNoDataText(getResources().getString(R.string.analyse_no_data));
        barChart.setDescription("");

        if (!dataRefreshOnly){
            barChart.setDrawGridBackground(false);
            barChart.setBackgroundColor(context.getResources().getColor(R.color.myWhite));
            //barChart.setDrawValueAboveBar(true);
            barChart.setPinchZoom(false);
            barChart.setScaleEnabled(false);
            barChart.setScaleXEnabled(false);
            barChart.setPinchZoom(false);
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.animateY(1500);


            //  disable both Y axis + legend
            YAxis rightAxis = barChart.getAxisRight();
            rightAxis.setEnabled(false);

            YAxis leftAxis = barChart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinValue(0f);
            leftAxis.setEnabled(false);

            Legend legend = barChart.getLegend();
            legend.setEnabled(false);

            //format X axle
            XAxis bottomAxis = barChart.getXAxis();
            bottomAxis.setPosition(XAxis.XAxisPosition.TOP);
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

                        Wallet wallet = new Wallet();
                        wallet.setWalletName("Total Balance");
                        wallet.setWalletId(0L);

                        Intent intent = new Intent(context, FirebaseDetailsActivity.class);
                        intent.putExtra(Constants.EXTRA_WALLET, wallet);
                        intent.putExtra(Constants.EXTRA_DATE, monthBeginMap.get(e.getXIndex()));
                        startActivity(intent);

                    }
                }

                @Override
                public void onNothingSelected() {
                }
            });
            barChart.setData(barData);
            barChart.getBarData().setValueTextSize(12f);

            barChart.notifyDataSetChanged();
            barChart.invalidate();
        }

        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }
}
