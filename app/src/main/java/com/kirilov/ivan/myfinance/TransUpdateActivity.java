package com.kirilov.ivan.myfinance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
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

import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.sqlite_db.FinanceContract;
import com.kirilov.ivan.myfinance.sqlite_db.FinanceDbHelper;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 19-May-15.
 */
public class TransUpdateActivity extends BaseActivity {

    private DatePickerDialog datePickerDialog;
    private long chosenDateInMs;
    private long chosenTransId;

    Spinner spinnerCategory, spinnerAcc;
    TextView timeText, amountText, descText, currencyText;

    Cursor cursorCat, cursorAcc;
    Context context;
    Toolbar toolbar;

    FinanceDbHelper financeDbHelper;
    Transaction myTrans;
    int transType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_update);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.transaction_update_toolbar);
        setSupportActionBar(toolbar);

        // extract INTENT to get chosen DATE, ID
        if (savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if (extras == null){
                //chosenDateInMs = 0;
                chosenTransId = 0;
            }else {
               // chosenDateInMs = extras.getLong(MainActivity.KEY_DATE);
                chosenTransId = extras.getLong(MainActivity.KEY_TRANS_ID);
            }
        } else {
            //chosenDateInMs = savedInstanceState.getLong(MainActivity.KEY_DATE);
            chosenTransId = savedInstanceState.getLong(MainActivity.KEY_TRANS_ID);
        }

//        //fetch the custom toolbar - set it as default, change the title
//        toolbar = (Toolbar) findViewById(R.id.trans_update_app_bar);
//        toolbar.setTitle("");
//        //TextView toolbarText = (TextView) toolbar.findViewById(R.id.toolbar_text);
//       // toolbarText.setText(R.string.trans_update_title);
//        setSupportActionBar(toolbar);
//
//        try {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }catch (NullPointerException e){
//            Log.d("EXCEPTION", " " + e.getMessage());
//        }
//        getSupportActionBar().setHomeButtonEnabled(true);

        timeText = (TextView) findViewById(R.id.trans_update_date);
        amountText = (TextView) findViewById(R.id.trans_update_editAmount);
        descText = (TextView) findViewById(R.id.trans_update_editDesc);
        currencyText = (TextView) findViewById(R.id.trans_update_textCurrency);

        // set MAX digits and number of digits AFTER decimal point
        amountText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(13,2)});

        // set used currency
        currencyText.setText(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY,"BGN"));

        // get selected transaction and inflate fields
        financeDbHelper = FinanceDbHelper.getInstance(context);
        myTrans = financeDbHelper.getTransaction(chosenTransId);
        chosenDateInMs = myTrans.getTrDate();

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
                newDate.set(year, monthOfYear, dayOfMonth);
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

        if (myTrans.getTrAmount() < 0){
            transType = FinanceContract.CategoriesEntry.CT_TYPE_DEBIT;      // =1 => -
            amountText.setText(Double.toString(-1 * myTrans.getTrAmount()));
        } else {
            transType = FinanceContract.CategoriesEntry.CT_TYPE_CREDIT;     // =2 => +
            amountText.setText(Double.toString(myTrans.getTrAmount()));
        }

        descText.setText(myTrans.getTrDesc());

        // initializing spinners
        inflateCategorySpinner(transType);
        inflateAccSpinner();

        //spinnerCategory.setSelection(getIndex(spinnerCategory, ));



    }

    @Override
    protected void onDestroy() {
        cursorCat.close();
        cursorAcc.close();
        super.onDestroy();
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


    // inflating spinner for categories and managing onSelect
    public void inflateCategorySpinner(int transType) {
        spinnerCategory = (Spinner) findViewById(R.id.trans_update_spinnerCat);
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT " + FinanceContract.CategoriesEntry.CAT_ID + " AS _id, "
                + FinanceContract.CategoriesEntry.CAT_NAME + ", "
                + FinanceContract.CategoriesEntry.CAT_TYPE + " FROM "
                + FinanceContract.CategoriesEntry.TABLE_NAME + " WHERE "
                + FinanceContract.CategoriesEntry.CAT_TYPE + " = "
                + transType;

        cursorCat = sqLiteDatabase.rawQuery(selectQuery, null);

        // A list of column names representing the data to bind to the UI
        String[] columnsToDisplay = new String[]{FinanceContract.CategoriesEntry.CAT_NAME};
        // The views that should display column in the "columnsToDisplay" parameter
        int[] listOfViews = new int[]{R.id.spinner_row};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.row_spinner, cursorCat, columnsToDisplay, listOfViews, 0);
        //simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(simpleCursorAdapter);
        for(int i = 0; i < simpleCursorAdapter.getCount(); i++)
        {
            Log.d("SPINNER", "IID: "+simpleCursorAdapter.getItemId(i) + "  TID:" + myTrans.getTrCat());
            if (simpleCursorAdapter.getItemId(i) == myTrans.getTrCat() )
            {
                    Log.d("SPINNER", "SCA-ItemID: "+simpleCursorAdapter.getItemId(i) + "  TRANS ID:" + myTrans.getTrCat());
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    // inflating spinner for accounts and managing onSelect
    public void inflateAccSpinner(){
        spinnerAcc = (Spinner) findViewById(R.id.trans_update_spinnerAcc);
        financeDbHelper = FinanceDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = financeDbHelper.getReadableDatabase();

        String selectQuery = "SELECT " + FinanceContract.AccountsEntry.ACC_ID + " AS _id, "
                + FinanceContract.AccountsEntry.ACC_NAME + " FROM "
                + FinanceContract.AccountsEntry.TABLE_NAME;

        cursorAcc = sqLiteDatabase.rawQuery(selectQuery,null);

        // A list of column names representing the data to bind to the UI
        String[] columnsToDisplay = new String[]{FinanceContract.AccountsEntry.ACC_NAME};
        // The views that should display column in the "columnsToDisplay" parameter
        int[] listOfViews = new int[]{R.id.spinner_row};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.row_spinner, cursorAcc, columnsToDisplay, listOfViews, 0);
        //simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAcc.setAdapter(simpleCursorAdapter);
        for(int i = 0; i < simpleCursorAdapter.getCount(); i++)
        {
            if (simpleCursorAdapter.getItemId(i) == myTrans.getTrWallet() )
            {
                spinnerAcc.setSelection(i);
                break;
            }
        }
    }

    public void btnSave(View view){
        double amount = 0;
        Boolean NO_AMOUNT = false;
        final Transaction updateTrans = new Transaction();
        financeDbHelper = FinanceDbHelper.getInstance(context);

        updateTrans.setTrId(myTrans.getTrId());
        updateTrans.setTrDate(chosenDateInMs);
        updateTrans.setTrWallet((int) spinnerAcc.getSelectedItemId());
        updateTrans.setTrCat((int) spinnerCategory.getSelectedItemId());

        try {
            amount = Double.parseDouble(amountText.getText().toString());
        }catch (NumberFormatException e){
            NO_AMOUNT = true;
            Toast.makeText(getApplicationContext(), "Please enter amount.", Toast.LENGTH_SHORT).show();
        }

        if (transType == FinanceContract.CategoriesEntry.CT_TYPE_DEBIT){
            updateTrans.setTrAmount(-1 * amount);
        } else {
            updateTrans.setTrAmount(amount);
        }

        updateTrans.setTrCurrency(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY,"BGN"));
        updateTrans.setTrDesc(descText.getText().toString());


        if(checkTransaction(updateTrans, NO_AMOUNT)) {

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.trans_update_save_dialog_title);
            alertDialogBuilder.setPositiveButton(R.string.trans_update_save_dialog_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    long transID = financeDbHelper.updateTrans(updateTrans);
                    if (transID > 0){
                        Toast.makeText(context, "Successfully updated "+transID+" transaction!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error with updating "+transID+" transaction!", Toast.LENGTH_SHORT).show();
                    }
                    // debugging
                    Log.d("UPDATE DB:", " " + updateTrans.getTrDate() + " " + updateTrans.getTrWallet() + " " + updateTrans.getTrCat() +
                            " " + updateTrans.getTrAmount() + " " + updateTrans.getTrCurrency() + " " + updateTrans.getTrDesc());
                    finish();
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.trans_update_save_dialog_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void btnCancel(View view){
        finish();
    }

    public void btnDelete(View view){
        financeDbHelper = FinanceDbHelper.getInstance(context);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.trans_update_delete_dialog_title);
        alertDialogBuilder.setPositiveButton(R.string.trans_update_delete_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int transID = financeDbHelper.deleteTrans(myTrans.getTrId());
                if (transID > 0){
                    Toast.makeText(context, "Successfully deleted "+transID+" transaction!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error with deleting "+transID+" transaction!", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.trans_update_save_dialog_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean checkTransaction(Transaction transaction, boolean no_amount){
        if (transaction.getTrDate()== 0) return false;
        if (transaction.getTrAmount() == 0.00 && !no_amount){
            Toast.makeText(getApplicationContext(), "Please enter not 0.00 amount.",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (no_amount) return false;

        return true;
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
