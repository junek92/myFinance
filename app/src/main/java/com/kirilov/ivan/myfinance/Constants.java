package com.kirilov.ivan.myfinance;

/**
 * Created by Ivan on 26-May-16.
 */
public final class Constants {
    //---   SPECIFIC FIREBASE NODE LOCATIONS
    public static final String FIREBASE_LOCATION_USER_CATEGORIES = "userCat";
    public static final String FIREBASE_LOCATION_USER_CATEGORIES_EXPENSE = "expenseCat";
    public static final String FIREBASE_LOCATION_USER_CATEGORIES_INCOME = "incomeCat";
    public static final String FIREBASE_LOCATION_USER_INFORMATION = "userInfo";
    public static final String FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS = "userWallets";
    public static final String FIREBASE_LOCATION_USER_INFORMATION_USER_CURRENT_MONTH = "userCurrentMonth";
    public static final String FIREBASE_LOCATION_USER_TRANSACTIONS = "userTrans";
    public static final String FIREBASE_LOCATION_USER_WALLETS_HISTORY = "userWalletsHistory";
    public static final String FIREBASE_LOCATION_USER_WALLETS_TRANSFERS = "walletTransfer";

    //---   INTENTS EXTRAS
    public static final String EXTRA_WALLET = "extra_wallet";
    public static final String EXTRA_DATE = "chosenDate";
    public static final String EXTRA_MONTH_BEGIN = "month_begin";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_TRANSACTION = "extra_transaction";


    //---   PREFERENCES KEY VALUES
    public static final String KEY_PREF_FIRST_LAUNCH = "first_launch";
    public static final String KEY_PREF_CURRENCY = "used_currency";
    public static final String KEY_PREF_EMAIL_PARSED = "parsed_user_email";
    public static final String FIREBASE_RETURN_DEFAULT_STATE = "defaultWallets";

    //---   LOG.D TAGS
    public static final String LOG_FIREBASE_LISTENERS = "FIREBASE-LISTENERS: ";
    public static final String LOG_FIREBASE_DATABASE_OPERATIONS = "DATABASE-OPERATIONS: ";
    public static final String LOG_NAVIGATION = "NAVIGATION: ";
    public static final String LOG_PREFERENCES = "PREFERENCES: ";
    public static final String LOG_APP_ERROR = "APP ERROR: ";

    //---   OTHER
    public static final long INCOME_CATEGORY_TYPE = 0L;
    public static final long EXPENSE_CATEGORY_TYPE = 1L;

    public static final long INCOME_TRANSACTION_TYPE = 0L;
    public static final long EXPENSE_TRANSACTION_TYPE = 1L;

}
