package com.example.vbook.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vbook.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    //fire ath
    private FirebaseAuth firebaseAuth;
    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog =new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        // ddeen man hinih tao tk
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        // begin login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String email="", password="";
    private void validateData() {
        // sau khi login thif xu lu dl
        //lay dl
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString().trim();

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        }
        else {
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Login In...");
        progressDialog.show();
        // login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                      //login thanh cong  , check user or admin
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                       //login that bai
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking user...");
        // kiem tra user hay admin
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        // kiem tra db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //get user type
                        String userType = ""+snapshot.child("userType").getValue();
                        //check usertpe
                        if (userType.equals("user")){
                            // laf user bth
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        } else if (userType.equals("admin")) {
                            // la quyen admin
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

}
