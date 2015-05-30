package com.kirilov.ivan.myfinance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ivan on 26-Apr-15.
 */
public class FinanceDbHelper extends SQLiteOpenHelper {

    // make sure you have only one instance of DBHelper
    public static FinanceDbHelper sInstance;

    public static final int DATABASE_VERSION = 1;               // database version - increment if change anything
    public static final String DATABASE_NAME = "finance.db";    // database name

    public static final String CREATE_TABLE_TRANSACTIONS =
            "CREATE TABLE " + FinanceContract.TransactionEntry.TABLE_NAME + "("
            + FinanceContract.TransactionEntry.TR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FinanceContract.TransactionEntry.TR_DATE + " INTEGER,"
            + FinanceContract.TransactionEntry.TR_ACCOUNT + " INTEGER,"
            + FinanceContract.TransactionEntry.TR_CATEGORY + " INTEGER,"
            + FinanceContract.TransactionEntry.TR_AMOUNT + " REAL,"
            + FinanceContract.TransactionEntry.TR_CURRENCY + " TEXT,"
            + FinanceContract.TransactionEntry.TR_DESCR + " TEXT);";

    public static final String CREATE_TABLE_ACCOUNTS =
            "CREATE TABLE " + FinanceContract.AccountsEntry.TABLE_NAME + "("
            + FinanceContract.AccountsEntry.ACC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FinanceContract.AccountsEntry.ACC_NAME + " TEXT);";

    public static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + FinanceContract.CategoriesEntry.TABLE_NAME + "("
            + FinanceContract.CategoriesEntry.CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + FinanceContract.CategoriesEntry.CAT_TYPE + " INTEGER,"
            + FinanceContract.CategoriesEntry.CAT_NAME + " TEXT);";

    // public method to ensure only single instance of FinanceDbHelper
    public static synchronized FinanceDbHelper getInstance(Context context){
        if (sInstance==null){
            sInstance = new FinanceDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // private to prevent direct instantiation
    private FinanceDbHelper(Context context){
        super( context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.d("FINANCE DB: ","DB created/opened");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(CREATE_TABLE_TRANSACTIONS);
                Log.d("FINANCE DB: ","Transactions table created !");
            db.execSQL(CREATE_TABLE_ACCOUNTS);
                Log.d("FINANCE DB: ","Accounts table created !");
            db.execSQL(CREATE_TABLE_CATEGORIES);
                Log.d("FINANCE DB: ","Categories table created !");
        }
        catch (Exception e){
            Log.d("FINANCE DB: ","FAILED TO CREATE TABLES!");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + FinanceContract.TransactionEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FinanceContract.AccountsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FinanceContract.CategoriesEntry.TABLE_NAME);

        // create new tables
        onCreate(db);
    }

    // --- functions to convert DATE&TIME in MS and reverse ---
    public String getTimeInString(long timeInMs, boolean monthOnly){
        SimpleDateFormat simpleDateFormat;
        String dateFormat = "dd-MMM-yyyy";
        String monthFormat = "MMM yyyy";

        if (monthOnly){
            simpleDateFormat = new SimpleDateFormat(monthFormat);
        }else {
            simpleDateFormat = new SimpleDateFormat(dateFormat);
        }


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMs);

        return simpleDateFormat.format(calendar.getTime());
    }

    public String getYearInString(long timeInMs){
        SimpleDateFormat simpleDateFormat;
        String yearFormat = "yyyy";

        simpleDateFormat = new SimpleDateFormat(yearFormat);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMs);

        return simpleDateFormat.format(calendar.getTime());
    }

    /* public long getTimeInMs(String timeInString){
        return 0;
    } */

    //close the DB
    public void closeDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()){
            db.close();
        }
    }

    //  --- CRUD(create, read, update, delete) Operations for each table ---

    //  --- CRUD for CATEGORIES ---
    // create new CATEGORY and return it's row ID in table (==ID)
    public long createCategory(Category category){
        // Opening DB for writing, using FinanceDbHelper object (this)
        SQLiteDatabase db = this.getWritableDatabase();

        // insert data for SQL query in ContentValue
        ContentValues contentValues = new ContentValues();

        //contentValues.put(FinanceContract.CategoriesEntry.CAT_ID, category.getCatId());
        contentValues.put(FinanceContract.CategoriesEntry.CAT_TYPE, category.getCatType());
        contentValues.put(FinanceContract.CategoriesEntry.CAT_NAME, category.getCatName());

        //insert row and return its position
        return db.insert(FinanceContract.CategoriesEntry.TABLE_NAME, null, contentValues);
    }

    // find a CATEGORY by ID and return it
    public Category getCategory(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + FinanceContract.CategoriesEntry.TABLE_NAME +
                                " WHERE " + FinanceContract.CategoriesEntry.CAT_ID + "=" + id;

            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor!=null) cursor.moveToFirst();

        Category category = new Category();
        category.setCatId(cursor.getLong(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_ID)));
        category.setCatType(cursor.getInt(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_TYPE)));
        category.setCatName(cursor.getString(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_NAME)));

        cursor.close();
        return category;
    }

    // read all CATEGORIES and return them in list array
    public List<Category> getAllCategories () {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Category> categories = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + FinanceContract.CategoriesEntry.TABLE_NAME;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);



        if (cursor.moveToFirst()){
            do {
                Category category = new Category();
                category.setCatId(cursor.getLong(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_ID)));
                category.setCatType(cursor.getInt(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_TYPE)));
                category.setCatName(cursor.getString(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_NAME)));
                //add it to the List
                categories.add(category);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    // read all CATEGORIES of selected TYPE and return them in list array
    public List<Category> getAllCategoriesByType( int type){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Category> categories = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + FinanceContract.CategoriesEntry.TABLE_NAME
                + " WHERE " + FinanceContract.CategoriesEntry.CAT_TYPE + "=" + type;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do {
                Category category = new Category();
                category.setCatId(cursor.getLong(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_ID)));
                category.setCatType(cursor.getInt(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_TYPE)));
                category.setCatName(cursor.getString(cursor.getColumnIndex(FinanceContract.CategoriesEntry.CAT_NAME)));

                categories.add(category);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    // getting CATEGORIES count
    public int getCategoryCount(){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + FinanceContract.CategoriesEntry.TABLE_NAME;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    // update CATEGORY by ID, returns number of rows which were updated
    public int updateCategory(Category category){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FinanceContract.CategoriesEntry.CAT_TYPE, category.getCatType());
        contentValues.put(FinanceContract.CategoriesEntry.CAT_NAME, category.getCatName());

        // update query : TABLE NAME; NEW VALUES; WHERE CLAUSE ( WHERE my_categories.cat_id = ?)
        // ? means - replace with args from args string; ARRAY OF STRING ARGUMENTS to be replaced with ?
        return db.update(FinanceContract.CategoriesEntry.TABLE_NAME,
                            contentValues,
                            FinanceContract.CategoriesEntry.CAT_ID + " = ?",
                            new String[]{String.valueOf(category.getCatId())});
    }

    // delete CATEGORY by ID
    public void deleteCategory(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FinanceContract.CategoriesEntry.TABLE_NAME,
                    FinanceContract.CategoriesEntry.CAT_ID + " = ?",
                    new String[]{String.valueOf(id)});
    }

    //  --- CRUD for ACCOUNTS ---
    // create new ACC and return its row ID in table (==ID)
    public long createAcc(Account account){
        SQLiteDatabase db=this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FinanceContract.AccountsEntry.ACC_NAME, account.getAccName());

        return db.insert(FinanceContract.AccountsEntry.TABLE_NAME, null, contentValues);
    }

    // find ACC by ID and return it
    public Account getAcc(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + FinanceContract.AccountsEntry.TABLE_NAME + " WHERE "
                + FinanceContract.AccountsEntry.ACC_ID + "=" + id;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery,null);
        if (cursor!=null) cursor.moveToFirst();

        Account account = new Account();
        account.setAccId(cursor.getLong(cursor.getColumnIndex(FinanceContract.AccountsEntry.ACC_ID)));
        account.setAccName(cursor.getString(cursor.getColumnIndex(FinanceContract.AccountsEntry.ACC_NAME)));

        cursor.close();
        return account;
    }

    // find all ACCs and return them in list array
    public List<Account> getAllAccs(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Account> accs = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + FinanceContract.AccountsEntry.TABLE_NAME;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do {
                Account account = new Account();
                account.setAccId(cursor.getLong(cursor.getColumnIndex(FinanceContract.AccountsEntry.ACC_ID)));
                account.setAccName(cursor.getString(cursor.getColumnIndex(FinanceContract.AccountsEntry.ACC_NAME)));
                accs.add(account);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return accs;
    }

    // getting ACCs count
    public int getAccCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + FinanceContract.AccountsEntry.TABLE_NAME;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery,null);
        int count = cursor.getCount();

        cursor.close();
        return count;
    }

    // update ACC by ID, returns number of updated rows
    public int updateAcc(Account account){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FinanceContract.AccountsEntry.ACC_ID, account.getAccId());
        contentValues.put(FinanceContract.AccountsEntry.ACC_NAME, account.getAccName());

        return db.update(FinanceContract.AccountsEntry.TABLE_NAME,
                contentValues,
                FinanceContract.AccountsEntry.ACC_ID + " = ?",
                new String[]{String.valueOf(account.getAccId())});
    }

    // delete ACC by ID
    public void deleteAcc(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FinanceContract.AccountsEntry.TABLE_NAME,
                FinanceContract.AccountsEntry.ACC_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    //  --- CRUD for TRANSACTIONS ---
    // create new TRANS and return row ID in table
    public long createTransaction(Transaction transaction){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        //contentValues.put(FinanceContract.TransactionEntry.TR_ID,transaction.getTrId());
        contentValues.put(FinanceContract.TransactionEntry.TR_DATE,transaction.getAtDate());
        contentValues.put(FinanceContract.TransactionEntry.TR_ACCOUNT,transaction.getAccUsed());
        contentValues.put(FinanceContract.TransactionEntry.TR_CATEGORY,transaction.getCatUsed());
        contentValues.put(FinanceContract.TransactionEntry.TR_AMOUNT,transaction.getAmountSpent());
        contentValues.put(FinanceContract.TransactionEntry.TR_CURRENCY,transaction.getCurrUsed());
        contentValues.put(FinanceContract.TransactionEntry.TR_DESCR, transaction.getDescAdded());

        return db.insert(FinanceContract.TransactionEntry.TABLE_NAME, null, contentValues);
    }

    // find TRANS by ID and return Transaction object
    public Transaction getTransaction(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME +
                " WHERE " + FinanceContract.TransactionEntry.TR_ID + "=" +id;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor!=null) cursor.moveToFirst();

        Transaction transaction = new Transaction();
        transaction.setTrId(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ID)));
        transaction.setAtDate(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DATE)));
        transaction.setAccUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ACCOUNT)));
        transaction.setCatUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CATEGORY)));
        transaction.setAmountSpent(cursor.getDouble(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_AMOUNT)));
        transaction.setCurrUsed(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CURRENCY)));
        transaction.setDescAdded(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DESCR)));

        cursor.close();
        return transaction;
    }

    // find all TRANSs in time interval
    public List<Transaction> getTransByTime(long startTime, long finalTime){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Transaction> transactions = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME + " ttra WHERE ttra."
                +FinanceContract.TransactionEntry.TR_DATE + " BETWEEN " + startTime + " AND " + finalTime;
        Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do {
                Transaction transaction = new Transaction();
                transaction.setTrId(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ID)));
                transaction.setAtDate(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DATE)));
                transaction.setAccUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ACCOUNT)));
                transaction.setCatUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CATEGORY)));
                transaction.setAmountSpent(cursor.getDouble(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_AMOUNT)));
                transaction.setCurrUsed(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CURRENCY)));
                transaction.setDescAdded(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DESCR)));
                transactions.add(transaction);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }

    // find all TRANSs in category and time interval
    public List<Transaction> getTransByCategoryAndTime(long catID, long startTime, long finalTime){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Transaction> transactions = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME + " ttra WHERE ttra."
                +FinanceContract.TransactionEntry.TR_CATEGORY + "=" + catID + " AND ttra."
                +FinanceContract.TransactionEntry.TR_DATE + " BETWEEN " + startTime + " AND " + finalTime;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do {
                Transaction transaction = new Transaction();
                transaction.setTrId(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ID)));
                transaction.setAtDate(cursor.getLong(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DATE)));
                transaction.setAccUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_ACCOUNT)));
                transaction.setCatUsed(cursor.getInt(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CATEGORY)));
                transaction.setAmountSpent(cursor.getDouble(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_AMOUNT)));
                transaction.setCurrUsed(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_CURRENCY)));
                transaction.setDescAdded(cursor.getString(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_DESCR)));
                transactions.add(transaction);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }

    // sum all TRANSs in category and time interval
    public float sumTransByCategoryAndTime(long catID, long startTime, long finalTime){
        SQLiteDatabase db = this.getReadableDatabase();
        float sumOfTrans = 0;

        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME + " ttra WHERE ttra."
                +FinanceContract.TransactionEntry.TR_CATEGORY + "=" + catID + " AND ttra."
                +FinanceContract.TransactionEntry.TR_DATE + " BETWEEN " + startTime + " AND " + finalTime;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                sumOfTrans = (float) (sumOfTrans + cursor.getDouble(cursor.getColumnIndex(FinanceContract.TransactionEntry.TR_AMOUNT)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sumOfTrans;
    }

    // getting TRANSs count by month
    public  int getTransCountByMonth(long startTime, long finalTime){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME + " ttra WHERE ttra."
                +FinanceContract.TransactionEntry.TR_DATE + " BETWEEN " + startTime + " AND " + finalTime;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    // getting TRANSs count
    public  int getTransCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + FinanceContract.TransactionEntry.TABLE_NAME;
            Log.d("FINANCE DB: ", selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    // update TRANS by ID and return number of updated rows
    public int updateTrans(Transaction transaction){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FinanceContract.TransactionEntry.TR_ID, transaction.getTrId());
        contentValues.put(FinanceContract.TransactionEntry.TR_DATE, transaction.getAtDate());
        contentValues.put(FinanceContract.TransactionEntry.TR_ACCOUNT, transaction.getAccUsed());
        contentValues.put(FinanceContract.TransactionEntry.TR_CATEGORY, transaction.getCatUsed());
        contentValues.put(FinanceContract.TransactionEntry.TR_AMOUNT, transaction.getAmountSpent());
        contentValues.put(FinanceContract.TransactionEntry.TR_CURRENCY, transaction.getCurrUsed());
        contentValues.put(FinanceContract.TransactionEntry.TR_DESCR, transaction.getDescAdded());

        return db.update(FinanceContract.TransactionEntry.TABLE_NAME,
                contentValues,
                FinanceContract.TransactionEntry.TR_ID + " = ?",
                new String[]{String.valueOf(transaction.getTrId())});
    }

    // delete TRANS by ID
    public int deleteTrans(long id){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete(FinanceContract.TransactionEntry.TABLE_NAME,
                FinanceContract.TransactionEntry.TR_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
}

