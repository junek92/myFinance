package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kirilov.ivan.myfinance.firebase_model.Wallet;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 01-Jun-16.
 */
public class FirebaseCreateWalletActivity extends BaseActivity {

    private DatabaseReference refUserInfoUserWallets;
    Query queryLimitToLast;
    ChildEventListener forSingleValue;

    private RecyclerView recyclerView;
    private GridImageAdapter gridImageAdapter;
    private GridLayoutManager gridLayoutManager;

    private Toolbar toolbar;
    private Context context;
    private long walletId;

    private GridView gridview;
    private ImageAdapter imageAdapter;

    private TextInputEditText textNewName;
    private Button btnAdd;

    private boolean isNewWallet;


    private Wallet extraWallet;
    private long lastWalletKey;

    public static long walletIconId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity_create_wallet);
        context = this;

        Intent intent = getIntent();
        if (!intent.hasExtra(Constants.EXTRA_WALLET)){
            Log.d(Constants.LOG_APP_ERROR, "\tFirebaseCreateWalletActivity -> NO INTENT EXTRA -> WALLET");
            finish();
        }
        extraWallet = intent.getExtras().getParcelable(Constants.EXTRA_WALLET);

        if (extraWallet.getWalletName().equals("")){
            isNewWallet = true;
        } else {
            isNewWallet = false;
        }

        toolbar = (Toolbar) findViewById(R.id.firebase_create_edit_wallet_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textNewName = (TextInputEditText) findViewById(R.id.firebase_create_edit_wallet_name);
        textNewName.setText(extraWallet.getWalletName());

        btnAdd = (Button) findViewById(R.id.firebase_create_edit_wallet_button);
        if (!extraWallet.getWalletName().equals("")){
            btnAdd.setText("SAVE");
        }

        walletIconId = extraWallet.getWalletIcon();

        recyclerView = (RecyclerView) findViewById(R.id.firebase_create_wallet_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(context, 3, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        // default icon is with ID = 1, but its on position ID-1 (because the icon for Total Balance is missing)
        gridImageAdapter = new GridImageAdapter(context, recyclerView, (int) walletIconId - 1);
        recyclerView.setAdapter(gridImageAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (forSingleValue != null){
            queryLimitToLast.removeEventListener(forSingleValue);
            Log.d(Constants.LOG_FIREBASE_DATABASE_OPERATIONS, " CREATE WALLET: forSingleWallet -> REMOVED!");
        }
    }

    public void onBtnAddSaveClick(View view){
        if (textNewName.getText().toString().isEmpty()){
            Toast.makeText(context, "The wallet needs a name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (walletIconId == -1){
            Toast.makeText(context, "Select an icon.", Toast.LENGTH_SHORT).show();
            return;
        }

        extraWallet.setWalletName(textNewName.getText().toString());
        extraWallet.setWalletIcon(walletIconId);

        refUserInfoUserWallets = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_INFORMATION + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + Constants.FIREBASE_LOCATION_USER_INFORMATION_USER_WALLETS + "/");

        progressDialogPleaseWait.show();

        if (isNewWallet){
            createWallet();
        } else {
            updateWallet();
        }
    }

    private void updateWallet(){
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Long.toString(extraWallet.getWalletId()) + "/walletName/" , extraWallet.getWalletName());
        childUpdates.put(Long.toString(extraWallet.getWalletId()) + "/walletIcon/" , extraWallet.getWalletIcon());

        refUserInfoUserWallets.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialogPleaseWait.dismiss();
                }
            }
        });

        final DatabaseReference reference = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_WALLETS_HISTORY + "/"
                + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                + extraWallet.getWalletId() + "/");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    Wallet dummyWallet = new Wallet();
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        dummyWallet = child.getValue(Wallet.class);
                        dummyWallet.setWalletName(extraWallet.getWalletName());
                        dummyWallet.setWalletIcon(extraWallet.getWalletIcon());
                        reference.child(child.getKey()).setValue(dummyWallet);
                    }

                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createWallet(){
        //TODO: It might be better to check again the MAX WALLET ID, before adding the wallet - to prevent mistakes if someone adds a wallet simultaneously
        refUserInfoUserWallets.child(Long.toString(extraWallet.getWalletId())).setValue(extraWallet).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialogPleaseWait.dismiss();
                    finish();
                }
            }
        });

    }
}
