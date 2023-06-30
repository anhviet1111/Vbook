package com.example.vbook.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vbook.MyApplication;
import com.example.vbook.R;
import com.example.vbook.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;

    //fire auth, lay du lieu nguoi dung
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private Uri imageUri = null;

    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        // handle click, goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click pick image
        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageAttachMenu();
            }
        });

        //handle click update profile
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: lấy thông tin người dùng" + firebaseAuth.getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // lay tat ca thong tin user tu snapshot
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String timestamp = "" + snapshot.child("time stamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String userType = "" + snapshot.child("userType").getValue();
                        //set data
                        binding.nameEt.setText(name);


                        // st hinh anh bang glide
                        Glide.with(ProfileEditActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Nhập Tên..", Toast.LENGTH_SHORT).show();
        }
        else {
            if (imageUri == null){
                updateProfile("");
            }
            else {
                uploadImage();
            }
        }

    }
    private void uploadImage(){
        Log.d(TAG, "uploadImage: Đang cập nhật ảnh người dùng");
        progressDialog.setMessage("Đang cập nhật ảnh người dùng");
        progressDialog.show();
        String filePathAndName = "ProfileImages/"+firebaseAuth.getUid();
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Đã cập nhật");
                        Log.d(TAG, "onSuccess: Lấy đường dẫn ảnh");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl =""+uriTask.getResult();

                        Log.d(TAG, "onSuccess: Upload thành công URl"+uploadedImageUrl);
                        updateProfile(uploadedImageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Cập nhật ảnh thất bại vì "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Cập nhật ảnh thất bại vì"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfile(String imageUrl ){
        Log.d(TAG, "updateProfile: dang cap nhat ho so nguoi dung");
        progressDialog.setMessage("dang cap nhat ho so nguoi dung");
        progressDialog.show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name",""+name);
        if (imageUri != null){
            hashMap.put("profileImage",""+imageUrl);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: cap nhat thanh cong");
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "cap nhat thanh cong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: that bai khi cap nhat den Db vi"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "that bai khi cap nhat den Db vi"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImageAttachMenu() {
        //popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int which = item.getItemId();
                if (which == 0) {
                    //camera
                    pickImageCamera();
                } else if (which == 1) {
                    //gallery
                    pickImageGallery();
                }
                return false;
            }
        });

    }

    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Chọn từ camera " + imageUri);
                        Intent data = result.getData();
                        binding.profileIv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: chọn từ thư viện " + imageUri);
                        binding.profileIv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
}