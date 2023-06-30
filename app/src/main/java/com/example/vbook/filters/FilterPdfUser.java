package com.example.vbook.filters;

import android.widget.Filter;

import com.example.vbook.adapters.AdapterPdfUser;
import com.example.vbook.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {

    //arraylist khi search
    ArrayList<ModelPdf> filterList;
    //adapter ma filter thuc thi
    AdapterPdfUser adapterPdfUser;
    //constructor


    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //gia tri ko dk null
        if (constraint!=null || constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();

            for (int i=0;i<filterList.size();i++){
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;

        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)results.values;
        adapterPdfUser.notifyDataSetChanged();
    }
}
