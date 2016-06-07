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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * Created by Ivan on 26-May-16.
 */
public class LoginActivity extends BaseActivity {
    private TextInputEditText userEmail, userPass;
    private Button btnLogin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        userEmail = (TextInputEditText) findViewById(R.id.login_email);
        userPass = (TextInputEditText) findViewById(R.id.login_pass);

        userPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    onBtnLogInPressed(v);
                    handled = true;
                }

                return handled;
            }
        });

        btnLogin = (Button) findViewById(R.id.login_button);

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (!intentAlreadySent) {
                        // User IS signed in
                        Log.d("FIREBASE", "LOGIN: onAuthStateChanged:signed_in:" + user.getUid());
                        //PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true).apply();

                            Log.d("PREFERENCES", "Pref Wallet DEF - LOGIN: " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true));

                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Constants.KEY_PREF_EMAIL_PARSED, user.getEmail().replace('.',',').toLowerCase()).apply();

                        Intent intent = new Intent(LoginActivity.this, FirebaseMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);   //Clear all activities and create new task
                        startActivity(intent);

                        intentAlreadySent = true;

                        Log.d("FIREBASE", "Go to MAIN ACTIVITY");
                        finish();
                    }
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
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }

    public void onBtnLogInPressed(View view){
        String email = userEmail.getText().toString();
        String password = userPass.getText().toString();

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

        progressDialogPleaseWait.show();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialogPleaseWait.dismiss();
                // if signing in is successful
                Log.d("FIREBASE", "signInWithEmail:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Authentication failed. " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void onBtnSignUpPressed(View view){
        Intent intent = new Intent(LoginActivity.this, CreateAccActivity.class);
        startActivity(intent);
    }

    public void onBtnForgotPressed(View view){

    }
}
