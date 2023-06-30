package com.example.vbook.filters;

import android.widget.Filter;

import com.example.vbook.adapters.AdapterCategory;
import com.example.vbook.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter{
    // arraylist muon tim kiem
    ArrayList<ModelCategory> filterList;
    // adapter
    AdapterCategory adapterCategory;
    // contrustor

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // bawt buoc ko de null hoac trong
        if(constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                //validate
                if(filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;
        adapterCategory.notifyDataSetChanged();
    }
}
