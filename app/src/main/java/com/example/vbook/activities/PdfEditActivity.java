package com.example.vbook.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.vbook.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
    //view binding
    private ActivityPdfEditBinding binding;
    private String bookId;

    //dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArraylist, categoryIdArraylist;
    private static final String TAG="BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        //setup dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();
        //click, chon the loai
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });


        //nut bam den tran truowc do
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //click bat daua upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }



    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get book info
                        selectedCategoryId =""+snapshot.child("categoryId").getValue();
                        String description = ""+snapshot.child("desciption").getValue();
                        String title =""+snapshot.child("title").getValue();

                        // set to view
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChange: loading Book category info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category=""+snapshot.child("category").getValue();

                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private  String title="", description="";
    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        description= binding.descriptionEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Nhap Ten ..", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Nhap Mo Ta", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "Chon The Loai", Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to database");

        progressDialog.setMessage("Updating book info..");
        progressDialog.show();

        //set datata to up date
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);

        //satart updateing
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book Updated..");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Book info updated", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed update due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        
                    }
                });

    }


    private String selectedCategoryId="", selectedCategoryTitle="";
    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArraylist.size()];
        for (int i=0; i<categoryTitleArraylist.size(); i++){
            categoriesArray[i]= categoryTitleArraylist.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryId = categoryIdArraylist.get(which);
                        selectedCategoryTitle = categoryTitleArraylist.get(which);

                        // textview
                        binding.categoryTv.setText(selectedCategoryTitle);


                    }
                })
                .show();

    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories");

        categoryIdArraylist = new ArrayList<>();
        categoryTitleArraylist = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              categoryIdArraylist.clear();
              categoryTitleArraylist.clear();
              for (DataSnapshot ds: snapshot.getChildren()){
                  String id = ""+ds.child("id").getValue();
                  String category =""+ds.child("category").getValue();
                  categoryIdArraylist.add(id);
                  categoryTitleArraylist.add(category);

                  Log.d(TAG, "onDataChange: ID"+id);
                  Log.d(TAG, "onDataChange: category"+category);
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}