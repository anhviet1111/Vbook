package com.example.vbook.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vbook.MyApplication;
import com.example.vbook.activities.PdfDetailActivity;
import com.example.vbook.activities.PdfEditActivity;
import com.example.vbook.databinding.RowPdfAdminBinding;
import com.example.vbook.filters.FilterPdfAdmin;
import com.example.vbook.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    //context
    private Context context;
    //arraylist
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    //binding
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;
    private static final String TAG="PDF_ADAPTER_TAG";

    private ProgressDialog progressDialog;

    //constructor
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }



    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding layout

        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfAdmin.HolderPdfAdmin holder, int position) {
        // get data, setdata,click

        //getdata
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();
        // timestamp dd/mm/yyyy
        String formattedDate = MyApplication.formatTimestamp(timestamp);
        //setdata
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);



        // load chi tiet category
        MyApplication.loadCategory(""+categoryId,
                holder.categoryTv);
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        // more option delete edit
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsnDialog(model,holder);
            }
        });

        // handle click, mo pdf detail
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsnDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId= model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        String[] options ={"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //edit , option new activity to edit
                            Intent intent = new Intent(context,PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);
                        } else if (which==1) {
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );

                        }
                    }
                })
                .show();
    }






    @Override
    public int getItemCount() {
        return pdfArrayList.size();// list size
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }


    //view class for row pdf admin
    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        // ui cua row pdf admin
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv,categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            //init view ui
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;

        }
    }

}
