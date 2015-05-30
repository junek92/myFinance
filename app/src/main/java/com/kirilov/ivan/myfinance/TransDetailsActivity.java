package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ivan on 19-May-15.
 */
public class TransDetailsActivity extends AppCompatActivity {
    //HashMap<Category, List<Transaction>> usedCategoriesMap;
    List<Category> usefulCategories;
    List<Transaction> allTrans;

    private long startOfMonth;
    private long endOfMonth;

    FinanceDbHelper financeDbHelper;
    ExpandableListView expandableListView;
    TransDetailsAdapter myAdapter;
    Context context;
    Toolbar toolbar;

    long chosenDateInMs;
    long chosenCat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_details);
        context = this;

        // extract INTENT to get chosen DATE
        if (savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if (extras == null){
                chosenDateInMs = 0;
                chosenCat = -1;
            }else {
                chosenDateInMs = extras.getLong(MainActivity.KEY_PREF_DATE);
                Log.d("CATEXP", " extras else " + chosenCat);
                chosenCat = extras.getLong(MainActivity.KEY_PREF_CAT_ID);
                Log.d("CATEXP", " extras else " + chosenCat);
            }
        } else {
            chosenDateInMs = savedInstanceState.getLong(MainActivity.KEY_PREF_DATE);
            chosenCat = savedInstanceState.getLong(MainActivity.KEY_PREF_CAT_ID);
            Log.d("CATEXP", " saved else " + chosenCat);
        }
            Log.d("TEST TIME", " " + chosenDateInMs);

        //fetch the custom toolbar - set it as default, change the title
        toolbar = (Toolbar) findViewById(R.id.details_app_bar);
        toolbar.setTitle("");
        TextView toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_text);
        toolbarText.setText(R.string.trans_details_title);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){
            Log.d("EXCEPTION", " " + e.getMessage());
        }
        getSupportActionBar().setHomeButtonEnabled(true);

        inflateWithData(chosenDateInMs);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            Log.d("TEST TIME", "Entered HOME");
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        inflateWithData(chosenDateInMs);
    }

    public void inflateWithData(long chosenDateInMs){
            Log.d("TEST TIME", "CALLED INFLATEWITHDATA");
        // extract 1 ST and LAST day of month
        getMonthDates(chosenDateInMs);

        // get needed categories
        usefulCategories = orderedCategories();

        expandableListView = (ExpandableListView) findViewById(R.id.details_expand);
        myAdapter = new TransDetailsAdapter(context, getLayoutInflater(), usefulCategories, startOfMonth, endOfMonth);
        expandableListView.setAdapter(myAdapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(context, TransUpdateActivity.class);
                intent.putExtra(MainActivity.KEY_PREF_TRANS_ID, id);
                startActivity(intent);
                return false;
            }
        });

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });

        if (chosenCat != -1) {
            int groupToExpand = 0;
            for (int i = 0; i < usefulCategories.size(); i++) {
                if (chosenCat == usefulCategories.get(i).getCatId()) {
                    groupToExpand = i;
                    break;
                }
            }
            expandableListView.expandGroup(groupToExpand);
        }
    }
    public void getMonthDates(long dateInMs){
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(dateInMs);
        // get today and clear time of day - 0h : 00m : 00s
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        // get start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        this.startOfMonth = calendar.getTimeInMillis();
            //Log.d("FINANCE DB:","Start of the month:       " + calendar.getTime());
            //Log.d("FINANCE DB:", "... in milliseconds:      " + calendar.getTimeInMillis());

        // get start of the next month
        calendar.add(Calendar.MONTH, 1);
        this.endOfMonth = calendar.getTimeInMillis();
            //Log.d("FINANCE DB:","Start of the next month:  " + calendar.getTime());
            //Log.d("FINANCE DB:","... in milliseconds:      " + calendar.getTimeInMillis());
    }

    public List<Category> orderedCategories(){
        List<Category> categories = new ArrayList<>();
        Set<Integer> catID = new HashSet<>();

        financeDbHelper = FinanceDbHelper.getInstance(context);

        allTrans = financeDbHelper.getTransByTime(startOfMonth, endOfMonth);

        for (int i = 0; i<allTrans.size(); i++){
            catID.add(allTrans.get(i).getCatUsed());
        }

        List<Integer> catIdList = new ArrayList<>(catID);
        for (int i = 0; i < catID.size(); i++){
            categories.add(financeDbHelper.getCategory(catIdList.get(i)));
        }

        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(Category lhs, Category rhs) {
                int cmp = lhs.getCatType() - rhs.getCatType();
                    Log.d("COMPARE", " LHS - RHS = CMP : " + lhs.getCatType() + " - " +rhs.getCatType() + " = " +cmp );
                return cmp;
            }
        });
        Collections.reverse(categories);

        return categories;
    }
}
