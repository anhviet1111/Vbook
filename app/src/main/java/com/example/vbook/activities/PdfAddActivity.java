package com.example.vbook.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.vbook.databinding.ActivityPdfAddBinding;
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

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {
     private ActivityPdfAddBinding binding;
     private FirebaseAuth firebaseAuth;

     // dialog
    private ProgressDialog progressDialog;

     // arraylist hold pdf
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

     private Uri pdfUri= null;

     private static final int PDF_PICK_CODE =1000;

     // Tag dor debug
    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // firebas auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //set up dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // nut bam toi trang trc do
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        // attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });
        // pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });
//click, up load pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//validate datta
              validateData();
            }
        });

    }
    private String title="" , description = "";

    private void validateData() {
        // step 1: validate data
        Log.d(TAG,"validateData: validating data..");

        //getdata
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();


        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Nhập Tên Sách..", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Nhập Mô Tả ... ", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Chọn Thể Loại.. ", Toast.LENGTH_SHORT).show();
        }
        else if(pdfUri==null){
            Toast.makeText(this, "Chọn Pdf", Toast.LENGTH_SHORT).show();
        }
        else {
            // all data is valid, can upload now
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
       // step 2: upload
        Log.d(TAG,"uploadPdfToStorage: uploading to storage..");

        // show progress
        progressDialog.setMessage("UpLoading Pdf..");
        progressDialog.show();
        //timestamp
        long timestamp = System.currentTimeMillis();

        // duong dan trong fire base
        String filePathAndName = "Books/" + timestamp;
        //reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"onSuccess: PDF Uploaded to Storage..");
                        Log.d(TAG,"onSuccess: Getting pdf url");
                        // get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl ="" +uriTask.getResult();

                        // upload firebase
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                     //
                     Log.d(TAG,"onFailure: PDF Upload Fail due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF Upload Fail due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        // upload info to firebase
        Log.d(TAG,"uploadPdfToStorage: uploading PDf info to firebase..");

        progressDialog.setMessage("Uploading Pdf Info..");
        String uid = firebaseAuth.getUid();

        // set data up, add view count dow
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id","" +timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewCount", 0);
        hashMap.put("downloadsCount", 0);



        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onSuccess : Successfully uploaded..");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploaded..", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"onFailure: Fail to upload database due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Fail to upload database due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Loading pdf categories..");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        // databse>categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();// cleae sau adding
                categoryTitleArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                   String categoryId = ""+ds.child("id").getValue();
                   String categoryTitle = ""+ds.child("category").getValue();

                   // them arraylist
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // chon id va tiltle
    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG,"categoryPickDialog: showing category pick dialog");
        // lay array cua categoris tuwf arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chon The Loai")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //iteam click
                        // lay iteam tuw list
                        selectedCategoryTitle= categoryTitleArrayList.get(which);
                        selectedCategoryId= categoryIdArrayList.get(which);
                        // set vao the loai text view
                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG,"onclick: Selected Category"+selectedCategoryId+""+selectedCategoryTitle);

                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG,"pdfPickIntent: Starting intent");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
           if(requestCode == PDF_PICK_CODE){
               Log.d(TAG,"onActivityResult: PDF Picked");
               pdfUri= data.getData();
               Log.d(TAG,"onActivityResult: URI: "+pdfUri);
           }
        }
        else {
            Log.d(TAG,"onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();

        }
    }
}// add fire base