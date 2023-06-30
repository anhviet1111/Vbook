package com.example.vbook.filters;

import android.widget.Filter;

import com.example.vbook.adapters.AdapterCategory;
import com.example.vbook.adapters.AdapterPdfAdmin;
import com.example.vbook.models.ModelCategory;
import com.example.vbook.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter{
    // arraylist muon tim kiem
    ArrayList<ModelPdf> filterList;
    // adapter
    AdapterPdfAdmin adapterPdfAdmin;
    // contrustor

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // bawt buoc ko de null  trong
        if(constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                //validate
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    // add to filter list
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
// them filter
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)results.values;
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
