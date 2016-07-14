package com.kirilov.ivan.myfinance;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.kirilov.ivan.myfinance.myExtras.Constants;

/**
 * Created by Ivan on 26-May-16.
 */
public class BaseActivity  extends AppCompatActivity{
    protected FirebaseDatabase firebaseDatabase;
    protected FirebaseAuth firebaseAuth;
    protected FirebaseAuth.AuthStateListener firebaseAuthListener;

    protected ProgressDialog progressDialogPleaseWait;

    protected boolean intentAlreadySent = false;
    protected int screenDpi;

    static boolean isInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            if(!isInitialized){
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                isInitialized = true;
            }else {
                Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS,"Disk Persistence Already Initialized !!!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenDpi = dm.densityDpi;

         /* Setup the "Please wait" progress dialog for future use */
        progressDialogPleaseWait = new ProgressDialog(this);
        progressDialogPleaseWait.setTitle(null);
        progressDialogPleaseWait.setMessage(getString(R.string.progress_dialog_please_wait_msg));
        progressDialogPleaseWait.setCancelable(false);

        if (!((this instanceof LoginActivity) || (this instanceof CreateAccActivity))) {
            firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User IS signed in
                        Log.d(Constants.LOG_PREFERENCES, "BASE: onAuthStateChanged:signed_in:" + user.getUid());

                    } else {
                        // User IS NOT signed in
                        if (!intentAlreadySent){
                            //put flag to check - if there are no wallets -> add the default ones
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true).apply();
                                Log.d(Constants.LOG_PREFERENCES, "Pref Wallet DEF - BASE LOG OUT: " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true));
                            //delete stored acc in the shared preference
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Constants.KEY_PREF_EMAIL_PARSED, "").apply();

                            Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);   //Clear all activities and create new task
                            startActivity(intent);
                            intentAlreadySent = true;

                            Log.d(Constants.LOG_PREFERENCES, "Go to LOGIN ACTIVITY");
                            finish();
                        } else {
                            Log.d(Constants.LOG_PREFERENCES, "BASE: WAITING FOR INTENT");
                        }
                    }
                }
            };

            firebaseAuth.addAuthStateListener(firebaseAuthListener);
        }

    }
    //TODO: IF there are problems with AUTHENTICATION LISTENERS for FIREBASE, maybe should put them in ONSTART and ONPAUSE
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!((this instanceof LoginActivity) || (this instanceof CreateAccActivity))) {
            if (firebaseAuthListener != null){
                firebaseAuth.removeAuthStateListener(firebaseAuthListener);
                Log.d(Constants.LOG_FIREBASE_LISTENERS, "BASE_ACTIVITY: firebaseAuthListener -> REMOVED");
            }
        }
    }
    //TODO: Add password reset option and implementation

    public void onLogOutPressed(View view){
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setMessage("Log out from your account?");
        aBuilder.setCancelable(true);
        aBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        aBuilder.setPositiveButton("LOG OUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
            }
        });
        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();
    }

    public boolean isEmailValid(String email) {
        return (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
}
