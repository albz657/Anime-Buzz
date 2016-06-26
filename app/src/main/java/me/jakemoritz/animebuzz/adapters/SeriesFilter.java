package me.jakemoritz.animebuzz.adapters;

import android.widget.Filter;

import java.util.ArrayList;

import me.jakemoritz.animebuzz.models.Series;

public class SeriesFilter extends Filter {

    private SeriesRecyclerViewAdapter adapter;
    private ArrayList<Series> originalSeriesList;
    private ArrayList<Series> filteredSeriesList;

    public SeriesFilter(SeriesRecyclerViewAdapter adapter, ArrayList<Series> seriesList) {
        super();
        this.adapter = adapter;
        this.originalSeriesList = seriesList;
        this.filteredSeriesList = new ArrayList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        filteredSeriesList.clear();
        FilterResults results = new FilterResults();

        if (constraint.length() == 0){
            filteredSeriesList.addAll(originalSeriesList);
        } else {
            String filterPattern = constraint.toString().toLowerCase().trim();

            for (Series series : originalSeriesList){
                if (series.getName().contains(filterPattern)){
                    filteredSeriesList.add(series);
                }
            }
        }
        results.values = filteredSeriesList;
        results.count = filteredSeriesList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.visibleSeries.clear();
        adapter.visibleSeries.addAll((ArrayList<Series>) results.values);
        adapter.notifyDataSetChanged();
    }
}
