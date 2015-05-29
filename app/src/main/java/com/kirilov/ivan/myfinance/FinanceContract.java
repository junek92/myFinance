package com.kirilov.ivan.myfinance;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 26-Apr-15.
 * Using contract class to store all the constants for the column, table names. It's easier to
 * manage new column and table names this way.
 */
public final class FinanceContract {
    // Empty constructor to prevent accidental usage
    public FinanceContract (){}

    // Inner class to describe the TRANSACTIONS table in DB
    public static abstract class TransactionEntry implements BaseColumns{
        public static final String TABLE_NAME = "my_transactions";
        public static final String TR_ID = "tr_id";
        public static final String TR_DATE = "tr_date";
        public static final String TR_ACCOUNT = "tr_account";
        public static final String TR_CATEGORY = "tr_category";
        public static final String TR_AMOUNT = "tr_amount";
        public static final String TR_CURRENCY = "tr_currency";
        public static final String TR_DESCR = "tr_description";
    }

    // Inner class to describe the ACCOUNTS table in DB
    public static abstract class AccountsEntry implements BaseColumns{
        public static final String TABLE_NAME = "my_accounts";
        public static final String ACC_ID = "acc_id";
        public static final String ACC_NAME = "acc_name";
    }

    // Inner class to describe the CATEGORIES table in DB
    public static abstract class CategoriesEntry implements BaseColumns{
        public static final String TABLE_NAME = "my_categories";
        public static final String CAT_ID = "cat_id";
        public static final String CAT_TYPE = "cat_type";
        public static final String CAT_NAME = "cat_name";
        public static final int CT_TYPE_DEBIT = 1;
        public static final int CT_TYPE_CREDIT = 2;
        public static final int CT_TYPE_TRANSFER = 3;
    }
}
