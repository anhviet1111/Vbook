package com.example.vbook.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vbook.activities.PdfListAdminActivity;
import com.example.vbook.filters.FilterCategory;
import com.example.vbook.models.ModelCategory;
import com.example.vbook.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {
    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    private RowCategoryBinding binding;

    private FilterCategory filter;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
//getdata
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        // set data
        holder.categoryTv.setText(category);
        // but bam, xoa category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xóa")
                        .setMessage("Sau Khi Xóa Sẽ Không Thể Hoàn Tác!")
                        .setPositiveButton("Xác Nhận", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Đang Xóa..", Toast.LENGTH_SHORT).show();
                                deleteCategory(model, holder);
                            }
                        })
                        .setNegativeButton("Hủy Bỏ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
        //dandle item, go pdf pdfactivityadmin
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);

            }
        });
    }

    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        // lay id de xoa
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // xoa thanh cong
                        Toast.makeText(context, "Đã Xóa Thành Công..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // xoa that bai
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterCategory(filterList,this);
        }
        return filter;
    }

    //view holder
    class HolderCategory extends RecyclerView.ViewHolder{
        //ui view of category.xml
        TextView categoryTv;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            //init ui view
            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
