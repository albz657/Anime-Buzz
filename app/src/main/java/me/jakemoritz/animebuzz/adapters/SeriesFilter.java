package me.jakemoritz.animebuzz.adapters;

import android.widget.Filter;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import me.jakemoritz.animebuzz.helpers.SharedPrefsHelper;
import me.jakemoritz.animebuzz.models.Series;

class SeriesFilter extends Filter {

    private SeriesRecyclerViewAdapter adapter;
    private OrderedRealmCollection<Series> originalSeriesList;
    private OrderedRealmCollection<Series> filteredSeriesList;

    SeriesFilter(SeriesRecyclerViewAdapter adapter, OrderedRealmCollection<Series> originalSeriesList) {
        this.adapter = adapter;
        this.originalSeriesList = originalSeriesList;
        this.filteredSeriesList = new RealmList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        filteredSeriesList.clear();
        FilterResults results = new FilterResults();

        boolean prefersEnglish = SharedPrefsHelper.getInstance().prefersEnglish();

        if (constraint.length() == 0){
            filteredSeriesList.addAll(originalSeriesList);
        } else {
            String filterPattern = constraint.toString().toLowerCase().trim();

            for (Series series : originalSeriesList){
                String seriesName;
                if (prefersEnglish && !series.getEnglishTitle().isEmpty()){
                    seriesName = series.getEnglishTitle();
                } else {
                    seriesName = series.getName();
                }
                if (seriesName.toLowerCase().contains(filterPattern)){
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
        adapter.getData().addAll((OrderedRealmCollection<Series>) results.values);
        adapter.notifyDataSetChanged();
    }
}
