package com.kirilov.ivan.myfinance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 12-May-15.
 */
public class AddDebitActivity extends AppCompatActivity {


    private DatePickerDialog datePickerDialog;
    private long chosenDateInMs;

    Context context;
    Toolbar toolbar;
    FinanceDbHelper financeDbHelper;
    Spinner spinnerCategory, spinnerAcc;
    TextView timeText, amountText, descText, currencyText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debit);
        context = this;

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
            Log.d("TEST TIME"," " + chosenDateInMs);

        //fetch the custom toolbar - set it as default, change the title
        toolbar = (Toolbar) findViewById(R.id.addDebit_app_bar);
        toolbar.setTitle("");
        TextView toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_text);
        toolbarText.setText(R.string.addDebit_title);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){
            Log.d("EXCEPTION", " " + e.getMessage());
        }
        getSupportActionBar().setHomeButtonEnabled(true);

        timeText = (TextView) findViewById(R.id.addDebit_date);
        amountText = (TextView) findViewById(R.id.addDebit_editAmount);
        descText = (TextView) findViewById(R.id.addDebit_editDesc);
        currencyText = (TextView) findViewById(R.id.addDebit_textCurrency);

        // set MAX digits and number of digits AFTER decimal point
        amountText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(13, 2)});

        // set used currency
        currencyText.setText(PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.KEY_PREF_CURRENCY,"BGN"));

        // set time for the text view and create date picker dialog
        if (chosenDateInMs == 0){
            timeText.setText(FinanceDbHelper.getInstance(getApplicationContext()).getTimeInString(chosenDateInMs = Calendar.getInstance().getTimeInMillis(), false));
            Log.d("FINANCE DB: ", " chd = 0, f - " + chosenDateInMs);
        } else {
            timeText.setText(FinanceDbHelper.getInstance(getApplicationContext()).getTimeInString(chosenDateInMs, false));
            Log.d("FINANCE DB: ", " chd != 0, f - " + chosenDateInMs);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(chosenDateInMs);
        datePickerDialog = new DatePickerDialog(this,android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth ,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year,monthOfYear,dayOfMonth);
                chosenDateInMs = newDate.getTimeInMillis();
                timeText.setText(FinanceDbHelper.getInstance(getApplicationContext()).getTimeInString(chosenDateInMs, false));
                    Log.d("FINANCE DB: ", " " + chosenDateInMs);

            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        // initializing spinners
        inflateCategorySpinner();
        inflateAccSpinner();
    }

    @Override
    protected void onResume() {
        // called when activity is returned on top of stack
        super.onResume();
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
            Log.d("TEST TIME", "Entered HOME");
        }
        return true;
    }

    // add new Debit transaction - check input data and do the query
    public void addDebitTransaction(View view){
        Boolean NO_AMOUNT = false;
        Transaction transaction = new Transaction();

        transaction.setAtDate(chosenDateInMs);
        transaction.setAccUsed((int) spinnerAcc.getSelectedItemId());
        transaction.setCatUsed((int) spinnerCategory.getSelectedItemId());
        try {
            transaction.setAmountSpent(-1*Double.parseDouble(amountText.getText().toString()));
        }catch (NumberFormatException e){
            NO_AMOUNT = true;
            Toast.makeText(getApplicationContext(), "Please enter amount.",Toast.LENGTH_SHORT).show();
        }
        transaction.setCurrUsed(PreferenceManager.getDefaultSharedPreferences(context).getString(MainActivity.KEY_PREF_CURRENCY,"BGN"));
        transaction.setDescAdded(descText.getText().toString());

        if(checkTransaction(transaction, NO_AMOUNT)) {
            long transID = FinanceDbHelper.getInstance(getApplicationContext()).createTransaction(transaction);
                Log.d("FINANCE DB:", "TransID: " + transID);

            // debugging
            Log.d("FINANCE DB:", " " + transaction.getAtDate() + " " + transaction.getAccUsed() + " " + transaction.getCatUsed() +
                    " " + transaction.getAmountSpent() + " " + transaction.getCurrUsed() + " " + transaction.getDescAdded());

            finish();
        }
    }

    private boolean checkTransaction(Transaction transaction, boolean no_amount){
        if (transaction.getAtDate()== 0) return false;
        if (transaction.getAmountSpent() == 0.00 && !no_amount){
            Toast.makeText(getApplicationContext(), "Please enter not 0.00 amount.",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (no_amount) return false;

        return true;
    }
    // inflating spinner for categories and managing onSelect
    public void inflateCategorySpinner() {
        spinnerCategory = (Spinner) findViewById(R.id.addDebit_spinnerCat);
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT " + FinanceContract.CategoriesEntry.CAT_ID + " AS _id, "
                + FinanceContract.CategoriesEntry.CAT_NAME + ", "
                + FinanceContract.CategoriesEntry.CAT_TYPE + " FROM "
                + FinanceContract.CategoriesEntry.TABLE_NAME + " WHERE "
                + FinanceContract.CategoriesEntry.CAT_TYPE + " = "
                + FinanceContract.CategoriesEntry.CT_TYPE_DEBIT;

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

    // inflating spinner for accounts and managing onSelect
    public void inflateAccSpinner(){
        spinnerAcc = (Spinner) findViewById(R.id.addDebit_spinnerAcc);
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT " + FinanceContract.AccountsEntry.ACC_ID + " AS _id, "
                + FinanceContract.AccountsEntry.ACC_NAME + " FROM "
                + FinanceContract.AccountsEntry.TABLE_NAME;

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery,null);

        // A list of column names representing the data to bind to the UI
        String[] columnsToDisplay = new String[]{FinanceContract.AccountsEntry.ACC_NAME};
        // The views that should display column in the "columnsToDisplay" parameter
        int[] listOfViews = new int[]{R.id.spinner_row};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.row_spinner, cursor, columnsToDisplay, listOfViews, 0);
        //simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAcc.setAdapter(simpleCursorAdapter);
        //cursor.close();
    }


    class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
            mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher=mPattern.matcher(dest);
            if(!matcher.matches())
                return "";
            return null;
        }

    }


}
