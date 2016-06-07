package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.kirilov.ivan.myfinance.firebase_model.Category;

/**
 * Created by Ivan on 04-Jun-16.
 */
public class FirebaseCreateCategoryActivity extends BaseActivity {
    private DatabaseReference reference;

    private Category extraCategory;

    private AppBarLayout appBarLayout;
    private Button btnAdd;
    private TextInputEditText catNameEditText;

    private RecyclerView recyclerView;
    private GridImageAdapter gridImageAdapter;
    private GridLayoutManager gridLayoutManager;

    public static long categoryIconId;

    Context context;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_activity_create_category);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.firebase_create_category_toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (!intent.hasExtra(Constants.EXTRA_CATEGORY)){
                Log.d(Constants.LOG_APP_ERROR, "\tFirebaseCreateCategoryActivity -> NO INTENT EXTRA -> CATEGORY");
            finish();
        }
        extraCategory = intent.getExtras().getParcelable(Constants.EXTRA_CATEGORY);

        appBarLayout = (AppBarLayout) findViewById(R.id.firebase_create_category_app_bar_layout);

        catNameEditText = (TextInputEditText) findViewById(R.id.firebase_create_category_name);
        catNameEditText.setText(extraCategory.getCatName());

        btnAdd = (Button) findViewById(R.id.firebase_create_category_button);
        if (!extraCategory.getCatName().equals("")){
            btnAdd.setText("SAVE");
        }

        if (extraCategory.getCatType() == Constants.EXPENSE_CATEGORY_TYPE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.accentColorDark));
            }
            toolbar.setBackgroundColor(getResources().getColor(R.color.accentColor));
            appBarLayout.setBackgroundColor(getResources().getColor(R.color.accentColor));

            btnAdd.setBackgroundColor(getResources().getColor(R.color.accentColor));
        }

        categoryIconId = extraCategory.getCatIcon();

        recyclerView = (RecyclerView) findViewById(R.id.firebase_create_category_recycle_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(context, 3, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        // default icon is with ID = 1, but its on position ID-1 (because the icons for Transfer TO/FROM are missing)
        gridImageAdapter = new GridImageAdapter(context, recyclerView, (int) categoryIconId - 1, extraCategory.getCatType());
        recyclerView.setAdapter(gridImageAdapter);
    }

    public void onBtnCreateCategory(View view){
        if (catNameEditText.getText().toString().isEmpty()){
            Toast.makeText(context, "The category needs a name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ( categoryIconId <= 0 ||
                ((categoryIconId > getResources().obtainTypedArray(R.array.categoryExpenseIcon).length()) && extraCategory.getCatType() == Constants.EXPENSE_CATEGORY_TYPE) ||
                ((categoryIconId > getResources().obtainTypedArray(R.array.categoryIncomeIcon).length()) && extraCategory.getCatType() == Constants.INCOME_CATEGORY_TYPE))
        {
            Toast.makeText(context, "Choose an icon.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialogPleaseWait.show();

        extraCategory.setCatName(catNameEditText.getText().toString());
        extraCategory.setCatIcon(categoryIconId);

        if (extraCategory.getCatType() == Constants.INCOME_CATEGORY_TYPE){
            reference = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                    + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                    + Constants.FIREBASE_LOCATION_USER_CATEGORIES_INCOME + "/");
        } else {
            reference = firebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USER_CATEGORIES + "/"
                    + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.KEY_PREF_EMAIL_PARSED, null) + "/"
                    + Constants.FIREBASE_LOCATION_USER_CATEGORIES_EXPENSE + "/");
        }

        //TODO: It might be better to check again the MAX CATEGORY ID, before adding the category - to prevent mistakes if someone adds a category simultaneously
        reference.child(Long.toString(extraCategory.getCatId())).setValue(extraCategory).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressDialogPleaseWait.dismiss();
                    finish();
                } else {
                    Toast.makeText(context, "Error.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
