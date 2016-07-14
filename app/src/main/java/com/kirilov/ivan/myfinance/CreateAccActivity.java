package com.kirilov.ivan.myfinance;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.kirilov.ivan.myfinance.firebase_model.User;
import com.kirilov.ivan.myfinance.myExtras.Constants;
import com.kirilov.ivan.myfinance.myExtras.Utilities;

import java.util.Calendar;

/**
 * Created on 27-May-16.
 * By Ivan
 */

public class CreateAccActivity extends BaseActivity {
    private TextInputEditText userEmail, userPass, userPassConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acc);

        userEmail = (TextInputEditText) findViewById(R.id.create_acc_email);
        userPass = (TextInputEditText) findViewById(R.id.create_acc_pass);
        userPassConfirm = (TextInputEditText) findViewById(R.id.create_acc_pass_confirm);

        userPassConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    onBtnCreatePressed(v);
                    handled = true;
                }

                return handled;
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null && !intentAlreadySent) {
                    // User IS signed in
                      Log.d("FIREBASE", "CREATE: onAuthStateChanged:signed_in:" + user.getUid());

                    //parse used account - to lower case and replace ',' with '.' - put in shared preference
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit().putString(Constants.KEY_PREF_EMAIL_PARSED, user.getEmail().replace('.',',').toLowerCase()).apply();

                    DatabaseReference mReference = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                            + user.getEmail().replace('.',',').toLowerCase() + "/");
                    User mUser = new User(firebaseAuth.getCurrentUser().getEmail(), Calendar.getInstance().getTimeInMillis(), "", Utilities.calculateBeggingOfCurrentMonth());
                    mReference.setValue(mUser);

                    Intent intent = new Intent(CreateAccActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);   //Clear all activities and create new task
                    startActivity(intent);

                    intentAlreadySent = true;

                        Log.d("FIREBASE", "Go to MAIN ACTIVITY");
                    finish();
                } else {
                    // User IS NOT signed in
                }
            }
        };

        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

    public void onBtnCreatePressed(View view) {
        String email = userEmail.getText().toString().toLowerCase();
        String password = userPass.getText().toString();
        String passwordConfirm = userPassConfirm.getText().toString();

        /**
         * If email and password are not empty show progress dialog and try to authenticate
         */
        if (email.equals("")) {
            userEmail.setError(getString(R.string.error_enter_email));
            return;
        }

        if (!isEmailValid(email)){
            userEmail.setError(String.format(getString(R.string.error_invalid_email_not_valid), email));
            return;
        }

        if (password.equals("")) {
            userPass.setError(getString(R.string.error_enter_password));
            return;
        }

        if (password.length() < 6){
            userPass.setError(getString(R.string.error_invalid_password_too_short));
            return;
        }


        if (passwordConfirm.equals("")) {
            userPassConfirm.setError(getString(R.string.error_enter_password));
            return;
        }

        if (passwordConfirm.compareTo(password) != 0) {
            userPassConfirm.setError(getString(R.string.error_confirm_password_not_same));
            return;
        }

        progressDialogPleaseWait.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialogPleaseWait.dismiss();
                        Log.d("FIREBASE", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(CreateAccActivity.this, "Creating new user failed. " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });


    }
}
