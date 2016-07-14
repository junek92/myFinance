package com.kirilov.ivan.myfinance.myExtras;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.firebase_model.Transaction;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Ivan on 31-May-16.
 */
public class Utilities {

    public static String getTimeInString(long timeInMs, boolean monthOnly){

        SimpleDateFormat simpleDateFormat;
        String dateFormat = "dd-MMM-yyyy";
        String monthFormat = "MMM yyyy";

        if (monthOnly){
            simpleDateFormat = new SimpleDateFormat(monthFormat);
        }else {
            simpleDateFormat = new SimpleDateFormat(dateFormat);
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(timeInMs);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static long calculateBeggingOfCurrentMonth(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        // set the time to be - 0h : 00m : 00s . 250ms
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.set(Calendar.MILLISECOND, 250);
        // set the date to be - 1st of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        Log.d("CALENDAR ", " Current month -> " + calendar.get(Calendar.MONTH));

        return calendar.getTimeInMillis();
    }

    public static long calculateBeggingOfMonth(long timeInMs){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(timeInMs);

        // set the time to be - 0h : 00m : 00s . 250ms
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.set(Calendar.MILLISECOND, 250);
        // set the date to be - 1st of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

            Log.d("CALENDAR", "STRING: " + calendar.toString() + "\n MS:" + calendar.getTimeInMillis());

        return calendar.getTimeInMillis();
    }

    public static long calculateBeggingOfNextMonth(long timeInMs){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(timeInMs);

        // set the time to be - 0h : 00m : 00s . 250ms
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.set(Calendar.MILLISECOND, 250);
        // set the date to be - 1st of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // check if its last month of the year -> 11
        // if true next is 0, not 12
        // => increase the year
        if (calendar.get(Calendar.MONTH) == 11){
            calendar.set(Calendar.MONTH, 1);
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        }

        Log.d("CALENDAR ", " CURRENT month -> " + calendar.get(Calendar.MONTH));
        Log.d("CALENDAR ", " NEXT month -> " + (calendar.get(Calendar.MONTH) + 1));

        return calendar.getTimeInMillis();
    }

    public static long calculateBeggingOfPrevMonth(long timeInMs){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(timeInMs);

        // set the time to be - 0h : 00m : 00s . 250ms
        calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.set(Calendar.MILLISECOND, 250);
        // set the date to be - 1st of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // check if its first month of the year -> 0
        // if true prev is 11, not -1
        // => decrease year with 1
        if (calendar.get(Calendar.MONTH) == 0){
            calendar.set(Calendar.MONTH, 11);
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        }

        Log.d("CALENDAR ", " CURRENT month -> " + calendar.get(Calendar.MONTH));
        Log.d("CALENDAR ", " NEXT month -> " + (calendar.get(Calendar.MONTH) + 1));

        return calendar.getTimeInMillis();
    }

    public static String getDayMonthString(long timeInMs){
        SimpleDateFormat simpleDateFormat;
        String yearFormat = "dd-MMM";

        simpleDateFormat = new SimpleDateFormat(yearFormat);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(timeInMs);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getYearInString(long timeInMs){
        SimpleDateFormat simpleDateFormat;
        String yearFormat = "yyyy";

        simpleDateFormat = new SimpleDateFormat(yearFormat);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(timeInMs);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getFormattedAmount(Double amount, boolean includeCurrency, Context context){
        DecimalFormat mFormat = new DecimalFormat("###,###,##0.00");
        if (!includeCurrency){
            return mFormat.format(amount);
        } else {
            return mFormat.format(amount) + " " + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_CURRENCY, "USD");
        }
    }

//  --- INCOME METHODS  ---

    public static void addIncomeToWallet(final long walletId, final Transaction transactionToAdd, final Context context){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

            Log.d("BUGHUNT: ", "addIncomeToWallet: 134 -> transactionToAdd WALLET: " + walletId + " AMOUNT: " + transactionToAdd.getTrAmount());

        // get reference to -> userInfo/EMAIL/userWallets/WALLET_ID/
        final DatabaseReference refUserWalletsWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
                + walletId + "/");

        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        final DatabaseReference refWalletsHistoryWalletIdBeginOfMonth = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + walletId + "/"
                + Utilities.calculateBeggingOfMonth(transactionToAdd.getTrDate()) + "/");

        //--- If transaction MONTH == CURRENT MONTH
        if (Utilities.calculateBeggingOfMonth(transactionToAdd.getTrDate()) == Utilities.calculateBeggingOfCurrentMonth()){
            // THEN => transaction should be added in CURRENT MONTH node
            refUserWalletsWallet.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        // read the old values
                        Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 157 -> dummyWallet WALLET BEGIN: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                        dummyWallet.setWalletIncome(dummyWallet.getWalletIncome() + transactionToAdd.getTrAmount());
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 163 -> dummyWallet WALLET ADD INCOME: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                        dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + transactionToAdd.getTrAmount());
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 169 -> dummyWallet WALLET ADD BALANCE: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        if (dummyWallet.getWalletLastTrans() < transactionToAdd.getTrDate()){
                            // set new LAST TRANSACTION date
                            dummyWallet.setWalletLastTrans(transactionToAdd.getTrDate());
                        }

                        Log.d("BUGHUNT: ", "addIncomeToWallet: 178 -> dummyWallet WALLET AFTER: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        refUserWalletsWallet.setValue(dummyWallet);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
        //--- ELSE => transaction should be added in HISTORY node
            refWalletsHistoryWalletIdBeginOfMonth.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        // Update the node with the new data
                        Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                        // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                        dummyWallet.setWalletIncome(dummyWallet.getWalletIncome() + transactionToAdd.getTrAmount());
                        // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                        dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + transactionToAdd.getTrAmount());

                        if (dummyWallet.getWalletLastTrans() < transactionToAdd.getTrDate()) {
                            // set new LAST TRANSACTION date
                            dummyWallet.setWalletLastTrans(transactionToAdd.getTrDate());
                        }

                        if (dummyWallet.getWalletIncome() <= 0d && dummyWallet.getWalletExpenses() >= 0){
                            // if there are no transactions for this WALLET in HISTORY NODE, at this TIMESTAMP
                            // => remove the time stamp - it's not needed
                            refWalletsHistoryWalletIdBeginOfMonth.setValue(null);
                        } else {
                            // else update the TIMESTAMP
                            refWalletsHistoryWalletIdBeginOfMonth.setValue(dummyWallet);
                        }

                        updateCarryOverOfWallet(walletId, transactionToAdd, context);
                    } else {
                        // Create new node with available data
                        addWalletWithIncomeToHistory(context, walletId, transactionToAdd);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void editIncomeWallet(final long walletId, final Transaction oldTrans, final Transaction newTrans, final Context context){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        Log.d("BUGHUNT: ", "addIncomeToWallet: 134 -> transactionToAdd WALLET: " + walletId + " AMOUNT: " + oldTrans.getTrAmount());

        // get reference to -> userInfo/EMAIL/userWallets/WALLET_ID/
        final DatabaseReference refUserWalletsWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
                + walletId + "/");

        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        final DatabaseReference refWalletsHistoryWalletIdBeginOfMonth = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + walletId + "/"
                + Utilities.calculateBeggingOfMonth(oldTrans.getTrDate()) + "/");

        //--- If oldTrans MONTH == CURRENT MONTH
        if (Utilities.calculateBeggingOfMonth(oldTrans.getTrDate()) == Utilities.calculateBeggingOfCurrentMonth()){
            // THEN => oldTrans should be added in CURRENT MONTH node
            refUserWalletsWallet.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        // read the old values
                        Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 157 -> dummyWallet WALLET BEGIN: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                        dummyWallet.setWalletIncome(dummyWallet.getWalletIncome() + oldTrans.getTrAmount());
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 163 -> dummyWallet WALLET ADD INCOME: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                        dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + oldTrans.getTrAmount());
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 169 -> dummyWallet WALLET ADD BALANCE: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        if (dummyWallet.getWalletLastTrans() < oldTrans.getTrDate()){
                            // set new LAST TRANSACTION date
                            dummyWallet.setWalletLastTrans(oldTrans.getTrDate());
                        }

                        Log.d("BUGHUNT: ", "addIncomeToWallet: 178 -> dummyWallet WALLET AFTER: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        refUserWalletsWallet.setValue(dummyWallet).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // OLD TRANSACTION IS DELETED
                                if (walletId == 0L){
                                    addIncomeToWallet(0L, newTrans, context);
                                } else {
                                    addIncomeToWallet(newTrans.getTrWallet(), newTrans, context);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //--- ELSE => oldTrans should be added in HISTORY node
            refWalletsHistoryWalletIdBeginOfMonth.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        // Update the node with the new data
                        Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                        // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                        dummyWallet.setWalletIncome(dummyWallet.getWalletIncome() + oldTrans.getTrAmount());
                        // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                        dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + oldTrans.getTrAmount());

                        if (dummyWallet.getWalletLastTrans() < oldTrans.getTrDate()) {
                            // set new LAST TRANSACTION date
                            dummyWallet.setWalletLastTrans(oldTrans.getTrDate());
                        }

                        if (dummyWallet.getWalletIncome() <= 0d && dummyWallet.getWalletExpenses() >= 0){
                            // if there are no transactions for this WALLET in HISTORY NODE, at this TIMESTAMP
                            // => remove the time stamp - it's not needed
                            refWalletsHistoryWalletIdBeginOfMonth.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // OLD TRANSACTION IS DELETED
                                    if (walletId == 0L){
                                        addIncomeToWallet(0L, newTrans, context);
                                    } else {
                                        addIncomeToWallet(newTrans.getTrWallet(), newTrans, context);
                                    }
                                }
                            });
                        } else {
                            // else update the TIMESTAMP
                            refWalletsHistoryWalletIdBeginOfMonth.setValue(dummyWallet).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // OLD TRANSACTION IS DELETED
                                    if (walletId == 0L){
                                        addIncomeToWallet(0L, newTrans, context);
                                    } else {
                                        addIncomeToWallet(newTrans.getTrWallet(), newTrans, context);
                                    }
                                }
                            });
                        }

                        updateCarryOverOfWallet(walletId, oldTrans, context);
                    } else {
                        // Create new node with available data
                        addWalletWithIncomeToHistory(context, walletId, oldTrans);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void addWalletWithIncomeToHistory(final Context context, final long walletId, final Transaction transaction){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final Wallet walletToAdd = new Wallet();

        // get reference to -> userInfo/EMAIL/userWallets/WALLET_ID/
        final DatabaseReference refUserWalletsWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
                + walletId + "/");

        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/
        final DatabaseReference refWalletsHistoryWalletId = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + walletId + "/");
//                + Utilities.calculateBeggingOfMonth(transaction.getTrDate()) + "/");
        final Query query = refWalletsHistoryWalletId.orderByKey().endAt(Long.toString(Utilities.calculateBeggingOfPrevMonth(transaction.getTrDate()))).limitToLast(1);

        // get all data for wallet INFO
        refUserWalletsWallet.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    walletToAdd.setWalletId(dataSnapshot.getValue(Wallet.class).getWalletId());
                    walletToAdd.setWalletName(dataSnapshot.getValue(Wallet.class).getWalletName());
                    walletToAdd.setWalletIcon(dataSnapshot.getValue(Wallet.class).getWalletIcon());
                    walletToAdd.setWalletCurrency(dataSnapshot.getValue(Wallet.class).getWalletCurrency());
                    walletToAdd.setWalletCreated(dataSnapshot.getValue(Wallet.class).getWalletCreated());
                    walletToAdd.setWalletLastTrans(transaction.getTrDate());
                    walletToAdd.setWalletIncome(transaction.getTrAmount());
                    walletToAdd.setWalletBalance(transaction.getTrAmount());
                    walletToAdd.setWalletExpenses(0d);
                    //to calculate carry over - first must check for previous months in HISTORY node
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()){
                                for (DataSnapshot child : dataSnapshot.getChildren()){
                                    walletToAdd.setWalletCarryOver(child.getValue(Wallet.class).getWalletCarryOver() + child.getValue(Wallet.class).getWalletBalance());
                                    refWalletsHistoryWalletId.child(String.valueOf(Utilities.calculateBeggingOfMonth(transaction.getTrDate()))).setValue(walletToAdd);
                                    updateCarryOverOfWallet(walletId, transaction, context);
                                }

                            } else {
                                walletToAdd.setWalletCarryOver(0d);
                                refWalletsHistoryWalletId.child(String.valueOf(Utilities.calculateBeggingOfMonth(transaction.getTrDate()))).setValue(walletToAdd);
                                updateCarryOverOfWallet(walletId, transaction, context);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });

    }



//  --- EXPENSE METHODS  ---
    public static void addExpenseToWallet(final long walletId, final Transaction transactionToAdd, final Context context){
    final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    Log.d("BUGHUNT: ", "addExpenseToWallet: 134 -> transactionToAdd WALLET: " + walletId + " AMOUNT: " + transactionToAdd.getTrAmount());

    // get reference to -> userInfo/EMAIL/userWallets/WALLET_ID/
    final DatabaseReference refUserWalletsWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
            + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
            + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
            + walletId + "/");

    // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
    final DatabaseReference refWalletsHistoryWalletIdBeginOfMonth = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
            + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
            + walletId + "/"
            + Utilities.calculateBeggingOfMonth(transactionToAdd.getTrDate()) + "/");

    //--- If transaction MONTH == CURRENT MONTH
    if (Utilities.calculateBeggingOfMonth(transactionToAdd.getTrDate()) == Utilities.calculateBeggingOfCurrentMonth()){
        // THEN => transaction should be added in CURRENT MONTH node
        refUserWalletsWallet.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    // read the old values
                    Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                    Log.d("BUGHUNT: ", "addExpenseToWallet: 157 -> dummyWallet WALLET BEGIN: " + dummyWallet.getWalletId()
                            + " INCOME: " + dummyWallet.getWalletIncome()
                            + " BALANCE: " + dummyWallet.getWalletBalance());

                    // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                    dummyWallet.setWalletExpenses(dummyWallet.getWalletExpenses() + transactionToAdd.getTrAmount());
                    Log.d("BUGHUNT: ", "addExpenseToWallet: 163 -> dummyWallet WALLET ADD INCOME: " + dummyWallet.getWalletId()
                            + " INCOME: " + dummyWallet.getWalletIncome()
                            + " BALANCE: " + dummyWallet.getWalletBalance());

                    // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                    dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + transactionToAdd.getTrAmount());
                    Log.d("BUGHUNT: ", "addExpenseToWallet: 169 -> dummyWallet WALLET ADD BALANCE: " + dummyWallet.getWalletId()
                            + " INCOME: " + dummyWallet.getWalletIncome()
                            + " BALANCE: " + dummyWallet.getWalletBalance());

                    if (dummyWallet.getWalletLastTrans() < transactionToAdd.getTrDate()){
                        // set new LAST TRANSACTION date
                        dummyWallet.setWalletLastTrans(transactionToAdd.getTrDate());
                    }

                    Log.d("BUGHUNT: ", "addExpenseToWallet: 178 -> dummyWallet WALLET AFTER: " + dummyWallet.getWalletId()
                            + " INCOME: " + dummyWallet.getWalletIncome()
                            + " BALANCE: " + dummyWallet.getWalletBalance());

                    refUserWalletsWallet.setValue(dummyWallet);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });
    } else {
        //--- ELSE => transaction should be added in HISTORY node
        refWalletsHistoryWalletIdBeginOfMonth.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    // Update the node with the new data
                    Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                    // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                    dummyWallet.setWalletExpenses(dummyWallet.getWalletExpenses() + transactionToAdd.getTrAmount());
                    // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                    dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + transactionToAdd.getTrAmount());

                    if (dummyWallet.getWalletLastTrans() < transactionToAdd.getTrDate()) {
                        // set new LAST TRANSACTION date
                        dummyWallet.setWalletLastTrans(transactionToAdd.getTrDate());
                    }

                    if (dummyWallet.getWalletIncome() <= 0d && dummyWallet.getWalletExpenses() >= 0){
                        // if there are no transactions for this WALLET in HISTORY NODE, at this TIMESTAMP
                        // => remove the time stamp - it's not needed
                        refWalletsHistoryWalletIdBeginOfMonth.setValue(null);
                    } else {
                        // else update the TIMESTAMP
                        refWalletsHistoryWalletIdBeginOfMonth.setValue(dummyWallet);
                    }

                    updateCarryOverOfWallet(walletId, transactionToAdd, context);
                } else {
                    // Create new node with available data
                    addWalletWithExpenseToHistory(context, walletId, transactionToAdd);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

    public static void editExpenseWallet(final long walletId, final Transaction oldTrans, final Transaction newTrans, final Context context){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        Log.d("BUGHUNT: ", "addIncomeToWallet: 134 -> transactionToAdd WALLET: " + walletId + " AMOUNT: " + oldTrans.getTrAmount());

        // get reference to -> userInfo/EMAIL/userWallets/WALLET_ID/
        final DatabaseReference refUserWalletsWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
                + walletId + "/");

        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/BEGIN_OF_MONTH
        final DatabaseReference refWalletsHistoryWalletIdBeginOfMonth = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + walletId + "/"
                + Utilities.calculateBeggingOfMonth(oldTrans.getTrDate()) + "/");

        //--- If oldTrans MONTH == CURRENT MONTH
        if (Utilities.calculateBeggingOfMonth(oldTrans.getTrDate()) == Utilities.calculateBeggingOfCurrentMonth()){
            // THEN => oldTrans should be added in CURRENT MONTH node
            refUserWalletsWallet.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        // read the old values
                        Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 157 -> dummyWallet WALLET BEGIN: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                        dummyWallet.setWalletExpenses(dummyWallet.getWalletExpenses() + oldTrans.getTrAmount());
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 163 -> dummyWallet WALLET ADD INCOME: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                        dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + oldTrans.getTrAmount());
                        Log.d("BUGHUNT: ", "addIncomeToWallet: 169 -> dummyWallet WALLET ADD BALANCE: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        if (dummyWallet.getWalletLastTrans() < oldTrans.getTrDate()){
                            // set new LAST TRANSACTION date
                            dummyWallet.setWalletLastTrans(oldTrans.getTrDate());
                        }

                        Log.d("BUGHUNT: ", "addIncomeToWallet: 178 -> dummyWallet WALLET AFTER: " + dummyWallet.getWalletId()
                                + " INCOME: " + dummyWallet.getWalletIncome()
                                + " BALANCE: " + dummyWallet.getWalletBalance());

                        refUserWalletsWallet.setValue(dummyWallet).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // OLD TRANSACTION IS DELETED
                                if (walletId == 0L){
                                    addExpenseToWallet(0L, newTrans, context);
                                } else {
                                    addExpenseToWallet(newTrans.getTrWallet(), newTrans, context);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //--- ELSE => oldTrans should be added in HISTORY node
            refWalletsHistoryWalletIdBeginOfMonth.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        // Update the node with the new data
                        Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                        // recalculate NEW INCOME = OLD INCOME + TRANSACTION AMOUNT
                        dummyWallet.setWalletExpenses(dummyWallet.getWalletExpenses() + oldTrans.getTrAmount());
                        // recalculate NEW BALANCE = OLD BALANCE + TRANSACTION AMOUNT
                        dummyWallet.setWalletBalance(dummyWallet.getWalletBalance() + oldTrans.getTrAmount());

                        if (dummyWallet.getWalletLastTrans() < oldTrans.getTrDate()) {
                            // set new LAST TRANSACTION date
                            dummyWallet.setWalletLastTrans(oldTrans.getTrDate());
                        }

                        if (dummyWallet.getWalletIncome() <= 0d && dummyWallet.getWalletExpenses() >= 0){
                            // if there are no transactions for this WALLET in HISTORY NODE, at this TIMESTAMP
                            // => remove the time stamp - it's not needed
                            refWalletsHistoryWalletIdBeginOfMonth.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // OLD TRANSACTION IS DELETED
                                    if (walletId == 0L){
                                        addExpenseToWallet(0L, newTrans, context);
                                    } else {
                                        addExpenseToWallet(newTrans.getTrWallet(), newTrans, context);
                                    }
                                }
                            });
                        } else {
                            // else update the TIMESTAMP
                            refWalletsHistoryWalletIdBeginOfMonth.setValue(dummyWallet).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // OLD TRANSACTION IS DELETED
                                    if (walletId == 0L){
                                        addExpenseToWallet(0L, newTrans, context);
                                    } else {
                                        addExpenseToWallet(newTrans.getTrWallet(), newTrans, context);
                                    }
                                }
                            });
                        }

                        updateCarryOverOfWallet(walletId, oldTrans, context);
                    } else {
                        // Create new node with available data
                        addWalletWithExpenseToHistory(context, walletId, oldTrans);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void addWalletWithExpenseToHistory(final Context context, final long walletId, final Transaction transaction){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final Wallet walletToAdd = new Wallet();

        // get reference to -> userInfo/EMAIL/userWallets/WALLET_ID/
        final DatabaseReference refUserWalletsWallet = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
                + walletId + "/");

        // get reference to -> userWalletsHistory/EMAIL/WALLET_ID/
        final DatabaseReference refWalletsHistoryWalletId = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + walletId + "/");
//                + Utilities.calculateBeggingOfMonth(transaction.getTrDate()) + "/");
        final Query query = refWalletsHistoryWalletId.orderByKey().endAt(Long.toString(Utilities.calculateBeggingOfPrevMonth(transaction.getTrDate()))).limitToLast(1);

        // get all data for wallet INFO
        refUserWalletsWallet.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    walletToAdd.setWalletId(dataSnapshot.getValue(Wallet.class).getWalletId());
                    walletToAdd.setWalletName(dataSnapshot.getValue(Wallet.class).getWalletName());
                    walletToAdd.setWalletIcon(dataSnapshot.getValue(Wallet.class).getWalletIcon());
                    walletToAdd.setWalletCurrency(dataSnapshot.getValue(Wallet.class).getWalletCurrency());
                    walletToAdd.setWalletCreated(dataSnapshot.getValue(Wallet.class).getWalletCreated());
                    walletToAdd.setWalletLastTrans(transaction.getTrDate());
                    walletToAdd.setWalletIncome(0d);
                    walletToAdd.setWalletBalance(transaction.getTrAmount());
                    walletToAdd.setWalletExpenses(transaction.getTrAmount());
                    //to calculate carry over - first must check for previous months in HISTORY node
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()){
                                for (DataSnapshot child : dataSnapshot.getChildren()){
                                    walletToAdd.setWalletCarryOver(child.getValue(Wallet.class).getWalletCarryOver() + child.getValue(Wallet.class).getWalletBalance());
                                    refWalletsHistoryWalletId.child(String.valueOf(Utilities.calculateBeggingOfMonth(transaction.getTrDate()))).setValue(walletToAdd);
                                    updateCarryOverOfWallet(walletId, transaction, context);
                                }

                            } else {
                                walletToAdd.setWalletCarryOver(0d);
                                refWalletsHistoryWalletId.child(String.valueOf(Utilities.calculateBeggingOfMonth(transaction.getTrDate()))).setValue(walletToAdd);
                                updateCarryOverOfWallet(walletId, transaction, context);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });

    }



    // TRANSACTION INDEPENDENT - will work for INCOME and EXPENSE
    public static void updateCarryOverOfWallet(final long walletId, final Transaction transaction, final Context context){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        // walletsHistory/EMAIL/WALLET_ID
        final DatabaseReference referenceWalletsHistory = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + walletId + "/");

        Query query = referenceWalletsHistory.orderByKey().startAt(Long.toString(Utilities.calculateBeggingOfNextMonth(transaction.getTrDate())));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    Wallet dummyWallet;
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        // get old values
                        dummyWallet = child.getValue(Wallet.class);
                        // update the CARRY OVER
                        dummyWallet.setWalletCarryOver(dummyWallet.getWalletCarryOver() + transaction.getTrAmount());
                        // save new values
                        referenceWalletsHistory.child(child.getKey()).setValue(dummyWallet);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });

        // userInfo/EMAIL/userWallets/WALLET_ID
        final DatabaseReference referenceUserWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/"
                + walletId + "/");

        referenceUserWallets.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    Wallet dummyWallet = dataSnapshot.getValue(Wallet.class);
                    dummyWallet.setWalletCarryOver(dummyWallet.getWalletCarryOver() + transaction.getTrAmount());
                    referenceUserWallets.setValue(dummyWallet);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
