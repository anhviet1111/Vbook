package com.example.vbook;

import static com.example.vbook.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.vbook.adapters.AdapterPdfAdmin;
import com.example.vbook.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        return date;

    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting..");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait..");
        progressDialog.setMessage("Deleting"+bookTitle+"...");// e.g deleting book
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from storage");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");
                        Log.d(TAG, "onSuccess: Now deleting from database");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from database");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted Successfully..", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from da due to"+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";
        // get file tu firebase

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes =storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle +" "+bytes);

                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if (mb >= 1){
                            sizeTv.setText(String.format("%.2f", mb)+"MB");
                        } else if (kb >=1 ) {
                           sizeTv.setText(String.format("%.2f", kb)+"KB");
                        }
                        else {
                            sizeTv.setText(String.format("%.2f", bytes)+"bytes");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }


    public static void  loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle+"successfully got the file");

                        // get pdf view
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                      progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                       progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");
                                        // neu page tv ko nul thi set page number
                                        if (pagesTv !=null){
                                            pagesTv.setText(""+nbPages);
                                        }
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: fail getting file from url due to"+e.getMessage());
                    }
                });
    }


    public static void loadCategory(String categoryId, TextView categoryTv) {
        // get category su dung categoryID
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        //set textview
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    public static void incrementBookViewCount(String bookId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get view count
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();

                        if (viewsCount.equals("") || viewsCount.equals("null")){
                            viewsCount="0";
                        }

                        //increment view count
                        long newViewsCount = Long.parseLong(viewsCount) +1;

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl){
        Log.d(TAG_DOWNLOAD, "downloadBook: downloading book..");

        String nameWithExtensions = bookTitle+ ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME"+nameWithExtensions);

        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("please wait");
        progressDialog.setMessage("downloading"+nameWithExtensions+"...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //download from firebase storange using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book downloaded");
                        saveDownloadedBook(context, progressDialog,bytes, nameWithExtensions, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "failed to download due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtensions, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: saving downloaded book");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();
            String filePath = downloadsFolder.getPath()+ "/" +nameWithExtensions;
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to Download Folder ");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);
        }
        catch (Exception e){
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download Folder due to"+e.getMessage());
            Toast.makeText(context, "Failed saving to Download Folder due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: increment book download count");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        // download count
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count"+downloadsCount);

                        if (downloadsCount.equals("") || downloadsCount.equals("null")){
                            downloadsCount="0";
                        }

                        long newDownloadsCount = Long.parseLong(downloadsCount) + 1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: New Download Count"+newDownloadsCount);

                        // set data to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        // upload count to database
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Downloads Count Updated..");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads Count Due to"+e.getMessage());
                                    }
                                });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }




    public static void addToFavorite(Context context, String bookId){
        //kiem tra user login
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Toast.makeText(context, "You are not log in", Toast.LENGTH_SHORT).show();
        }
        else {
            long timestamp = System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId",""+bookId);
            hashMap.put("timestamp",""+timestamp);
            //luu vao database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Đã thêm vào mục yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Có lỗi khi thêm vào mục yêu thích"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }
    public static void removeFromFavorite(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Toast.makeText(context, "You are not log in", Toast.LENGTH_SHORT).show();
        }
        else {

            //xoa khoi database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, " Bỏ yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Có lỗi khi xóa yêu thích"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }
    }

