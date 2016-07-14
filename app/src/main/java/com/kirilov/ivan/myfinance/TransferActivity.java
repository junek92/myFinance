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
import com.kirilov.ivan.myfinance.adapters.TransferGridImageNameAdapter;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Transfer;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 07-Jun-16.
 */
public class TransferActivity extends BaseActivity {


    private DatePickerDialog datePickerDialog;
    private TextInputEditText amountText, descriptionText;
    private TextView currencyText, dateText;

    private RecyclerView walletsFromRecycleView, walletsToRecycleView;
    private TransferGridImageNameAdapter walletsFromGridAdapter, walletsToGridAdapter;
    private GridLayoutManager walletsFromGridLayoutManager, walletsToGridLayoutManager;

    private long chosenDateInMs;

    private Transaction extraTransaction, incomeTransaction, expenseTransaction, incomeToDelete, expenseToDelete;

    public static long walletFromId;
    public static long walletToId;
    private boolean isNewTransaction;

    Toolbar toolbar;
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        context = this;
        isNewTransaction = true;

        Intent intent = getIntent();
        if (!intent.hasExtra(Constants.EXTRA_TRANSACTION)){
            Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            finish();
        }

        extraTransaction = intent.getParcelableExtra(Constants.EXTRA_TRANSACTION);

        toolbar = (Toolbar) findViewById(R.id.transfer_toolbar);
        setSupportActionBar(toolbar);

        amountText = (TextInputEditText) findViewById(R.id.transfer_amount);
        descriptionText = (TextInputEditText) findViewById(R.id.transfer_description);

        walletFromId = 1;
        walletToId = 1;

        if (extraTransaction.getTrAmount() != 0d){
            isNewTransaction = false;

            Button btnSave = (Button) findViewById(R.id.transfer_btn_add);
            btnSave.setVisibility(View.GONE);

            Button btnDelete = (Button) findViewById(R.id.transfer_btn_delete);
            btnDelete.setVisibility(View.VISIBLE);

            if (extraTransaction.getTrAmount() < 0){
                amountText.setText(Double.toString(-1 * extraTransaction.getTrAmount()));
            } else {
                amountText.setText(Double.toString(extraTransaction.getTrAmount()));
            }

            descriptionText.setText(extraTransaction.getTrDesc());

            if (extraTransaction.getTrType() == Constants.INCOME_TRANSACTION_TYPE){
                incomeToDelete = new Transaction(
                        extraTransaction.getTrId(),
                        extraTransaction.getTrDate(),
                        extraTransaction.getTrWallet(),
                        extraTransaction.getTrType(),
                        extraTransaction.getTrCat(),
                        extraTransaction.getTrAmount(),
                        extraTransaction.getTrCurrency(),
                        extraTransaction.getTrDesc()
                );

                walletToId = incomeToDelete.getTrWallet();

            } else if (extraTransaction.getTrType() == Constants.EXPENSE_TRANSACTION_TYPE){
                expenseToDelete = new Transaction(
                        extraTransaction.getTrId(),
                        extraTransaction.getTrDate(),
                        extraTransaction.getTrWallet(),
                        extraTransaction.getTrType(),
                        extraTransaction.getTrCat(),
                        extraTransaction.getTrAmount(),
                        extraTransaction.getTrCurrency(),
                        extraTransaction.getTrDesc()
                );

                walletFromId = expenseToDelete.getTrWallet();
            }

            getMissingTransaction();
        }

        chosenDateInMs = extraTransaction.getTrDate();
        // set default wallets to be 1
        // this way the app will prevent accidental taps on ADD button


        amountText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.myWhite), PorterDuff.Mode.SRC_ATOP);
        descriptionText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.myWhite), PorterDuff.Mode.SRC_ATOP);

        currencyText = (TextView) findViewById(R.id.transfer_currency);
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

        if (isNewTransaction){
            setupWalletsRecycleView();
        }

        setupDatePickerDialog();
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

    public void btnAddEditTransfer(View view){
//        extraTransaction.setTrWallet();

        if (walletFromId == walletToId) {
            Toast.makeText(getApplicationContext(), "Choose different wallets.",Toast.LENGTH_SHORT).show();
            return;
        }

        // transfer from/to is always with category id = 0
        extraTransaction.setTrCat(0L);

        try {
            extraTransaction.setTrAmount(Double.parseDouble(amountText.getText().toString()));
        }catch (NumberFormatException e){
            Toast.makeText(getApplicationContext(), "Please enter amount.",Toast.LENGTH_SHORT).show();
            return;
        }

        extraTransaction.setTrDesc(descriptionText.getText().toString());

        if(checkTransaction(extraTransaction)) {
            if (isNewTransaction){
                addTransfer();
//            } else {
//                editTransfer();
            }
        }


    }

    public void btnDeleteTransfer(View view){
        if (expenseToDelete != null && incomeToDelete != null){
            AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
            aBuilder.setMessage("Delete this transfer?");
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
                    deleteTransfer();

                    Toast.makeText(context, "Successfully deleted!",Toast.LENGTH_SHORT).show();

                    finish();
                }
            });

            AlertDialog alertDialog = aBuilder.create();
            alertDialog.show();
        }
    }

    public void addTransfer(){
        incomeTransaction = new Transaction();
        expenseTransaction = new Transaction();

        incomeTransaction.setTrId(extraTransaction.getTrId());
        incomeTransaction.setTrDate(extraTransaction.getTrDate());
        incomeTransaction.setTrAmount(extraTransaction.getTrAmount());
        incomeTransaction.setTrCurrency(extraTransaction.getTrCurrency());
        incomeTransaction.setTrDesc(extraTransaction.getTrDesc());
        incomeTransaction.setTrCat(0L);
        incomeTransaction.setTrType(Constants.INCOME_TRANSACTION_TYPE);
        incomeTransaction.setTrWallet(walletToId);

        expenseTransaction.setTrId(extraTransaction.getTrId());
        expenseTransaction.setTrDate(extraTransaction.getTrDate());
        expenseTransaction.setTrAmount(-1 * extraTransaction.getTrAmount());
        expenseTransaction.setTrCurrency(extraTransaction.getTrCurrency());
        expenseTransaction.setTrDesc(extraTransaction.getTrDesc());
        expenseTransaction.setTrCat(0L);
        expenseTransaction.setTrType(Constants.EXPENSE_TRANSACTION_TYPE);
        expenseTransaction.setTrWallet(walletFromId);

        // userTrans/EMAIL/WALLET_ID/0/0/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference refIncomeTransactionWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + incomeTransaction.getTrWallet() + "/"
                + Constants.INCOME_TRANSACTION_TYPE + "/"
                + incomeTransaction.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(incomeTransaction.getTrDate()) + "/"
                + incomeTransaction.getTrDate() + "/");

        // userTrans/EMAIL/WALLET_ID/1/0/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference refExpenseTransactionWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + expenseTransaction.getTrWallet() + "/"
                + Constants.EXPENSE_TRANSACTION_TYPE + "/"
                + expenseTransaction.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(expenseTransaction.getTrDate()) + "/"
                + expenseTransaction.getTrDate() + "/");

        DatabaseReference refTrasferNode = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_TRANSFERS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Utilities.calculateBeggingOfMonth(expenseTransaction.getTrDate()) + "/"
                + expenseTransaction.getTrDate() + "/");

        Transfer transfer = new Transfer(incomeTransaction.getTrDate(), walletFromId, walletToId);
        refTrasferNode.setValue(transfer);

        //---

        //---    add the transaction in WALLET TO
        refIncomeTransactionWallet.setValue(incomeTransaction);

        //---    add the transaction in WALLET FROM
        refExpenseTransactionWallet.setValue(expenseTransaction);

        //---   ReCalculate transactions WALLET TO -> income and balance
        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        Utilities.addIncomeToWallet(incomeTransaction.getTrWallet(), incomeTransaction, context);

        //---   ReCalculate transactions WALLET income and balance
        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        Utilities.addExpenseToWallet(expenseTransaction.getTrWallet(), expenseTransaction, context);

        finish();
    }

    public void editTransfer(){
        //TODO: implement transfer editing
    }

    public void deleteTransfer(){
        // userTrans/EMAIL/WALLET_ID/0/0/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference refIncomeTransactionWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + incomeToDelete.getTrWallet() + "/"
                + Constants.INCOME_TRANSACTION_TYPE + "/"
                + incomeToDelete.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(incomeToDelete.getTrDate()) + "/"
                + incomeToDelete.getTrDate() + "/");

        // userTrans/EMAIL/WALLET_ID/1/0/BEGIN_OF_MONTH/TRANSACTION_ID
        DatabaseReference refExpenseTransactionWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + expenseToDelete.getTrWallet() + "/"
                + Constants.EXPENSE_TRANSACTION_TYPE + "/"
                + expenseToDelete.getTrCat() + "/"
                + Utilities.calculateBeggingOfMonth(expenseToDelete.getTrDate()) + "/"
                + expenseToDelete.getTrDate() + "/");

        DatabaseReference refTrasferNode = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_TRANSFERS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Utilities.calculateBeggingOfMonth(expenseToDelete.getTrDate()) + "/"
                + expenseToDelete.getTrDate() + "/");

        //---
        refTrasferNode.setValue(null);
        //---    add the transaction in WALLET TO
        refIncomeTransactionWallet.setValue(null);

        //---    add the transaction in WALLET FROM
        refExpenseTransactionWallet.setValue(null);

        //---   ReCalculate transactions WALLET TO -> income and balance
        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        incomeToDelete.setTrAmount(-1 * incomeToDelete.getTrAmount());
        Utilities.addIncomeToWallet(incomeToDelete.getTrWallet(), incomeToDelete, context);

        //---   ReCalculate transactions WALLET income and balance
        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        expenseToDelete.setTrAmount(-1 * expenseToDelete.getTrAmount());
        Utilities.addExpenseToWallet(expenseToDelete.getTrWallet(), expenseToDelete, context);


//        // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
//        DatabaseReference referenceWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
//                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
//                + transToDelete.getTrWallet() + "/"
//                + Constants.INCOME_TRANSACTION_TYPE + "/"
//                + transToDelete.getTrCat() + "/"
//                + Utilities.calculateBeggingOfMonth(transToDelete.getTrDate()) + "/"
//                + transToDelete.getTrDate() + "/");
//
//        // userTrans/EMAIL/WALLET_ID/0/CAT_ID/BEGIN_OF_MONTH/TRANSACTION_ID
//        DatabaseReference referenceTotalBalance = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
//                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
//                + "0" + "/"
//                + Constants.INCOME_TRANSACTION_TYPE + "/"
//                + transToDelete.getTrCat() + "/"
//                + Utilities.calculateBeggingOfMonth(transToDelete.getTrDate()) + "/"
//                + transToDelete.getTrDate() + "/");
//
////---   NULL the transaction in WALLET
//        referenceWallet.setValue(null);
//
////---   NULL the transaction in TOTAL BALANCE wallet
//        referenceTotalBalance.setValue(null);
//
////---    now ReCalculate transactions WALLET income and balance
//        //To do so -> add to the income AMOUNT = TR_AMOUNT * -1
//        transToDelete.setTrAmount(-1 * transToDelete.getTrAmount());
//        Utilities.addIncomeToWallet(transToDelete.getTrWallet(), transToDelete, context);
//
////---   now ReCalculate TOTAL BALANCE's income and balance
//        Utilities.addIncomeToWallet(0L, transToDelete, context);



    }

    public void setupWalletsRecycleView(){
        walletsFromRecycleView = (RecyclerView) findViewById(R.id.transfer_from_wallet_recycle_view);
        walletsToRecycleView = (RecyclerView) findViewById(R.id.transfer_to_wallet_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        walletsFromRecycleView.setHasFixedSize(true);
        walletsToRecycleView.setHasFixedSize(true);

        walletsFromGridLayoutManager = new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false);
        walletsToGridLayoutManager = new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false);

        walletsFromRecycleView.setLayoutManager(walletsFromGridLayoutManager);
        walletsToRecycleView.setLayoutManager(walletsToGridLayoutManager);

        //TODO: First must find first wallet ID, it might not be 1 - always
        // default wallet IF not chosen by the user is 1, but its on position ID-1 (because Total Balance is missing)
        walletsFromGridAdapter = new TransferGridImageNameAdapter(context, walletsFromRecycleView, 0, (int) (walletFromId - 1));
        walletsToGridAdapter = new TransferGridImageNameAdapter(context, walletsToRecycleView, 1, (int) (walletToId - 1));

        walletsFromRecycleView.setAdapter(walletsFromGridAdapter);
        walletsToRecycleView.setAdapter(walletsToGridAdapter);

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
                            walletsFromGridAdapter.addWalletToList(child.getValue(Wallet.class));
                            walletsToGridAdapter.addWalletToList(child.getValue(Wallet.class));
                            walletsFromGridAdapter.notifyDataSetChanged();
                            walletsToGridAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                    TransferActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                TransferActivity.this.finish();
            }
        });
    }

    private void setupDatePickerDialog(){
        dateText = (TextView) findViewById(R.id.transfer_text_view_date);
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

                        if (newDate.get(Calendar.YEAR) < year || newDate.get(Calendar.MONTH) < monthOfYear){
                            Toast.makeText(context, "Can't add transactions in future!", Toast.LENGTH_SHORT).show();
                            return;
                        }

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

    private void getMissingTransaction(){
        final Transfer transfer = new Transfer();

        // -->  walletTransfer / EMAIL / BEGIN_OF_MONTH / TIMESTAMP
        DatabaseReference walletsTransfers = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_TRANSFERS + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Utilities.calculateBeggingOfMonth(extraTransaction.getTrDate()) + "/"
                + extraTransaction.getTrDate() + "/");

        final ValueEventListener getTransaction = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    if (expenseToDelete == null){
                        expenseToDelete = dataSnapshot.getValue(Transaction.class);
                        walletFromId = expenseToDelete.getTrWallet();
                        setupWalletsRecycleView();
                    } else if (incomeToDelete == null){
                        incomeToDelete = dataSnapshot.getValue(Transaction.class);
                        walletToId = incomeToDelete.getTrWallet();
                        setupWalletsRecycleView();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        walletsTransfers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    transfer.setTransferTime(dataSnapshot.getValue(Transfer.class).getTransferTime());
                    transfer.setFromWalletId(dataSnapshot.getValue(Transfer.class).getFromWalletId());
                    transfer.setToWalletId(dataSnapshot.getValue(Transfer.class).getToWalletId());

                    if (expenseToDelete == null){
                        DatabaseReference reference = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                                + transfer.getFromWalletId() + "/"
                                + Constants.EXPENSE_TRANSACTION_TYPE + "/"
                                + "0" + "/"
                                + Utilities.calculateBeggingOfMonth(transfer.getTransferTime()) + "/"
                                + transfer.getTransferTime() + "/");

                        reference.addListenerForSingleValueEvent(getTransaction);
                    } else if (incomeToDelete == null){
                         DatabaseReference reference = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_TRANSACTIONS + "/"
                                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                                + transfer.getToWalletId() + "/"
                                + Constants.INCOME_TRANSACTION_TYPE + "/"
                                + "0" + "/"
                                + Utilities.calculateBeggingOfMonth(transfer.getTransferTime()) + "/"
                                + transfer.getTransferTime() + "/");

                        reference.addListenerForSingleValueEvent(getTransaction);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




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
