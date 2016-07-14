package com.kirilov.ivan.myfinance;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kirilov.ivan.myfinance.myExtras.Constants;


/**
 * Created by Ivan on 26-May-16.
 */
public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private TextInputEditText userEmail, userPass;
    private Button btnLogin, btnGoogleLogin;

    private static final int RC_SIGN_IN = 9001;     // Request code assigned to the ActivityForResult
    private GoogleApiClient mGoogleApiClient;

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
        btnGoogleLogin = (Button) findViewById(R.id.login_google_button);

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (!intentAlreadySent) {
                        // User IS signed in
                            Log.d(Constants.LOG_PREFERENCES, "LOGIN: onAuthStateChanged:signed_in:" + user.getUid());
                        //PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true).apply();

                            Log.d(Constants.LOG_PREFERENCES, "Pref Wallet DEF - LOGIN: " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Constants.FIREBASE_RETURN_DEFAULT_STATE, true));

                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Constants.KEY_PREF_EMAIL_PARSED, user.getEmail().replace('.',',').toLowerCase()).apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);   //Clear all activities and create new task
                        startActivity(intent);

                        intentAlreadySent = true;

                        Log.d(Constants.LOG_PREFERENCES, "Go to MAIN ACTIVITY");
                        finish();
                    }
                } else {
                    // User IS NOT signed in
                }
            }
        };

        firebaseAuth.addAuthStateListener(firebaseAuthListener);



//---   GOOGLE SIGN IN
        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_app_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();




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
                Log.d(Constants.LOG_FIREBASE_LISTENERS, "signInWithEmail:onComplete:" + task.isSuccessful());
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
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setTitle("Reset password");
        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        final EditText emailEditText = (EditText) viewInflated.findViewById(R.id.dialog_forgot_email);
        emailEditText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.myWhite), PorterDuff.Mode.SRC_ATOP);

        aBuilder.setView(viewInflated);
        aBuilder.setCancelable(true);
        aBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        aBuilder.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String resetEmail = emailEditText.getText().toString();

                if (isEmailValid(resetEmail)){
                    Log.d(Constants.LOG_APP_ERROR, "EMAIL -> " + resetEmail);
                    firebaseAuth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Please check your email inbox.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Reset failed. " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(LoginActivity.this, "Enter a valid email address.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                Log.d(Constants.LOG_FIREBASE_LISTENERS, "STATUS: " + result.getStatus().toString());
                Log.d(Constants.LOG_FIREBASE_LISTENERS, "onActivityResult:isSuccess -> FALSE");
                // [END_EXCLUDE]
            }
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(Constants.LOG_FIREBASE_LISTENERS, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // [START_EXCLUDE silent]
        progressDialogPleaseWait.show();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(Constants.LOG_FIREBASE_LISTENERS, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(Constants.LOG_FIREBASE_LISTENERS, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        progressDialogPleaseWait.dismiss();
                        // [END_EXCLUDE]
                    }
                });
    }

    //TODO: linkWithCredential -> https://firebase.google.com/docs/auth/android/account-linking
}
