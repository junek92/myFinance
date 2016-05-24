package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ivan on 19-May-16.
 */
public class FirebaseActivityTest extends AppCompatActivity {


    private FirebaseDatabase database;                      // connects to my remote real time DB
    private DatabaseReference pullDbRef;                    // reference to specific node
    private FirebaseAuth mAuth;                             // used for authentication with Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;   // listener to determine if the user is logged in

    private String userEmail, childResult;



    private String pushKey, pushValue,
                    pullKey;

    ValueEventListener myValueEventListener;    // used to catch changes in key->value
    ChildEventListener myChildEventListener;

    Context context;
    Boolean attached, loggedin;


    TextView textViewIsLogged, textViewResult;
    EditText editTextEmail, editTextPassword,
            editTextKeyPush, editTextValuePush,
            editTextKeyPull, editTextValuePull;

    Button btnPush, btnAttach, btnLogInOut, btnCreateAcc;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_test);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.firebase_toolbar);
        setSupportActionBar(toolbar);

        attached = false;
        loggedin = false;
        //--- GET SOME OLD STUFF DONE
        btnPush = (Button) findViewById(R.id.fb_btn_push);
        btnAttach = (Button) findViewById(R.id.fb_btn_pull);
        btnLogInOut = (Button) findViewById(R.id.fb_btn_login_out);
        btnCreateAcc = (Button) findViewById(R.id.fb_btn_create_acc);

        textViewIsLogged = (TextView) findViewById(R.id.fb_text_logged);
        textViewResult = (TextView) findViewById(R.id.fb_text_view);


        editTextEmail = (EditText) findViewById(R.id.fb_email);
        editTextPassword = (EditText) findViewById(R.id.fb_password);
        editTextKeyPush = (EditText) findViewById(R.id.fb_key_push);
        editTextValuePush = (EditText) findViewById(R.id.fb_value_push);
        editTextKeyPull = (EditText) findViewById(R.id.fb_key_pull);

        //--- FIREBASE STUFF    ---
        // get instance of Firebase database and Firebase authentication
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    btnCreateAcc.setEnabled(false);

                    Log.d("FIREBASE", "onAuthStateChanged:signed_in:" + user.getUid());
                    loggedin = true;
                    userEmail = user.getEmail();
                    userEmail = userEmail.replace('.',',');

                    textViewIsLogged.setText(user.getEmail());

                    btnLogInOut.setBackgroundColor(Color.RED);
                    btnLogInOut.setText("LOGOUT");

                } else {
                    // User is signed out
                    btnCreateAcc.setEnabled(true);

                    Log.d("FIREBASE", "onAuthStateChanged:signed_out");
                    loggedin = false;

                    textViewIsLogged.setText("not logged in");

                    btnLogInOut.setBackgroundColor(Color.GREEN);
                    btnLogInOut.setText("LOGIN");
                }
            }
        };

        myValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //GenericTypeIndicator<List<SamplePOJO>> gti = new GenericTypeIndicator<List<SamplePOJO>>() {};
                //List<SamplePOJO> sampleData = dataSnapshot.getValue(gti);
                String result = "";
                result = dataSnapshot.toString() + "\n\n";

                // Hash Map to store all <KEY VALUE> pairs
                HashMap<String, SamplePOJO> mAllTransactionHashMap = new HashMap<>();

                // enhanced FOR loop - for (int current : int [] ) { whats_the_integer = current }
                for (DataSnapshot transaction : dataSnapshot.getChildren()) {
                    mAllTransactionHashMap.put(transaction.getKey(), transaction.getValue(SamplePOJO.class));
                }


                int order = 0;
                // google how to read HashMap with enhanced for :/
                for (Map.Entry<String, SamplePOJO> transaction : mAllTransactionHashMap.entrySet()){
                    result += "---\t" + order + "\t---------------\n KEY: " + transaction.getKey() + "\n " + transaction.getValue().toString() + "\n";
                    order++;
                }
                textViewResult.setText(result);
                //textViewResult.setText(sampleData.get(0).toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                editTextValuePull.setText("onCancelled");

            }
        };

        childResult = "";
        myChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                childResult += "\n--------------\n KEY: " + dataSnapshot.getKey() + "\n" + dataSnapshot.getValue(SamplePOJO.class).toString() + "\n";
                textViewResult.setText(childResult);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                childResult += "\n--------------\n KEY: " + dataSnapshot.getKey() + "\n" + dataSnapshot.getValue(SamplePOJO.class).toString() + "\n";
                textViewResult.setText(childResult);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };



    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void logInOut(View view){
        if (loggedin){
            mAuth.signOut();

        } else {
            mAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("FIREBASE", "signInWithEmail:onComplete:" + task.isSuccessful());


                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.w("FIREBASE", "signInWithEmail", task.getException());
                        Toast.makeText(FirebaseActivityTest.this, "Authentication failed." + task.getException().toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void createAcc(View view){
        mAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("FIREBASE", "createUserWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Toast.makeText(FirebaseActivityTest.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void btnPush(View view){
        pushKey = userEmail + "/" + editTextKeyPush.getText().toString();
        pushValue = editTextValuePush.getText().toString();

        // vzima reference kym dadeniqt vyzel posochen s PUSHKEY i sled tova izprashata stoinostta v PUSHVALUE, chrez setValue
        DatabaseReference myRef = database.getReference(pushKey);
        SamplePOJO samplePOJO = new SamplePOJO(1, 123456789, 1d, 2d, 123.54d, "BGN", "ebasi pojoto");

        myRef.setValue(samplePOJO);
    }

    public void btnDeAttachListener(View view){
        pullKey = editTextKeyPull.getText().toString();

        if (attached){
            // remove listener
            attached = false;
            btnAttach.setBackgroundColor(Color.GREEN);
            btnAttach.setText("ATTACH");

            // premahva LISTENERA ot izbraniqt reference (vyzel)
            pullDbRef.removeEventListener(myValueEventListener);
            textViewResult.setText("");

        } else {
            // attach listener
            attached = true;
            btnAttach.setBackgroundColor(Color.RED);
            btnAttach.setText("REMOVE");

            // pravi reference kym izbraniqt vyzel i mu zakacha listener koito prochita pyrvonachalnata stoinost i sledi za bydeshti promeni, dokato ne se razkachi

            pullDbRef = database.getReference(userEmail);
            Query mQuery = pullDbRef.orderByKey();
            mQuery.addChildEventListener(myChildEventListener);
//            mQuery.addListenerForSingleValueEvent(myValueEventListener);
//            Query query = pullDbRef.orderByChild("trId");
//            query.addValueEventListener(myValueEventListener);
//            pullDbRef.addChildEventListener(myChildEventListener);
//            pullDbRef.addValueEventListener(myValueEventListener);
        }
    }
}
