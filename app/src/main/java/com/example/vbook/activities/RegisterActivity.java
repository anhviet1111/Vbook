package com.example.vbook.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vbook.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;

    // tao progress
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // init firebase
        firebaseAuth= FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog =new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        // quay vef
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //bawt dau man hinh dang ki
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vadidateData();

            }
        });
    }
//sau khi tao tk, lay du lieu
    private String name="",email="",password="";
    private void vadidateData() {
        //get data
        name=binding.nameEt.getText().toString().trim();
        email= binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString().trim();
        String cPassword=binding.cPasswordEt.getText().toString().trim();

        //validate data
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Enter you name...", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this, "confirm password..", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cPassword)) {
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
        }
        else {
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //show progress
        progressDialog.setMessage("Creating account...");
        progressDialog.show();
        // create in firebase
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account creation sucess, add infirebase
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        //account creating failed
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info..");

        // moc thoi gian
        long timestamp= System.currentTimeMillis();
        // laay moc thoi gian, khi user dang ki
        String uid = firebaseAuth.getUid();
        // set data add database
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileImage",""); //bo trong,
        hashMap.put("userType","user");//user hoawc admin
        hashMap.put("time stamp", timestamp);

        // set data to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // du lieu dua vao database
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account Created...", Toast.LENGTH_SHORT).show();
                        //tao account thanh cong
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // du lieu khong dua duoc vao database
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}


