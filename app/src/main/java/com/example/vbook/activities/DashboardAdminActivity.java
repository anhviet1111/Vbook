package com.example.vbook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vbook.adapters.AdapterCategory;
import com.example.vbook.databinding.ActivityDashboardAdminBinding;
import com.example.vbook.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {
    //view binding
    private ActivityDashboardAdminBinding binding;
    //
    private FirebaseAuth firebaseAuth;

    // arraylist
    private ArrayList<ModelCategory> categoryArrayList;
    // adapter
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // init firebase
        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();
        loadCategories();

        // edit text tim kiem---------------------

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterCategory.getFilter().filter(s);
                } catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // edit text tim kiem--------------------
        // handle log out------------------------
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
        // set nut bam them the loaii
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });
        // add pdf
        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, ProfileActivity.class));
            }
        });
    }

    private void loadCategories() {
        // arraylist
        categoryArrayList = new ArrayList<>();

        // lay tat ca category tu firebase > categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist
                categoryArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    // get data
                    ModelCategory model = ds.getValue(ModelCategory.class);

                    // add arraylist
                    categoryArrayList.add(model);
                }
                // set adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this, categoryArrayList);
                // set adapter recycle view
                binding.categoriesRv.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void checkUser() {
        // get user
        FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
        if (firebaseUser==null){
            // ko login
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else{
            // login in, lay info
            String email= firebaseUser.getEmail();
            //set in text view
            binding.subtitleTv.setText(email);
        }
    }
}
