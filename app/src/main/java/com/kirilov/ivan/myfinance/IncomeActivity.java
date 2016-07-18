package com.kirilov.ivan.myfinance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.adapters.GridImageNameAdapter;
import com.kirilov.ivan.myfinance.firebase_model.Category;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 03-Jun-16.
 */
public class IncomeActivity extends BaseActivity {

    private DatePickerDialog datePickerDialog;
    private TextInputEditText amountText, descriptionText;
    private TextView currencyText, dateText;

    private RecyclerView walletsRecycleView, categoriesRecyclerView;
    private GridImageNameAdapter walletsGridAdapter, categoriesGridAdapter;
    private GridLayoutManager walletsGridLayoutManager, categoriesGridLayoutManager;

    private long chosenDateInMs;

    private Transaction extraTransaction, transToDelete;

    public static long walletId;
    public static long categoryId;
    private boolean isNewTransaction;

    Toolbar toolbar;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);
        context = this;
        isNewTransaction = true;

        Intent intent = getIntent();
        if (!intent.hasExtra(Constants.EXTRA_TRANSACTION)){
            Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            finish();
        }
        extraTransaction = intent.getParcelableExtra(Constants.EXTRA_TRANSACTION);

        toolbar = (Toolbar) findViewById(R.id.income_toolbar);
        setSupportActionBar(toolbar);

        amountText = (TextInputEditText) findViewById(R.id.income_amount);
        descriptionText = (TextInputEditText) findViewById(R.id.income_description);

        if (extraTransaction.getTrAmount() != 0d){
            isNewTransaction = false;

            Button btnSave = (Button) findViewById(R.id.income_btn_add);
            btnSave.setText(getResources().getString(R.string.income_btn_save));

            Button btnDelete = (Button) findViewById(R.id.income_btn_delete);
            btnDelete.setVisibility(View.VISIBLE);

            amountText.setText(Double.toString(extraTransaction.getTrAmount()));
            descriptionText.setText(extraTransaction.getTrDesc());

            transToDelete = new Transaction(
                    extraTransaction.getTrId(),
                    extraTransaction.getTrDate(),
                    extraTransaction.getTrWallet(),
                    extraTransaction.getTrType(),
                    extraTransaction.getTrCat(),
                    extraTransaction.getTrAmount(),
                    extraTransaction.getTrCurrency(),
                    extraTransaction.getTrDesc()
            );
        }

        chosenDateInMs = extraTransaction.getTrDate();
        walletId = extraTransaction.getTrWallet();
        categoryId = extraTransaction.getTrCat();

        amountText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.myWhite), PorterDuff.Mode.SRC_ATOP);
        descriptionText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.myWhite), PorterDuff.Mode.SRC_ATOP);

        currencyText = (TextView) findViewById(R.id.income_currency);
        currencyText.setText(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD"));

        // filter input of decimals
        amountText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(7,2)});

        // clear focus on DONE clicked
        amountText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    amountText.clearFocus();
                }
                return false;
            }
        });
        // hide the soft keyboard
        amountText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(amountText.getWindowToken(), 0);
            }
        });


        // clear focus on DONE clicked
        descriptionText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    descriptionText.clearFocus();
                }
                return false;
            }
        });
        // hide the soft keyboard
        descriptionText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(descriptionText.getWindowToken(), 0);
            }
        });

        setupWalletsRecycleView();
        setupCategoriesRecycleView();
        setupDatePickerDialog();

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (amountText.hasFocus()){
            amountText.clearFocus();
        } else if (descriptionText.hasFocus()){
            descriptionText.clearFocus();
        } else {
            super.onBackPressed();
        }
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

    public void setupWalletsRecycleView(){
        walletsRecycleView = (RecyclerView) findViewById(R.id.income_wallet_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        walletsRecycleView.setHasFixedSize(true);

        walletsGridLayoutManager = new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false);
        walletsRecycleView.setLayoutManager(walletsGridLayoutManager);

        // default wallet IF not chosen by the user is 1, but its on position ID-1 (because Total Balance is missing)
        //TODO: First must find first wallet ID, it might not be 1 - always
        walletsGridAdapter = new GridImageNameAdapter(context, walletsRecycleView, 3, (int) (walletId - 1));
        walletsRecycleView.setAdapter(walletsGridAdapter);

        DatabaseReference referenceWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/");

        Query query = referenceWallets.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        if (child.getValue(Wallet.class).getWalletId() != 0L){
                            walletsGridAdapter.addWalletToList(child.getValue(Wallet.class));
                            walletsGridAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                    IncomeActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                IncomeActivity.this.finish();
            }
        });
    }

    public void setupCategoriesRecycleView(){
        int gridRowNumber;
        categoriesRecyclerView = (RecyclerView) findViewById(R.id.income_category_recycle_view);

        categoriesRecyclerView.setHasFixedSize(true);

        // Change the number of grid rows, according to the screen DPI
        if (screenDpi <= 320){
            gridRowNumber = 1;
        } else {
            gridRowNumber = 2;
        }

        categoriesGridLayoutManager = new GridLayoutManager(context, gridRowNumber, GridLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(categoriesGridLayoutManager);

        //TODO: First must find first cat ID, it might not be 1 - always
        categoriesGridAdapter = new GridImageNameAdapter(context, categoriesRecyclerView, (int) Constants.INCOME_CATEGORY_TYPE, (int) (categoryId - 1));
        categoriesRecyclerView.setAdapter(categoriesGridAdapter);

        DatabaseReference referenceCategories = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_CATEGORIES_INCOME + "/");

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
                } else {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                    IncomeActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                IncomeActivity.this.finish();
            }
        });
    }

    private void setupDatePickerDialog(){
        dateText = (TextView) findViewById(R.id.income_date);
        dateText.setText(Utilities.getTimeInString(chosenDateInMs, false));

        // create a calendar and set the time to be = chosenTimeInMs
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(chosenDateInMs);

        // create new DatePickerDialog and inflate it
        datePickerDialog = new DatePickerDialog(this,R.style.datePickerDialogIncome,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

                        // prevent the user to add transactions in the future
                        if (year > newDate.get(Calendar.YEAR)){
                            // it's future year => return
                            Toast.makeText(context, "Can't add transactions in future!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (year == newDate.get(Calendar.YEAR)){
                            // it's this year => check if it's future month
                            if (monthOfYear > newDate.get(Calendar.MONTH)){
                                // it's future month => return
                                Toast.makeText(context, "Can't add transactions in future!", Toast.LENGTH_SHORT).show();
                                return;
                            }   //ELSE: it's current or past month => OK
                        }//ELSE: it's past year => OK

                        newDate.set(year, monthOfYear, dayOfMonth);

                            Log.d("DATE TESTING", " newDate.set: -> " + newDate.toString());
                        chosenDateInMs = newDate.getTimeInMillis();

                        extraTransaction.setTrDate(chosenDateInMs);
                        extraTransaction.setTrId(chosenDateInMs);

                        dateText.setText(Utilities.getTimeInString(chosenDateInMs, false));

                            Log.d("DATE TESTING", " chosenDateInMs" + chosenDateInMs);

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // set onClick listener on the TextView with the date, to show the date picker dialog
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
    }

    public void btnAddEditIncome(View view){
        extraTransaction.setTrWallet(walletId);
        extraTransaction.setTrCat(categoryId);

        try {
            extraTransaction.setTrAmount(Double.parseDouble(amountText.getText().toString()));
        }catch (NumberFormatException e){
            Toast.makeText(getApplicationContext(), "Please enter amount.",Toast.LENGTH_SHORT).show();
            return;
        }

        extraTransaction.setTrDesc(descriptionText.getText().toString());

        if(checkTransaction(extraTransaction)) {
            if (isNewTransaction){
                addIncome();
            } else {
                editIncome();
            }
        }
    }

    public void btnDeleteIncome(View view){
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setMessage("Delete this transaction?");
        aBuilder.setCancelable(true);

        aBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        aBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteIncome();

                Toast.makeText(context, "Successfully deleted!",Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();
    }

    private void addIncome(){
        // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference referenceWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + extraTransaction.getTrWallet() + "/"
                + Constants.INCOME_TRANSACTION_TYPE + "/"
                + extraTransaction.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(extraTransaction.getTrDate()) + "/"
                + extraTransaction.getTrDate() + "/");

        // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference referenceTotalBalance = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + "0" + "/"
                + Constants.INCOME_TRANSACTION_TYPE + "/"
                + extraTransaction.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(extraTransaction.getTrDate()) + "/"
                + extraTransaction.getTrDate() + "/");

//---    add the transaction in WALLET
        referenceWallet.setValue(extraTransaction);

//---    add the transaction in TOTAL BALANCE wallet
        referenceTotalBalance.setValue(extraTransaction);

//---   ReCalculate transactions WALLET income and balance
        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        Utilities.addIncomeToWallet(extraTransaction.getTrWallet(), extraTransaction, context);

//---   ReCalculate TOTAL BALANCE's income and balance
        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        Utilities.addIncomeToWallet(0L, extraTransaction, context);

        finish();
    }

    private void editIncome(){
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setMessage("Save changes?");
        aBuilder.setCancelable(true);

        aBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        aBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
        // TODO: needs better revision ->
                // initial data is stored in -> transToDelete
                transToDelete.getTrAmount();
                // changed data is stored in -> extraTransaction
                extraTransaction.getTrAmount();

                // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
                DatabaseReference referenceWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                        + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                        + transToDelete.getTrWallet() + "/"
                        + Constants.INCOME_TRANSACTION_TYPE + "/"
                        + transToDelete.getTrCat() + "/"
                        + Utilities.calculateBeggingOfMonth(transToDelete.getTrDate()) + "/"
                        + transToDelete.getTrDate() + "/");

                // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
                DatabaseReference referenceTotalBalance = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                        + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                        + "0" + "/"
                        + Constants.INCOME_TRANSACTION_TYPE + "/"
                        + transToDelete.getTrCat() + "/"
                        + Utilities.calculateBeggingOfMonth(transToDelete.getTrDate()) + "/"
                        + transToDelete.getTrDate() + "/");

//---   NULL the transaction in WALLET
                referenceWallet.setValue(null);

//---   NULL the transaction in TOTAL BALANCE wallet
                referenceTotalBalance.setValue(null);

                // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
                DatabaseReference referenceWalletNew = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                        + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                        + extraTransaction.getTrWallet() + "/"
                        + Constants.INCOME_TRANSACTION_TYPE + "/"
                        + extraTransaction.getTrCat() + "/"
                        + Utilities.calculateBeggingOfMonth(extraTransaction.getTrDate()) + "/"
                        + extraTransaction.getTrDate() + "/");

                // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
                DatabaseReference referenceTotalBalanceNew = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                        + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                        + "0" + "/"
                        + Constants.INCOME_TRANSACTION_TYPE + "/"
                        + extraTransaction.getTrCat() + "/"
                        + Utilities.calculateBeggingOfMonth(extraTransaction.getTrDate()) + "/"
                        + extraTransaction.getTrDate() + "/");

//---    add the transaction in WALLET
                referenceWalletNew.setValue(extraTransaction);

//---    add the transaction in TOTAL BALANCE wallet
                referenceTotalBalanceNew.setValue(extraTransaction);

                transToDelete.setTrAmount(-1 * transToDelete.getTrAmount());
                Utilities.editIncomeWallet(transToDelete.getTrWallet(), transToDelete, extraTransaction, context);

                Utilities.editIncomeWallet(0L, transToDelete, extraTransaction, context);

                Toast.makeText(context, "Successfully changed!",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();

    }

    private void deleteIncome(){
        // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference referenceWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + transToDelete.getTrWallet() + "/"
                + Constants.INCOME_TRANSACTION_TYPE + "/"
                + transToDelete.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(transToDelete.getTrDate()) + "/"
                + transToDelete.getTrDate() + "/");

        // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference referenceTotalBalance = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + "0" + "/"
                + Constants.INCOME_TRANSACTION_TYPE + "/"
                + transToDelete.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(transToDelete.getTrDate()) + "/"
                + transToDelete.getTrDate() + "/");

//---   NULL the transaction in WALLET
        referenceWallet.setValue(null);

//---   NULL the transaction in TOTAL BALANCE wallet
        referenceTotalBalance.setValue(null);

//---    now ReCalculate transactions WALLET income and balance
        //To do so -> add to the income AMOUNT = TR_AMOUNT * -1
        transToDelete.setTrAmount(-1 * transToDelete.getTrAmount());
        Utilities.addIncomeToWallet(transToDelete.getTrWallet(), transToDelete, context);

//---   now ReCalculate TOTAL BALANCE's income and balance
        Utilities.addIncomeToWallet(0L, transToDelete, context);

    }

    private boolean checkTransaction(Transaction transaction){
        if (transaction.getTrDate() == 0) return false;

        if (transaction.getTrAmount() == 0.00){
//            //inputlayout.setError("0.00 is not a valid amount!");
//            amountText.setError("0.00 is not a valid amount!");
            Toast.makeText(getApplicationContext(), "0.00 is not a valid amount!",Toast.LENGTH_SHORT).show();
            return false;
        }

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
