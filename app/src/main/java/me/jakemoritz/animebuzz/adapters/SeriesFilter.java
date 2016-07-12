package me.jakemoritz.animebuzz.adapters;

import android.widget.Filter;

import me.jakemoritz.animebuzz.models.Series;
import me.jakemoritz.animebuzz.models.SeriesList;

public class SeriesFilter extends Filter {

    private SeriesRecyclerViewAdapter adapter;
    private SeriesList originalSeriesList;
    private SeriesList filteredSeriesList;

    public SeriesFilter(SeriesRecyclerViewAdapter adapter, SeriesList seriesList) {
        super();
        this.adapter = adapter;
        this.originalSeriesList = seriesList;
        this.filteredSeriesList = new SeriesList();
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
        adapter.getVisibleSeries().clear();
        adapter.getVisibleSeries().addAll((SeriesList) results.values);
        adapter.notifyDataSetChanged();
    }
}
